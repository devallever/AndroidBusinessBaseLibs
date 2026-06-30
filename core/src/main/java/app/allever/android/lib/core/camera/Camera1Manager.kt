package app.allever.android.lib.core.camera

import android.content.Context
import android.hardware.Camera
import android.media.MediaRecorder
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import java.io.File
import java.util.Timer
import java.util.TimerTask
class Camera1Manager(context: Context, container: ViewGroup) : BaseCameraManager(context, container) {
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private var surfaceView: SurfaceView = SurfaceView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    private var recordCallback: RecordCallback? = null
    private var recordTimer: Timer? = null
    private var recordStartTime = 0L
    private var videoFile: File? = null
    private var maxZoom = 0

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val params = camera?.parameters
            maxZoom = params?.maxZoom ?: 0
            val currentZoom = params?.zoom ?: 0
            val newZoom = (currentZoom + (detector.scaleFactor - 1) * 10).toInt().coerceIn(0, maxZoom)
            params?.zoom = newZoom
            camera?.parameters = params
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            camera?.setOneShotPreviewCallback(null)
            val focusRect = calculateTapArea(e.x, e.y, 1f)
            val meteringRect = calculateTapArea(e.x, e.y, 1.5f)
            val params = camera?.parameters
            params?.focusAreas = listOf(Camera.Area(focusRect.rect, 1000))
            params?.meteringAreas = listOf(meteringRect, 1000) as List<Camera.Area?>?
            params?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            camera?.parameters = params
            camera?.autoFocus { success, _ -> }
            return true
        }
    })

    init {
        container.addView(surfaceView)
        surfaceView.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) { openCamera() }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) { closeCamera() }
        })
    }

    private fun calculateTapArea(x: Float, y: Float, coefficient: Float): Camera.Area {
        val focusAreaSize = 100
        val centerX = (x / surfaceView.width * 2000 - 1000).toInt()
        val centerY = (y / surfaceView.height * 2000 - 1000).toInt()
        val left = (centerX - focusAreaSize * coefficient).toInt().coerceIn(-1000, 1000)
        val top = (centerY - focusAreaSize * coefficient).toInt().coerceIn(-1000, 1000)
        val right = (centerX + focusAreaSize * coefficient).toInt().coerceIn(-1000, 1000)
        val bottom = (centerY + focusAreaSize * coefficient).toInt().coerceIn(-1000, 1000)
        return Camera.Area(android.graphics.Rect(left, top, right, bottom), 1000)
    }

    override fun doOpenCamera() {
        try {
            camera = Camera.open(currentCameraId)
            setupCameraParams()
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(surfaceView.holder)
            camera?.startPreview()
            updatePreviewViewSize(surfaceView)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun setupCameraParams() {
        val params = camera?.parameters ?: return
        val targetRatio = when (currentAspectRatio) {
            AspectRatio.RATIO_1_1 -> 1.0
            AspectRatio.RATIO_3_4 -> 3.0 / 4.0
            AspectRatio.RATIO_16_9 -> 9.0 / 16.0
            AspectRatio.FULL_SCREEN -> {
                val w = container.width
                val h = container.height
                if (w > 0 && h > 0) h.toDouble() / w.toDouble() else 9.0 / 16.0
            }
        }

        // 筛选比例最接近且面积最大的尺寸
        val optimalSize = params.supportedPreviewSizes
            .filter { Math.abs(it.width.toDouble() / it.height.toDouble() - targetRatio) < 0.15 }
            .maxByOrNull { it.width * it.height }
            ?: params.supportedPreviewSizes[0]

        params.setPreviewSize(optimalSize.width, optimalSize.height)

        val optimalPictureSize = params.supportedPictureSizes
            .filter { Math.abs(it.width.toDouble() / it.height.toDouble() - targetRatio) < 0.15 }
            .maxByOrNull { it.width * it.height }
            ?: params.supportedPictureSizes[0]
        params.setPictureSize(optimalPictureSize.width, optimalPictureSize.height)

        params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        camera?.parameters = params
    }

    override fun doCloseCamera() {
        camera?.stopPreview(); camera?.release(); camera = null
    }

    override fun doSwitchCamera() {
        currentCameraId = if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK
        doCloseCamera(); doOpenCamera()
    }

    override fun doSetFlashMode(mode: FlashMode) {
        val params = camera?.parameters
        params?.flashMode = when (mode) {
            FlashMode.ON -> Camera.Parameters.FLASH_MODE_ON
            FlashMode.AUTO -> Camera.Parameters.FLASH_MODE_AUTO
            FlashMode.TORCH -> Camera.Parameters.FLASH_MODE_TORCH
            else -> Camera.Parameters.FLASH_MODE_OFF
        }
        camera?.parameters = params
    }

    override fun doSetAspectRatio(ratio: AspectRatio) {
        camera?.stopPreview()
        setupCameraParams()
        camera?.startPreview()
        updatePreviewViewSize(surfaceView)
    }

    override fun doSetVideoQuality(quality: VideoQuality) {

    }

    override fun doTakePhoto(file: File, callback: CameraResultCallback) {
        camera?.takePicture(null, null, { data, _ ->
            try { file.writeBytes(data); callback.onSuccess(file) }
            catch (e: Exception) { callback.onError(e.message ?: "Save failed") }
        })
    }

    override fun doStartRecording(file: File, maxDurationMillis: Long, callback: RecordCallback) {
        this.recordCallback = callback
        this.videoFile = file
        camera?.unlock()
        val size = camera?.parameters?.previewSize
        mediaRecorder = MediaRecorder().apply {
            setCamera(camera)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoSize(size?.width ?: 1080, size?.height ?: 1920)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOrientationHint(90)
            setPreviewDisplay(surfaceView.holder.surface)
            try {
                prepare(); start()
                startRecordTimer(maxDurationMillis)
            } catch (e: Exception) {
                callback.onError(e.message ?: "Record failed"); camera?.lock()
            }
        }
    }

    private fun startRecordTimer(maxDurationMillis: Long) {
        recordStartTime = System.currentTimeMillis()
        recordTimer = Timer()
        recordTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val duration = System.currentTimeMillis() - recordStartTime
                recordCallback?.onProgress(duration)
                if (duration >= maxDurationMillis && maxDurationMillis > 0) stopRecording()
            }
        }, 0, 100)
    }

    override fun doStopRecording() {
        recordTimer?.cancel(); recordTimer = null
        try { mediaRecorder?.stop(); videoFile?.let { recordCallback?.onSuccess(it) } }
        catch (e: Exception) { recordCallback?.onError("Stop failed") }
        mediaRecorder?.reset(); mediaRecorder?.release(); mediaRecorder = null
        camera?.lock(); camera?.startPreview()
    }
}