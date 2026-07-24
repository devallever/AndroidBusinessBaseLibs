package app.allever.android.lib.camera.core

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.MediaRecorder
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Suppress("DEPRECATION")
class CameraEngine : BaseCameraEngine() {
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mPreviewSize: Camera.Size? = null
    private var mPreviewSurface: Surface? = null

    override fun openCamera(cameraFacing: CameraFacing) {
        currentFacing = cameraFacing

        launchCameraTask {
            if (mCamera != null) {
                stopPreview()
                releaseCamera()
            }

            val cameraId = if (cameraFacing == CameraFacing.FACE_BACK) {
                CameraInfo.CAMERA_FACING_BACK
            } else {
                CameraInfo.CAMERA_FACING_FRONT
            }

            try {
                mCamera = Camera.open(cameraId)
                if (mCamera == null) {
                    updateState(CameraState.IDLE)
                    return@launchCameraTask
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
                        supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ->
                            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                        supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ->
                            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                        supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) ->
                            params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                    }
                }

                when (mConfig.flashMode) {
                    FlashMode.OFF -> params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                    FlashMode.ON -> params.flashMode = Camera.Parameters.FLASH_MODE_ON
                    FlashMode.AUTO -> params.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                    FlashMode.TORCH -> params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                }

                camera.parameters = params

                when (val view = getPreviewView()) {
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
        launchCameraTask {
            if (mCamera != null) {
                stopPreview()
                releaseCamera()
                updateState(CameraState.IDLE)
            }
        }
    }

    override fun takePicture(resultCallback: ResultCallback?) {
        if (!isPreviewing || mCamera == null || isCapturing) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        isCapturing = true
        updateState(CameraState.TAKING_PHOTO)

        val cameraId = if (currentFacing == CameraFacing.FACE_BACK) {
            CameraInfo.CAMERA_FACING_BACK
        } else {
            CameraInfo.CAMERA_FACING_FRONT
        }

        mCamera?.takePicture(null, null, null, Camera.PictureCallback { data, camera ->
            launchCameraTask {
                try {
                    var additionalDegree = 0
                    if (currentFacing == CameraFacing.FACE_FRONT) {
                        additionalDegree = 180
                    }
                    val degree = getDisplayOrientation(cameraId) + additionalDegree
                    val rotatedData = rotateImageBytes(data, degree)
                    val photoFile = savePhotoToGallery(rotatedData)
                    withContext(Dispatchers.Main) {
                        resultCallback?.onSuccess(photoFile)
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        resultCallback?.onFailure("Failed to save photo: ${e.message}")
                    }
                } finally {
                    isCapturing = false
                    updateState(CameraState.OPENED)
                    camera.startPreview()
                }
            }
        })
    }

    override fun startRecordVideo(resultCallback: ResultCallback?) {
        if (!isPreviewing || mCamera == null || isRecording) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        videoCallback = resultCallback
        currentVideoFile = createVideoFile()

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
        if (!isRecording || mMediaRecorder == null) return

        try {
            mMediaRecorder?.stop()
            currentVideoFile?.let { file ->
                scanVideoToGallery(file)
                videoCallback?.onSuccess(file)
            }
        } catch (e: Exception) {
            videoCallback?.onFailure("Failed to stop recording: ${e.message}")
        } finally {
            releaseMediaRecorder()
            isRecording = false
            updateState(CameraState.OPENED)
            videoCallback = null
            currentVideoFile = null
        }
    }

    override fun release() {
        stopRecordVideo()
        closeCamera()
        cancelCameraScope()
        mSurfaceHolder = null
        mSurfaceTexture = null
        mPreviewSurface = null
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
        val camera = mCamera ?: return false
        val videoFile = currentVideoFile ?: return false

        mMediaRecorder = MediaRecorder().apply {
            camera.unlock()
            setCamera(camera)
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)

            val profile = getCamcorderProfile(mConfig.videoQuality)
            setProfile(profile)
            setOutputFile(videoFile.absolutePath)

            mPreviewSurface?.let { setPreviewDisplay(it) }

            if (mConfig.maxVideoDuration > 0) {
                setMaxDuration(mConfig.maxVideoDuration.toInt())
            }
        }

        return try {
            mMediaRecorder?.prepare()
            true
        } catch (e: IOException) {
            releaseMediaRecorder()
            false
        }
    }

    private fun getBestSupportedSize(sizes: List<Camera.Size>, targetSize: Point): Camera.Size {
        if (sizes.isEmpty()) return mCamera!!.parameters.previewSize

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
        val windowManager = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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

        return if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            val result = (info.orientation + degrees) % 360
            (360 - result) % 360
        } else {
            (info.orientation - degrees + 360) % 360
        }
    }
}
