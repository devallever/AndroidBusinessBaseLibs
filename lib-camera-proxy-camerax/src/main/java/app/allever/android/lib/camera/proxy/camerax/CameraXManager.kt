package app.allever.android.lib.camera.proxy.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import app.allever.android.lib.core.camera.BaseCameraManager
import app.allever.android.lib.core.camera.CameraResultCallback
import app.allever.android.lib.core.camera.FlashMode
import app.allever.android.lib.core.camera.ICameraManager
import app.allever.android.lib.core.camera.RecordCallback
import java.io.File
import java.util.concurrent.Executor

@SuppressLint("ClickableViewAccessibility")
class CameraXManager(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    container: ViewGroup
) : BaseCameraManager(context, container) {

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val cameraExecutor: Executor = ContextCompat.getMainExecutor(context)
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    private var previewView: PreviewView = PreviewView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    // 手势检测器
    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                val newScale = (scale * detector.scaleFactor).coerceIn(
                    1f,
                    camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
                )
                camera?.cameraControl?.setZoomRatio(newScale)
                return true
            }
        })

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val factory = previewView.meteringPointFactory
                val point = factory.createPoint(e.x, e.y)
                val action = FocusMeteringAction.Builder(
                    point,
                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                ).build()
                camera?.cameraControl?.startFocusAndMetering(action)
                return true
            }
        })

    init {
        container.addView(previewView)
        // 绑定触摸事件
        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        imageCapture = ImageCapture.Builder().build()
        val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
            doSetFlashMode(currentFlashMode) // 绑定后应用闪光灯状态
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun doOpenCamera() {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()
            bindUseCases()
        }, cameraExecutor)
    }

    override fun doCloseCamera() {
        currentRecording?.stop()
        currentRecording = null
        cameraProvider?.unbindAll()
    }

    override fun doSwitchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        bindUseCases()
    }

    override fun doSetFlashMode(mode: FlashMode) {
        val flashMode = when (mode) {
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode

        // TORCH 模式用于录像常亮
        camera?.cameraControl?.enableTorch(mode == FlashMode.TORCH)
    }

    override fun doTakePhoto(file: File, callback: CameraResultCallback) {
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(options, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) { callback.onSuccess(file) }
            override fun onError(exc: ImageCaptureException) { callback.onError(exc.message ?: "Capture failed") }
        })
    }

    @SuppressLint("MissingPermission")
    override fun doStartRecording(file: File, maxDurationMillis: Long, callback: RecordCallback) {
        val options = FileOutputOptions.Builder(file).build()
        currentRecording = videoCapture?.output
            ?.prepareRecording(context, options)
            ?.withAudioEnabled()
            ?.start(cameraExecutor) { event ->
                if (event is VideoRecordEvent.Status) {
                    val millis = event.recordingStats.recordedDurationNanos / 1_000_000
                    callback.onProgress(millis)
                    if (millis >= maxDurationMillis && maxDurationMillis > 0) {
                        stopRecording()
                    }
                } else if (event is VideoRecordEvent.Finalize) {
                    if (event.hasError()) callback.onError("Record failed")
                    else callback.onSuccess(file)
                }
            }
    }

    override fun doStopRecording() {
        currentRecording?.stop()
        currentRecording = null
    }
}