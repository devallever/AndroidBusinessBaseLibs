package app.allever.android.lib.camera.proxy.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import app.allever.android.lib.core.camera.AspectRatio
import app.allever.android.lib.core.camera.BaseCameraManager
import app.allever.android.lib.core.camera.CameraResultCallback
import app.allever.android.lib.core.camera.FlashMode
import app.allever.android.lib.core.camera.RecordCallback
import app.allever.android.lib.core.camera.VideoQuality
import java.io.File
import java.util.concurrent.Executor

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
    private var camera: androidx.camera.core.Camera? = null

    private var previewView: PreviewView = PreviewView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        scaleType = PreviewView.ScaleType.FILL_CENTER
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
            val newScale = (scale * detector.scaleFactor).coerceIn(1f, camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f)
            camera?.cameraControl?.setZoomRatio(newScale)
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(e.x, e.y)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE).build()
            camera?.cameraControl?.startFocusAndMetering(action)
            return true
        }
    })

    init {
        container.addView(previewView)
        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        // CameraX 比例设置 (全屏和16:9均使用16:9以获取最大视野，由UI层负责裁剪填满)
        val aspectRatio = when (currentAspectRatio) {
            AspectRatio.RATIO_1_1, AspectRatio.RATIO_3_4 -> androidx.camera.core.AspectRatio.RATIO_4_3
            AspectRatio.RATIO_16_9, AspectRatio.FULL_SCREEN -> androidx.camera.core.AspectRatio.RATIO_16_9
        }

        val preview = Preview.Builder().setTargetAspectRatio(aspectRatio).build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().setTargetAspectRatio(aspectRatio).build()

        // 录像质量选择
        val quality = when (currentVideoQuality) {
            VideoQuality.SD_480P -> Quality.SD
            VideoQuality.HD_720P -> Quality.HD
            VideoQuality.FHD_1080P -> Quality.FHD
            VideoQuality.UHD_4K -> Quality.UHD
        }
        val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(quality)).build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
            doSetFlashMode(currentFlashMode)
            updatePreviewViewSize(previewView)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun doOpenCamera() {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({ cameraProvider = future.get(); bindUseCases() }, cameraExecutor)
    }

    override fun doCloseCamera() {
        currentRecording?.stop(); currentRecording = null
        cameraProvider?.unbindAll()
    }

    override fun doSwitchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        bindUseCases()
    }

    override fun doSetFlashMode(mode: FlashMode) {
        imageCapture?.flashMode = when (mode) {
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        camera?.cameraControl?.enableTorch(mode == FlashMode.TORCH)
    }

    override fun doSetAspectRatio(ratio: AspectRatio) { bindUseCases() }
    override fun doSetVideoQuality(quality: VideoQuality) { bindUseCases() }

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
                    if (millis >= maxDurationMillis && maxDurationMillis > 0) stopRecording()
                } else if (event is VideoRecordEvent.Finalize) {
                    if (event.hasError()) callback.onError("Record failed") else callback.onSuccess(file)
                }
            }
    }

    override fun doStopRecording() {
        currentRecording?.stop()
        currentRecording = null
    }
}