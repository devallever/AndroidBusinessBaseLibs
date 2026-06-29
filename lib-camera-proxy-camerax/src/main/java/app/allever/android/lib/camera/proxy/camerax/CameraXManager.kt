package app.allever.android.lib.camera.proxy.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import app.allever.android.lib.core.camera.ICameraManager
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
    private var previewView: PreviewView = PreviewView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    init {
        container.addView(previewView)
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        imageCapture = ImageCapture.Builder().build()
        val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
        videoCapture = VideoCapture.withOutput(recorder)
        try {
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture)
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

    override fun doTakePhoto(file: File, callback: CameraResultCallback) {
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(options, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) { callback.onSuccess(file) }
            override fun onError(exc: ImageCaptureException) { callback.onError(exc.message ?: "Capture failed") }
        })
    }

    @SuppressLint("MissingPermission")
    override fun doStartRecording(file: File, callback: CameraResultCallback) {
        val options = FileOutputOptions.Builder(file).build()
        currentRecording = videoCapture?.output
            ?.prepareRecording(context, options)
            ?.withAudioEnabled()
            ?.start(cameraExecutor) { event ->
                if (event is VideoRecordEvent.Finalize) {
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