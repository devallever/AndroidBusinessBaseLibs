package app.allever.android.lib.camera.proxy.camera2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import app.allever.android.lib.camera.core.BaseCameraEngine
import app.allever.android.lib.camera.core.CameraFacing
import app.allever.android.lib.camera.core.CameraState
import app.allever.android.lib.camera.core.PhotoResultCallback
import app.allever.android.lib.camera.core.VideoResultCallback
import java.io.FileOutputStream

/**
 * 基于 Camera2 API 的相机引擎实现。
 *
 * 使用主线程 Looper 承载 Camera2 回调，确保安全访问 View。
 * ImageReader 回调使用独立 HandlerThread 避免阻塞 UI。
 */
class Camera2Engine : BaseCameraEngine() {

    companion object {
        private const val TAG = "Camera2Engine"
        private const val MAX_IMAGES = 3
    }

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var mediaRecorder: MediaRecorder? = null
    private var characteristics: CameraCharacteristics? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val imageThread = HandlerThread("ImageReader").apply { start() }
    private val imageHandler = Handler(imageThread.looper)

    private var surfaceHolder: SurfaceHolder? = null
    private var isSurfaceReady = false
    private var pendingCameraFacing: CameraFacing? = null

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")
            isSurfaceReady = true
            pendingCameraFacing?.let { facing ->
                pendingCameraFacing = null
                doOpenCamera(facing)
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceDestroyed")
            isSurfaceReady = false
        }
    }

    override fun bindPreview(view: View) {
        super.bindPreview(view)
        when (view) {
            is SurfaceView -> {
                surfaceHolder?.removeCallback(surfaceCallback)
                surfaceHolder = view.holder
                surfaceHolder?.addCallback(surfaceCallback)
                isSurfaceReady = view.holder.surface.isValid
            }
            is TextureView -> {
                isSurfaceReady = view.isAvailable
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(cameraFacing: CameraFacing) {
        currentFacing = cameraFacing
        closeCameraInternal()

        if (!isSurfaceReady) {
            pendingCameraFacing = cameraFacing
            return
        }
        doOpenCamera(cameraFacing)
    }

    @SuppressLint("MissingPermission")
    private fun doOpenCamera(cameraFacing: CameraFacing) {
        val context = getContext() ?: return
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val cameraId = findCameraId(cameraManager, cameraFacing) ?: run {
            updateState(CameraState.IDLE)
            return
        }

        characteristics = cameraManager.getCameraCharacteristics(cameraId)

        try {
            cameraManager.openCamera(cameraId, cameraStateCallback, mainHandler)
        } catch (e: Exception) {
            updateState(CameraState.IDLE)
        }
    }

    override fun closeCamera() {
        closeCameraInternal()
        updateState(CameraState.IDLE)
    }

    override fun takePicture(resultCallback: PhotoResultCallback?) {
        if (!isPreviewing || cameraDevice == null || isCapturing) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        isCapturing = true
        updateState(CameraState.TAKING_PHOTO)
        photoCallback = resultCallback
        currentPhotoFile = createPhotoFile()

        try {
            val request = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                imageReader?.surface?.let { addTarget(it) }
            }
            captureSession?.capture(request.build(), null, mainHandler)
        } catch (e: Exception) {
            isCapturing = false
            updateState(CameraState.OPENED)
            resultCallback?.onFailure("Failed to take picture: ${e.message}")
            photoCallback = null
            currentPhotoFile = null
        }
    }

    override fun startRecordVideo(resultCallback: VideoResultCallback?) {
        if (!isPreviewing || cameraDevice == null || isRecording) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        videoCallback = resultCallback
        currentVideoFile = createVideoFile()

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                val profile = getCamcorderProfile(mConfig.videoQuality)
                setProfile(profile)
                setOutputFile(currentVideoFile!!.absolutePath)
                if (mConfig.maxVideoDuration > 0) {
                    setMaxDuration(mConfig.maxVideoDuration.toInt())
                }
                prepare()
            }

            val recorderSurface = mediaRecorder!!.surface
            captureSession?.stopRepeating()
            captureSession?.close()

            val targets = mutableListOf<Surface>()
            getPreviewSurface()?.let { targets.add(it) }
            targets.add(recorderSurface)

            cameraDevice!!.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    startRecording(session, recorderSurface)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    releaseMediaRecorder()
                    videoCallback?.onFailure("Recording session config failed")
                    videoCallback = null
                }
            }, mainHandler)
        } catch (e: Exception) {
            releaseMediaRecorder()
            resultCallback?.onFailure("Failed to start recording: ${e.message}")
            videoCallback = null
        }
    }

    override fun stopRecordVideo() {
        if (!isRecording || mediaRecorder == null) return

        try {
            mediaRecorder?.stop()
            currentVideoFile?.let { videoCallback?.onSuccess(it) }
        } catch (e: Exception) {
            videoCallback?.onFailure("Failed to stop recording: ${e.message}")
        } finally {
            releaseMediaRecorder()
            isRecording = false
            updateState(CameraState.OPENED)
            restartPreviewSession()
            videoCallback = null
            currentVideoFile = null
        }
    }

    override fun release() {
        stopRecordVideo()
        closeCameraInternal()
        surfaceHolder?.removeCallback(surfaceCallback)
        surfaceHolder = null
        imageThread.quitSafely()
        mediaRecorder = null
        characteristics = null
        cancelCameraScope()
        resetState()
    }

    // ==================== 内部方法 ====================

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createPreviewSession(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
            isPreviewing = false
            updateState(CameraState.IDLE)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    private fun getPreviewSurface(): Surface? {
        val view = getPreviewView() ?: return null
        return when (view) {
            is SurfaceView -> if (view.holder.surface.isValid) view.holder.surface else null
            is TextureView -> view.surfaceTexture?.let { Surface(it) }
            else -> null
        }
    }

    private fun createPreviewSession(camera: CameraDevice) {
        try {
            val previewSurface = getPreviewSurface() ?: run {
                updateState(CameraState.IDLE)
                return
            }

            val configMap = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val jpegSize = configMap?.getOutputSizes(ImageFormat.JPEG)
                ?.maxByOrNull { it.width * it.height } ?: Size(1920, 1080)

            imageReader?.close()
            imageReader = ImageReader.newInstance(jpegSize.width, jpegSize.height, ImageFormat.JPEG, MAX_IMAGES)
            imageReader?.setOnImageAvailableListener({ reader -> handleImageAvailable(reader) }, imageHandler)

            camera.createCaptureSession(
                listOf(previewSurface, imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        startPreview(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        isPreviewing = false
                        updateState(CameraState.IDLE)
                    }
                },
                mainHandler
            )
        } catch (e: Exception) {
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    private fun startPreview(session: CameraCaptureSession) {
        try {
            val previewSurface = getPreviewSurface() ?: run {
                isPreviewing = false
                updateState(CameraState.IDLE)
                return
            }

            val request = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }
            session.setRepeatingRequest(request!!.build(), null, mainHandler)

            isPreviewing = true
            updateState(CameraState.OPENED)
        } catch (e: Exception) {
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    private fun startRecording(session: CameraCaptureSession, recorderSurface: Surface) {
        try {
            val request = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)?.apply {
                getPreviewSurface()?.let { addTarget(it) }
                addTarget(recorderSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            }
            session.setRepeatingRequest(request!!.build(), null, mainHandler)

            mediaRecorder?.start()
            isRecording = true
            updateState(CameraState.RECORDING)
        } catch (e: Exception) {
            releaseMediaRecorder()
            videoCallback?.onFailure("Failed to start recording: ${e.message}")
            videoCallback = null
        }
    }

    private fun restartPreviewSession() {
        val camera = cameraDevice ?: return
        try {
            val targets = mutableListOf<Surface>()
            getPreviewSurface()?.let { targets.add(it) }
            imageReader?.surface?.let { targets.add(it) }

            camera.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    startPreview(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, mainHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart preview session", e)
        }
    }

    private fun handleImageAvailable(reader: ImageReader) {
        val image = reader.acquireLatestImage() ?: return
        try {
            if (!isCapturing) return
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            savePhotoBytes(bytes)
        } finally {
            image.close()
        }
    }

    private fun savePhotoBytes(bytes: ByteArray) {
        launchCameraTask {
            try {
                val photoFile = currentPhotoFile ?: return@launchCameraTask
                FileOutputStream(photoFile).use { it.write(bytes) }

                val degree = getPhotoRotationDegree()
                if (degree != 0) rotatePhotoFile(photoFile, degree)

                photoCallback?.onSuccess(photoFile)
            } catch (e: Exception) {
                photoCallback?.onFailure("Failed to save photo: ${e.message}")
            } finally {
                isCapturing = false
                updateState(CameraState.OPENED)
                photoCallback = null
                currentPhotoFile = null
            }
        }
    }

    private fun closeCameraInternal() {
        pendingCameraFacing = null
        val session = captureSession
        captureSession = null
        val camera = cameraDevice
        cameraDevice = null

        session?.let { s ->
            try { s.stopRepeating() } catch (e: Exception) { Log.w(TAG, "stopRepeating", e) }
            try { s.abortCaptures() } catch (e: Exception) { Log.w(TAG, "abortCaptures", e) }
            try { s.close() } catch (e: Exception) { Log.w(TAG, "close session", e) }
        }
        camera?.let { c -> try { c.close() } catch (e: Exception) { Log.w(TAG, "close camera", e) } }

        imageReader?.close()
        imageReader = null
        characteristics = null
        isPreviewing = false
    }

    private fun releaseMediaRecorder() {
        try { mediaRecorder?.stop() } catch (e: Exception) {}
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    private fun findCameraId(manager: CameraManager, facing: CameraFacing): String? {
        val targetFacing = if (facing == CameraFacing.FACE_BACK)
            CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT
        return manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == targetFacing
        }
    }

    private fun getPhotoRotationDegree(): Int {
        return characteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
    }
}
