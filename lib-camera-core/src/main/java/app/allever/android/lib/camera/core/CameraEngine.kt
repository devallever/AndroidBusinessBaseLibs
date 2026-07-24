package app.allever.android.lib.camera.core

import android.content.Context
import android.content.Intent
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

@Suppress("DEPRECATION")
class CameraEngine : BaseCameraEngine() {
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mPreviewSize: Camera.Size? = null
    private var mPreviewSurface: Surface? = null
    private var mVideoCallback: ResultCallback? = null
    private var mCurrentVideoFile: File? = null

    override fun openCamera(cameraFacing: CameraFacing) {
        if (isPreviewing) {
            closeCamera()
        }

        currentFacing = cameraFacing
        startCameraThread()

        postCameraTask {
            val cameraId = if (cameraFacing == CameraFacing.FACE_BACK) 0 else 1

            try {
                mCamera = Camera.open(cameraId)
                configureCamera()
                startPreview()
                updateState(CameraState.OPENED)
            } catch (e: Exception) {
                updateState(CameraState.IDLE)
            }
        }
    }

    fun openCamera() {
        openCamera(mConfig.cameraFacing)
    }

    override fun closeCamera() {
        postCameraTask {
            stopPreview()
            releaseCamera()
            stopCameraThread()
            updateState(CameraState.IDLE)
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

        mCamera?.takePicture(
            null,
            null,
            null,
            Camera.PictureCallback { data, camera ->
                try {
                    val photoFile = savePhotoToGallery(data)
                    resultCallback?.onSuccess(photoFile)
                } catch (e: IOException) {
                    resultCallback?.onFailure("Failed to save photo: ${e.message}")
                } finally {
                    isCapturing = false
                    updateState(CameraState.OPENED)
                    camera.startPreview()
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
        mSurfaceHolder = null
        mSurfaceTexture = null
        mPreviewSurface = null
        mVideoCallback = null
        mCurrentVideoFile = null
        resetState()
    }

    private fun configureCamera() {
        mCamera?.let { camera ->
            val parameters = camera.parameters

            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO

            when (mConfig.flashMode) {
                FlashMode.OFF -> parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                FlashMode.ON -> parameters.flashMode = Camera.Parameters.FLASH_MODE_ON
                FlashMode.AUTO -> parameters.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                FlashMode.TORCH -> parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            }

            mPreviewSize = getOptimalPreviewSize(parameters.supportedPreviewSizes)
            mPreviewSize?.let {
                parameters.setPreviewSize(it.width, it.height)
            }

            val pictureSize = getOptimalPictureSize(parameters.supportedPictureSizes)
            pictureSize?.let {
                parameters.setPictureSize(it.width, it.height)
            }

            camera.parameters = parameters

            val displayOrientation = getDisplayOrientation()
            camera.setDisplayOrientation(displayOrientation)
        }
    }

    private fun startPreview() {
        val view = getPreviewView() ?: return

        try {
            when (view) {
                is SurfaceView -> {
                    mSurfaceHolder = view.holder
                    mSurfaceHolder?.addCallback(SurfaceCallback())
                    mCamera?.setPreviewDisplay(mSurfaceHolder)
                    mPreviewSurface = mSurfaceHolder?.surface
                }
                is TextureView -> {
                    mSurfaceTexture = view.surfaceTexture
                    mCamera?.setPreviewTexture(mSurfaceTexture)
                    mPreviewSurface = Surface(mSurfaceTexture)
                }
            }
            mCamera?.startPreview()
            isPreviewing = true
        } catch (e: IOException) {
            isPreviewing = false
        }
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

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>): Camera.Size? {
        val targetRatio = when (mConfig.aspectRatio) {
            AspectRatio.RATIO_1_1 -> 1.0
            AspectRatio.RATIO_3_4 -> 3.0 / 4.0
            AspectRatio.RATIO_16_9 -> 16.0 / 9.0
            AspectRatio.FULL_SCREEN -> 9.0 / 16.0
        }

        var optimalSize: Camera.Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height.toDouble()
            val diff = Math.abs(ratio - targetRatio)
            if (diff < minDiff) {
                minDiff = diff
                optimalSize = size
            }
        }

        return optimalSize ?: sizes.firstOrNull()
    }

    private fun getOptimalPictureSize(sizes: List<Camera.Size>): Camera.Size? {
        var largestSize: Camera.Size? = null
        var maxArea = 0

        for (size in sizes) {
            val area = size.width * size.height
            if (area > maxArea) {
                maxArea = area
                largestSize = size
            }
        }

        return largestSize
    }

    private fun getDisplayOrientation(): Int {
        val view = getPreviewView() ?: return 90
        val context = view.context
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation

        val cameraId = if (currentFacing == CameraFacing.FACE_BACK) 0 else 1
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)

        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }

        return result
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

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                mCamera?.setPreviewDisplay(holder)
                mCamera?.startPreview()
            } catch (e: IOException) {
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            if (holder.surface == null) {
                return
            }

            try {
                mCamera?.stopPreview()
                configureCamera()
                mCamera?.setPreviewDisplay(holder)
                mCamera?.startPreview()
            } catch (e: IOException) {
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }
    }
}