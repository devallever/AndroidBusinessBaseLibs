package app.allever.android.lib.camera.proxy.camera2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.TotalCaptureResult
import android.media.CamcorderProfile
import android.media.Image
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
import app.allever.android.lib.camera.core.ResultCallback
import app.allever.android.lib.camera.core.VideoQuality
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

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

    /** Camera2 回调使用主线程，确保安全访问 View */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** ImageReader 使用独立线程，避免阻塞 UI */
    private val imageThread = HandlerThread("ImageReader").apply { start() }
    private val imageHandler = Handler(imageThread.looper)

    /** Surface 生命周期管理 */
    private var surfaceHolder: SurfaceHolder? = null
    private var isSurfaceReady = false

    /** 待打开相机的标记（surface 未就绪时） */
    private var pendingCameraFacing: CameraFacing? = null

    private var photoCallback: ResultCallback? = null
    private var videoCallback: ResultCallback? = null
    private var currentPhotoFile: File? = null
    private var currentVideoFile: File? = null

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")
            isSurfaceReady = true
            // 如果有等待打开的相机请求，现在 surface 就绪了
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
                // surface 可能已经创建
                isSurfaceReady = view.holder.surface.isValid
                Log.d(TAG, "bindPreview SurfaceView, isSurfaceReady=$isSurfaceReady")
            }
            is TextureView -> {
                isSurfaceReady = view.isAvailable
                Log.d(TAG, "bindPreview TextureView, isSurfaceReady=$isSurfaceReady")
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(cameraFacing: CameraFacing) {
        currentFacing = cameraFacing
        Log.d(TAG, "openCamera: $cameraFacing, isSurfaceReady=$isSurfaceReady")

        closeCameraInternal()

        if (!isSurfaceReady) {
            // surface 未就绪，等待 surfaceCreated 回调
            pendingCameraFacing = cameraFacing
            Log.d(TAG, "Surface not ready, waiting for surfaceCreated")
            return
        }

        doOpenCamera(cameraFacing)
    }

    @SuppressLint("MissingPermission")
    private fun doOpenCamera(cameraFacing: CameraFacing) {
        val context = getPreviewView()?.context ?: return
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val cameraId = findCameraId(cameraManager, cameraFacing)
        if (cameraId == null) {
            Log.e(TAG, "Cannot find camera for facing: $cameraFacing")
            updateState(CameraState.IDLE)
            return
        }

        characteristics = cameraManager.getCameraCharacteristics(cameraId)
        Log.d(TAG, "Opening camera: id=$cameraId")

        try {
            cameraManager.openCamera(cameraId, cameraStateCallback, mainHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera", e)
            updateState(CameraState.IDLE)
        }
    }

    override fun closeCamera() {
        closeCameraInternal()
        updateState(CameraState.IDLE)
    }

    override fun switchCamera() {
        val newFacing = if (currentFacing == CameraFacing.FACE_BACK) {
            CameraFacing.FACE_FRONT
        } else {
            CameraFacing.FACE_BACK
        }
        openCamera(newFacing)
    }

    override fun takePicture(resultCallback: ResultCallback?) {
        if (!isPreviewing || cameraDevice == null || isCapturing) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        isCapturing = true
        updateState(CameraState.TAKING_PHOTO)
        photoCallback = resultCallback

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        currentPhotoFile = File(mConfig.photoSavePath, "IMG_$timestamp.jpg")

        try {
            val request = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                imageReader?.surface?.let { addTarget(it) }
            }
            captureSession?.capture(
                request.build(),
                object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        Log.d(TAG, "onCaptureCompleted")
                    }
                },
                mainHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to take picture", e)
            isCapturing = false
            updateState(CameraState.OPENED)
            resultCallback?.onFailure("Failed to take picture: ${e.message}")
            photoCallback = null
            currentPhotoFile = null
        }
    }

    override fun startRecordVideo(resultCallback: ResultCallback?) {
        if (!isPreviewing || cameraDevice == null || isRecording) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        videoCallback = resultCallback

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        currentVideoFile = File(mConfig.videoSavePath, "VID_$timestamp.mp4")

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

            val previewSurface = getPreviewSurface()
            val targets = mutableListOf<Surface>()
            previewSurface?.let { targets.add(it) }
            targets.add(recorderSurface)

            cameraDevice!!.createCaptureSession(
                targets,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        startRecording(session, recorderSurface)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Recording session config failed")
                        releaseMediaRecorder()
                        videoCallback?.onFailure("Recording session config failed")
                        videoCallback = null
                    }
                },
                mainHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            releaseMediaRecorder()
            resultCallback?.onFailure("Failed to start recording: ${e.message}")
            videoCallback = null
        }
    }

    override fun stopRecordVideo() {
        if (!isRecording || mediaRecorder == null) return

        try {
            mediaRecorder?.stop()
            currentVideoFile?.let { file ->
                videoCallback?.onSuccess(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
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
        photoCallback = null
        videoCallback = null
        currentPhotoFile = null
        currentVideoFile = null
        characteristics = null

        cancelCameraScope()
        resetState()
    }

    // ==================== 内部方法 ====================

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "CameraDevice onOpened")
            cameraDevice = camera
            createPreviewSession(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "CameraDevice onDisconnected")
            camera.close()
            cameraDevice = null
            isPreviewing = false
            updateState(CameraState.IDLE)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "CameraDevice onError: $error")
            camera.close()
            cameraDevice = null
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    /**
     * 直接从预览 View 获取 Surface。
     */
    private fun getPreviewSurface(): Surface? {
        val view = getPreviewView() ?: return null
        return when (view) {
            is SurfaceView -> {
                val holder = view.holder
                if (holder.surface.isValid) holder.surface else null
            }
            is TextureView -> {
                view.surfaceTexture?.let { Surface(it) }
            }
            else -> null
        }
    }

    private fun createPreviewSession(camera: CameraDevice) {
        try {
            val previewSurface = getPreviewSurface()
            if (previewSurface == null) {
                Log.e(TAG, "Preview surface is null")
                updateState(CameraState.IDLE)
                return
            }
            Log.d(TAG, "Creating preview session, surface=$previewSurface")

            val configMap = characteristics?.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )
            val jpegSize = configMap?.getOutputSizes(ImageFormat.JPEG)
                ?.maxByOrNull { it.width * it.height }
                ?: Size(1920, 1080)

            imageReader?.close()
            imageReader = ImageReader.newInstance(
                jpegSize.width, jpegSize.height, ImageFormat.JPEG, MAX_IMAGES
            )
            imageReader?.setOnImageAvailableListener(
                { reader -> handleImageAvailable(reader) },
                imageHandler
            )

            val targets = listOf(previewSurface, imageReader!!.surface)

            camera.createCaptureSession(
                targets,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.d(TAG, "CaptureSession onConfigured")
                        captureSession = session
                        startPreview(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "CaptureSession onConfigureFailed")
                        isPreviewing = false
                        updateState(CameraState.IDLE)
                    }
                },
                mainHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create preview session", e)
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    private fun startPreview(session: CameraCaptureSession) {
        try {
            val previewSurface = getPreviewSurface()
            if (previewSurface == null) {
                Log.e(TAG, "Preview surface is null in startPreview")
                isPreviewing = false
                updateState(CameraState.IDLE)
                return
            }

            val request = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }
            session.setRepeatingRequest(
                request!!.build(),
                object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureFailed(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        failure: CaptureFailure
                    ) {
                        Log.e(TAG, "onCaptureFailed: reason=${failure.reason}")
                    }
                },
                mainHandler
            )

            isPreviewing = true
            updateState(CameraState.OPENED)
            Log.d(TAG, "Preview started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start preview", e)
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    private fun startRecording(session: CameraCaptureSession, recorderSurface: Surface) {
        try {
            val previewSurface = getPreviewSurface()
            val request = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)?.apply {
                previewSurface?.let { addTarget(it) }
                addTarget(recorderSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
            }
            session.setRepeatingRequest(request!!.build(), null, mainHandler)

            mediaRecorder?.start()
            isRecording = true
            updateState(CameraState.RECORDING)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            releaseMediaRecorder()
            videoCallback?.onFailure("Failed to start recording: ${e.message}")
            videoCallback = null
        }
    }

    private fun restartPreviewSession() {
        val camera = cameraDevice ?: return
        try {
            val previewSurface = getPreviewSurface()
            val targets = mutableListOf<Surface>()
            previewSurface?.let { targets.add(it) }
            imageReader?.surface?.let { targets.add(it) }

            camera.createCaptureSession(
                targets,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        startPreview(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Restart preview session failed")
                    }
                },
                mainHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart preview session", e)
        }
    }

    private fun handleImageAvailable(reader: ImageReader) {
        val image = reader.acquireLatestImage() ?: return
        try {
            if (!isCapturing) return

            // 必须在 image.close() 之前提取字节数据
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

                FileOutputStream(photoFile).use { fos ->
                    fos.write(bytes)
                }

                val degree = getPhotoRotationDegree()
                if (degree != 0) {
                    rotatePhotoFile(photoFile, degree)
                }

                photoCallback?.onSuccess(photoFile)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save photo", e)
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
        // 先置 null 防止其他方法引用已关闭的对象
        val session = captureSession
        captureSession = null

        val camera = cameraDevice
        cameraDevice = null

        // 每个操作独立 try-catch，任一失败不影响后续清理
        session?.let { s ->
            try { s.stopRepeating() } catch (e: Exception) { Log.w(TAG, "stopRepeating", e) }
            try { s.abortCaptures() } catch (e: Exception) { Log.w(TAG, "abortCaptures", e) }
            try { s.close() } catch (e: Exception) { Log.w(TAG, "close session", e) }
        }

        camera?.let { c ->
            try { c.close() } catch (e: Exception) { Log.w(TAG, "close camera", e) }
        }

        imageReader?.close()
        imageReader = null

        isPreviewing = false
    }

    private fun releaseMediaRecorder() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // ignore
        }
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    // ==================== 辅助方法 ====================

    private fun findCameraId(manager: CameraManager, facing: CameraFacing): String? {
        val targetFacing = if (facing == CameraFacing.FACE_BACK) {
            CameraCharacteristics.LENS_FACING_BACK
        } else {
            CameraCharacteristics.LENS_FACING_FRONT
        }
        for (id in manager.cameraIdList) {
            val chars = manager.getCameraCharacteristics(id)
            val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing == targetFacing) {
                return id
            }
        }
        return null
    }

    private fun getCamcorderProfile(quality: VideoQuality): CamcorderProfile {
        val qualityLevel = when (quality) {
            VideoQuality.SD_480P -> CamcorderProfile.QUALITY_480P
            VideoQuality.HD_720P -> CamcorderProfile.QUALITY_720P
            VideoQuality.FHD_1080P -> CamcorderProfile.QUALITY_1080P
            VideoQuality.UHD_4K -> CamcorderProfile.QUALITY_2160P
        }
        return if (CamcorderProfile.hasProfile(qualityLevel)) {
            CamcorderProfile.get(qualityLevel)
        } else {
            CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        }
    }

    private fun getPhotoRotationDegree(): Int {
        val sensorOrientation = characteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
        return if (currentFacing == CameraFacing.FACE_FRONT) {
            (sensorOrientation + 180) % 360
        } else {
            sensorOrientation
        }
    }

    private fun rotatePhotoFile(file: File, degree: Int) {
        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath) ?: return
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degree.toFloat())
        val rotated = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        FileOutputStream(file).use { fos ->
            rotated.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
        }
        rotated.recycle()
    }
}
