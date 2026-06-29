package app.allever.android.lib.camera.proxy.camera2
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import app.allever.android.lib.core.camera.AspectRatio
import app.allever.android.lib.core.camera.BaseCameraManager
import app.allever.android.lib.core.camera.CameraResultCallback
import app.allever.android.lib.core.camera.FlashMode
import app.allever.android.lib.core.camera.ICameraManager
import app.allever.android.lib.core.camera.RecordCallback
import java.io.File
import java.util.Timer
import java.util.TimerTask

class Camera2Manager(context: Context, container: ViewGroup) : BaseCameraManager(context, container) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var mediaRecorder: MediaRecorder? = null
    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null
    private var cameraId = "0"
    private var previewSize: Size = Size(1080, 1920)
    private var characteristics: CameraCharacteristics? = null
    private var zoomRect: Rect? = null
    private var recordCallback: RecordCallback? = null
    private var recordTimer: Timer? = null
    private var recordStartTime = 0L
    private var videoFile: File? = null

    private var textureView: TextureView = TextureView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val maxZoom = characteristics?.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
            val sensorRect = characteristics?.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return false
            val currentZoom = zoomRect?.width()?.toFloat()?.div(sensorRect.width()) ?: 1f
            val newZoom = (currentZoom * detector.scaleFactor).coerceIn(1f, maxZoom)
            val w = (sensorRect.width() / newZoom).toInt()
            val h = (sensorRect.height() / newZoom).toInt()
            val x = (sensorRect.width() - w) / 2
            val y = (sensorRect.height() - h) / 2
            zoomRect = Rect(x, y, x + w, y + h)
            updatePreview()
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            focusOnPoint(e.x, e.y)
            return true
        }
    })

    init {
        container.addView(textureView)
        bgThread = HandlerThread("Camera2Bg").apply { start() }
        bgHandler = Handler(bgThread!!.looper)
        textureView.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(s: SurfaceTexture, w: Int, h: Int) { openCamera() }
            override fun onSurfaceTextureSizeChanged(s: SurfaceTexture, w: Int, h: Int) {}
            override fun onSurfaceTextureDestroyed(s: SurfaceTexture): Boolean = false
            override fun onSurfaceTextureUpdated(s: SurfaceTexture) {}
        }
    }

    private fun focusOnPoint(x: Float, y: Float) {
        val sensorRect = characteristics?.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
        val focusW = sensorRect.width() / 5
        val focusH = sensorRect.height() / 5
        val focusX = (x / textureView.width * sensorRect.width()).toInt() - focusW / 2
        val focusY = (y / textureView.height * sensorRect.height()).toInt() - focusH / 2
        val focusRect = Rect(focusX, focusY, focusX + focusW, focusY + focusH)

        val reqBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        reqBuilder?.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(android.hardware.camera2.params.MeteringRectangle(focusRect, 1000)))
        reqBuilder?.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(android.hardware.camera2.params.MeteringRectangle(focusRect, 1000)))
        reqBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        reqBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
        captureSession?.capture(reqBuilder!!.build(), null, bgHandler)
    }

    private fun updatePreview() {
        val reqBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        val surface = Surface(textureView.surfaceTexture)
        reqBuilder?.addTarget(surface)
        zoomRect?.let { reqBuilder?.set(CaptureRequest.SCALER_CROP_REGION, it) }
        reqBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        captureSession?.setRepeatingRequest(reqBuilder!!.build(), null, bgHandler)
    }

    @SuppressLint("MissingPermission")
    override fun doOpenCamera() {
        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId)
            setupPreviewSize()
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) { cameraDevice = camera; startPreview() }
                override fun onDisconnected(camera: CameraDevice) { camera.close() }
                override fun onError(camera: CameraDevice, p1: Int) { camera.close() }
            }, bgHandler)
        } catch (e: CameraAccessException) { e.printStackTrace() }
    }

    private fun setupPreviewSize() {
        val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return
        val targetRatio = when (currentAspectRatio) {
            AspectRatio.RATIO_1_1 -> 1.0
            AspectRatio.RATIO_3_4 -> 3.0 / 4.0
            AspectRatio.RATIO_16_9 -> 9.0 / 16.0
            AspectRatio.FULL_SCREEN -> {
                // 动态获取屏幕物理比例 (高/宽)
                val w = container.width
                val h = container.height
                if (w > 0 && h > 0) h.toDouble() / w.toDouble() else 9.0 / 16.0
            }
        }

        // 筛选比例最接近且面积最大的尺寸
        previewSize = map.getOutputSizes(SurfaceTexture::class.java)
            .filter { Math.abs(it.width.toDouble() / it.height.toDouble() - targetRatio) < 0.15 }
            .maxByOrNull { it.width * it.height }
            ?: map.getOutputSizes(SurfaceTexture::class.java)[0]
    }

    private fun startPreview() {
        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val surface = Surface(texture)
        val reqBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        reqBuilder?.addTarget(surface)
        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                updatePreview()
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, bgHandler)
    }

    override fun doCloseCamera() {
        captureSession?.close(); captureSession = null
        cameraDevice?.close(); cameraDevice = null
        mediaRecorder?.release(); mediaRecorder = null
    }

    override fun doSwitchCamera() {
        cameraId = if (cameraId == "0") "1" else "0"
        doCloseCamera(); doOpenCamera()
    }

    override fun doSetFlashMode(mode: FlashMode) { /* 同前文 */ }

    override fun doSetAspectRatio(ratio: AspectRatio) {
        setupPreviewSize()
        textureView.surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
        startPreview()
        updatePreviewViewSize(textureView)
    }

    override fun doTakePhoto(file: File, callback: CameraResultCallback) {
        val imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage() ?: return@setOnImageAvailableListener
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining()); buffer.get(bytes)
            try { file.writeBytes(bytes); callback.onSuccess(file) }
            catch (e: Exception) { callback.onError("Save failed") }
            image.close(); imageReader.close()
        }, bgHandler)

        val reqBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        reqBuilder?.addTarget(imageReader.surface)
        zoomRect?.let { reqBuilder?.set(CaptureRequest.SCALER_CROP_REGION, it) }
        captureSession?.capture(reqBuilder!!.build(), null, bgHandler)
    }

    override fun doStartRecording(file: File, maxDurationMillis: Long, callback: RecordCallback) {
        this.recordCallback = callback
        this.videoFile = file
        captureSession?.close(); captureSession = null
        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoEncodingBitRate(10000000); setVideoFrameRate(30)
            setVideoSize(previewSize.width, previewSize.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
        val texture = textureView.surfaceTexture!!
        texture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val previewSurface = Surface(texture)
        val recordSurface = mediaRecorder!!.surface
        val reqBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        reqBuilder?.addTarget(previewSurface); reqBuilder?.addTarget(recordSurface)
        zoomRect?.let { reqBuilder?.set(CaptureRequest.SCALER_CROP_REGION, it) }

        cameraDevice?.createCaptureSession(listOf(previewSurface, recordSurface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                session.setRepeatingRequest(reqBuilder!!.build(), null, bgHandler)
                mediaRecorder?.start()
                startRecordTimer(maxDurationMillis)
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, bgHandler)
    }

    private fun startRecordTimer(maxDurationMillis: Long) {
        recordStartTime = System.currentTimeMillis()
        recordTimer = Timer()
        recordTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val duration = System.currentTimeMillis() - recordStartTime
                recordCallback?.onProgress(duration)
                if (duration >= maxDurationMillis) stopRecording()
            }
        }, 0, 100)
    }

    override fun doStopRecording() {
        recordTimer?.cancel(); recordTimer = null
        try { mediaRecorder?.stop(); videoFile?.let { recordCallback?.onSuccess(it) } }
        catch (e: Exception) { recordCallback?.onError("Stop failed") }
        mediaRecorder?.reset(); mediaRecorder?.release(); mediaRecorder = null
        startPreview()
    }
}