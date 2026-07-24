package app.allever.android.lib.camera.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class CameraEngine : BaseCameraEngine() {
    private val mExecutor = Executors.newSingleThreadExecutor()

    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mPreviewSize: Camera.Size? = null
    private var mPreviewSurface: Surface? = null
    private var mVideoCallback: ResultCallback? = null
    private var mCurrentVideoFile: File? = null

    override fun openCamera(cameraFacing: CameraFacing) {
        currentFacing = cameraFacing

        mExecutor.execute {
            if (mCamera != null) {
                stopPreview()
                releaseCamera()
            }

            val cameraId = if (cameraFacing == CameraFacing.FACE_BACK) {
                Camera.CameraInfo.CAMERA_FACING_BACK
            } else {
                Camera.CameraInfo.CAMERA_FACING_FRONT
            }

            try {
                mCamera = Camera.open(cameraId)

                if (mCamera == null) {
                    updateState(CameraState.IDLE)
                    return@execute
                }

                val camera = mCamera!!
                val params = camera.parameters

                params.previewFormat = ImageFormat.NV21

                val bestPreviewSize = getBestSupportedSize(params.supportedPreviewSizes, Point(1920, 1080))
                params.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height)

                val bestPictureSize = getBestSupportedSize(params.supportedPictureSizes, Point(1920, 1080))
                params.setPictureSize(bestPictureSize.width, bestPictureSize.height)

                val supportedFocusModes = params.supportedFocusModes
                if (supportedFocusModes.isNotEmpty()) {
                    when {
                        supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) -> {
                            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                        }
                        supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) -> {
                            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                        }
                        supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) -> {
                            params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                        }
                    }
                }

                when (mConfig.flashMode) {
                    FlashMode.OFF -> params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                    FlashMode.ON -> params.flashMode = Camera.Parameters.FLASH_MODE_ON
                    FlashMode.AUTO -> params.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                    FlashMode.TORCH -> params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                }

                camera.parameters = params

                val view = getPreviewView()
                when (view) {
                    is SurfaceView -> {
                        mSurfaceHolder = view.holder
                        camera.setPreviewDisplay(mSurfaceHolder)
                        mPreviewSurface = mSurfaceHolder?.surface
                    }
                    is TextureView -> {
                        mSurfaceTexture = view.surfaceTexture
                        camera.setPreviewTexture(mSurfaceTexture)
                        mPreviewSurface = Surface(mSurfaceTexture)
                    }
                }

                camera.setDisplayOrientation(getDisplayOrientation(cameraId))
                camera.startPreview()
                isPreviewing = true
                updateState(CameraState.OPENED)
            } catch (e: Exception) {
                isPreviewing = false
                updateState(CameraState.IDLE)
            }
        }
    }

    fun openCamera() {
        openCamera(mConfig.cameraFacing)
    }

    override fun closeCamera() {
        mExecutor.execute {
            if (mCamera != null) {
                stopPreview()
                releaseCamera()
                updateState(CameraState.IDLE)
            }
        }
    }

    override fun switchCamera() {
        val newFacing = if (currentFacing == CameraFacing.FACE_BACK) CameraFacing.FACE_FRONT else CameraFacing.FACE_BACK
        openCamera(newFacing)
    }

    override fun takePicture(resultCallback: ResultCallback?) {
        if (!isPreviewing || mCamera == null || isCapturing) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        isCapturing = true
        updateState(CameraState.TAKING_PHOTO)

        val cameraId = if (currentFacing == CameraFacing.FACE_BACK) {
            Camera.CameraInfo.CAMERA_FACING_BACK
        } else {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        }

        mCamera?.takePicture(
            null,
            null,
            null,
            Camera.PictureCallback { data, camera ->
                mExecutor.execute {
                    try {
                        var additionalDegree = 0
                        if (currentFacing == CameraFacing.FACE_FRONT) {
                            additionalDegree = 180
                        }
                        val degree = getDisplayOrientation(cameraId) + additionalDegree
                        val rotatedData = rotateImage(data, degree)
                        val photoFile = savePhotoToGallery(rotatedData)
                        resultCallback?.onSuccess(photoFile)
                    } catch (e: IOException) {
                        resultCallback?.onFailure("Failed to save photo: ${e.message}")
                    } finally {
                        isCapturing = false
                        updateState(CameraState.OPENED)
                        camera.startPreview()
                    }
                }
            }
        )
    }

    override fun startRecordVideo(resultCallback: ResultCallback?) {
        if (!isPreviewing || mCamera == null || isRecording) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        mVideoCallback = resultCallback
        mCurrentVideoFile = createVideoFile()

        if (!prepareMediaRecorder()) {
            resultCallback?.onFailure("Failed to prepare media recorder")
            return
        }

        try {
            mMediaRecorder?.start()
            isRecording = true
            updateState(CameraState.RECORDING)
        } catch (e: Exception) {
            releaseMediaRecorder()
            resultCallback?.onFailure("Failed to start recording: ${e.message}")
        }
    }

    override fun stopRecordVideo() {
        if (!isRecording || mMediaRecorder == null) {
            return
        }

        try {
            mMediaRecorder?.stop()
            mCurrentVideoFile?.let { file ->
                scanVideoToGallery(file)
                mVideoCallback?.onSuccess(file)
            }
        } catch (e: Exception) {
            mVideoCallback?.onFailure("Failed to stop recording: ${e.message}")
        } finally {
            releaseMediaRecorder()
            isRecording = false
            updateState(CameraState.OPENED)
            mVideoCallback = null
            mCurrentVideoFile = null
        }
    }

    override fun release() {
        stopRecordVideo()
        closeCamera()
        mExecutor.shutdown()
        mSurfaceHolder = null
        mSurfaceTexture = null
        mPreviewSurface = null
        mVideoCallback = null
        mCurrentVideoFile = null
        resetState()
    }

    private fun stopPreview() {
        mCamera?.stopPreview()
        isPreviewing = false
    }

    private fun releaseCamera() {
        mCamera?.release()
        mCamera = null
    }

    private fun releaseMediaRecorder() {
        mMediaRecorder?.reset()
        mMediaRecorder?.release()
        mMediaRecorder = null
        mCamera?.lock()
    }

    private fun prepareMediaRecorder(): Boolean {
        mCamera?.let { camera ->
            mCurrentVideoFile?.let { videoFile ->
                mMediaRecorder = MediaRecorder().apply {
                    camera.unlock()
                    setCamera(camera)

                    setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                    setVideoSource(MediaRecorder.VideoSource.CAMERA)

                    val profile = when (mConfig.videoQuality) {
                        VideoQuality.SD_480P -> CamcorderProfile.get(CamcorderProfile.QUALITY_480P)
                        VideoQuality.HD_720P -> CamcorderProfile.get(CamcorderProfile.QUALITY_720P)
                        VideoQuality.FHD_1080P -> CamcorderProfile.get(CamcorderProfile.QUALITY_1080P)
                        VideoQuality.UHD_4K -> CamcorderProfile.get(CamcorderProfile.QUALITY_2160P)
                    }
                    setProfile(profile)

                    setOutputFile(videoFile.absolutePath)

                    mPreviewSurface?.let {
                        setPreviewDisplay(it)
                    }

                    if (mConfig.maxVideoDuration > 0) {
                        setMaxDuration(mConfig.maxVideoDuration.toInt())
                    }
                }

                try {
                    mMediaRecorder?.prepare()
                    return true
                } catch (e: IOException) {
                    releaseMediaRecorder()
                    return false
                }
            }
        }
        return false
    }

    private fun getBestSupportedSize(sizes: List<Camera.Size>, targetSize: Point): Camera.Size {
        if (sizes.isEmpty()) {
            return mCamera!!.parameters.previewSize
        }

        val sortedSizes = sizes.sortedWith(compareByDescending { it.width * it.height })
        var bestSize = sortedSizes[0]
        val targetRatio = targetSize.x.toFloat() / targetSize.y.toFloat()

        for (size in sortedSizes) {
            val sizeRatio = size.width.toFloat() / size.height.toFloat()
            if (Math.abs(sizeRatio - targetRatio) < Math.abs(bestSize.width.toFloat() / bestSize.height.toFloat() - targetRatio)) {
                bestSize = size
            }
        }

        return bestSize
    }

    private fun getDisplayOrientation(cameraId: Int): Int {
        val view = getPreviewView() ?: return 90
        val context = view.context
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation

        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)

        val result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            return (360 - result) % 360
        } else {
            return (info.orientation - degrees + 360) % 360
        }
    }

    private fun rotateImage(data: ByteArray, degree: Int): ByteArray {
        if (degree % 360 == 0) {
            return data
        }

        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degree.toFloat())

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        bitmap.recycle()

        val outputStream = java.io.ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        rotatedBitmap.recycle()

        return outputStream.toByteArray()
    }

    private fun createPhotoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(mConfig.photoSavePath, "IMG_$timestamp.jpg")
    }

    private fun createVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(mConfig.videoSavePath, "VID_$timestamp.mp4")
    }

    @Throws(IOException::class)
    private fun savePhotoToGallery(data: ByteArray): File {
        val view = getPreviewView() ?: throw IOException("Preview view is null")
        val context = view.context
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timestamp.jpg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraCore")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(data)
                }
                val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
                cursor?.moveToFirst()?.let {
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                    cursor.close()
                    return File(path)
                }
                cursor?.close()
            }
        }

        val photoFile = File(mConfig.photoSavePath, fileName)
        FileOutputStream(photoFile).use { fos ->
            fos.write(data)
        }
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(photoFile)
        context.sendBroadcast(mediaScanIntent)
        return photoFile
    }

    private fun scanVideoToGallery(file: File) {
        val view = getPreviewView() ?: return
        val context = view.context

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CameraCore")
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
        }
    }
}