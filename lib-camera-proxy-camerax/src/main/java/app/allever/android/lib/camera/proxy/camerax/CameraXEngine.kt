package app.allever.android.lib.camera.proxy.camerax

import android.annotation.SuppressLint
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
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
import app.allever.android.lib.camera.core.BaseCameraEngine
import app.allever.android.lib.camera.core.CameraConfig
import app.allever.android.lib.camera.core.CameraFacing
import app.allever.android.lib.camera.core.CameraState
import app.allever.android.lib.camera.core.FlashMode
import app.allever.android.lib.camera.core.PhotoResultCallback
import app.allever.android.lib.camera.core.VideoResultCallback
import app.allever.android.lib.camera.core.VideoQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class CameraXEngine : BaseCameraEngine() {

    companion object {
        private const val TAG = "CameraXEngine"
    }

    private var mLifecycleOwner: LifecycleOwner? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private var mCameraSelector: CameraSelector? = null
    private var mPreview: Preview? = null
    private var mImageCapture: ImageCapture? = null
    private var mVideoCapture: VideoCapture<Recorder>? = null
    private var mCurrentRecording: Recording? = null

    private var mCameraExecutor: Executor? = null

    override fun bindPreview(view: View) {
        super.bindPreview(view)
        mCameraExecutor = ContextCompat.getMainExecutor(view.context)
        if (mLifecycleOwner == null) {
            mLifecycleOwner = view.context as? LifecycleOwner
        }
    }

    override fun setContext(context: android.content.Context) {
        super.setContext(context)
        mCameraExecutor = ContextCompat.getMainExecutor(context)
        if (mLifecycleOwner == null) {
            mLifecycleOwner = context as? LifecycleOwner
        }
    }

    override fun openCamera(cameraFacing: CameraFacing) {
        currentFacing = cameraFacing

        launchCameraTask {
            closeCameraInternal()

            val context = getContext() ?: return@launchCameraTask
            val lifecycleOwner = mLifecycleOwner ?: return@launchCameraTask

            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    mCameraProvider = cameraProviderFuture.get()
                    bindUseCases(lifecycleOwner)
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                updateState(CameraState.IDLE)
            }
        }
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner) {
        val provider = mCameraProvider ?: return
        provider.unbindAll()

        mCameraSelector = if (currentFacing == CameraFacing.FACE_BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val aspectRatio = AspectRatio.RATIO_16_9
        val useCases = mutableListOf<UseCase>()

        // Preview 仅在有 PreviewView 时创建
        val previewView = getPreviewView() as? PreviewView
        if (previewView != null) {
            mPreview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            useCases.add(mPreview!!)
        }

        mImageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(convertFlashMode(mConfig.flashMode))
            .build()
        useCases.add(mImageCapture!!)

        val quality = when (mConfig.videoQuality) {
            VideoQuality.SD_480P -> Quality.SD
            VideoQuality.HD_720P -> Quality.HD
            VideoQuality.FHD_1080P -> Quality.FHD
            VideoQuality.UHD_4K -> Quality.UHD
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(quality))
            .build()
        mVideoCapture = VideoCapture.withOutput(recorder)
        useCases.add(mVideoCapture!!)

        try {
            provider.bindToLifecycle(lifecycleOwner, mCameraSelector!!, *useCases.toTypedArray())
            isPreviewing = true
            updateState(CameraState.OPENED)
        } catch (e: Exception) {
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    override fun closeCamera() {
        closeCameraInternal()
        updateState(CameraState.IDLE)
    }

    private fun closeCameraInternal() {
        try { mCameraProvider?.unbindAll() } catch (e: Exception) {}
        isPreviewing = false
    }

    override fun takePicture(callback: PhotoResultCallback?) {
        if (!isPreviewing || mImageCapture == null || isCapturing) {
            callback?.onFailure("Camera not ready")
            return
        }

        isCapturing = true
        updateState(CameraState.TAKING_PHOTO)

        val photoFile = createPhotoFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        mImageCapture?.takePicture(outputOptions, mCameraExecutor!!, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                launchCameraTask {
                    try {
                        scanPhotoToGallery(photoFile)
                        withContext(Dispatchers.Main) {
                            callback?.onSuccess(photoFile)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            callback?.onFailure("Failed to save photo: ${e.message}")
                        }
                    } finally {
                        isCapturing = false
                        updateState(CameraState.OPENED)
                    }
                }
            }

            override fun onError(exc: ImageCaptureException) {
                isCapturing = false
                updateState(CameraState.OPENED)
                callback?.onFailure(exc.message ?: "Capture failed")
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun startRecordVideo(callback: VideoResultCallback?) {
        if (!isPreviewing || mVideoCapture == null || isRecording) {
            callback?.onFailure("Camera not ready")
            return
        }

        val context = getContext() ?: return
        videoCallback = callback
        currentVideoFile = createVideoFile()

        try {
            val options = FileOutputOptions.Builder(currentVideoFile!!).build()
            mCurrentRecording = mVideoCapture?.output
                ?.prepareRecording(context, options)
                ?.withAudioEnabled()
                ?.start(mCameraExecutor!!) { event ->
                    when (event) {
                        is VideoRecordEvent.Status -> {
                            val millis = event.recordingStats.recordedDurationNanos / 1_000_000
                            if (mConfig.maxVideoDuration > 0 && millis >= mConfig.maxVideoDuration) {
                                stopRecordVideo()
                            }
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (event.hasError()) {
                                videoCallback?.onFailure("Record failed")
                            } else {
                                currentVideoFile?.let { file ->
                                    scanVideoToGallery(file)
                                    videoCallback?.onSuccess(file)
                                }
                            }
                            isRecording = false
                            updateState(CameraState.OPENED)
                            mCurrentRecording = null
                            videoCallback = null
                            currentVideoFile = null
                        }
                    }
                }

            isRecording = true
            updateState(CameraState.RECORDING)
        } catch (e: Exception) {
            callback?.onFailure("Failed to start recording: ${e.message}")
            mCurrentRecording = null
            videoCallback = null
            currentVideoFile = null
        }
    }

    override fun stopRecordVideo() {
        if (!isRecording || mCurrentRecording == null) return

        try {
            mCurrentRecording?.stop()
            mCurrentRecording = null
        } catch (e: Exception) {
            videoCallback?.onFailure("Failed to stop recording: ${e.message}")
            isRecording = false
            updateState(CameraState.OPENED)
            videoCallback = null
            currentVideoFile = null
        }
    }

    override fun release() {
        stopRecordVideo()
        closeCamera()
        mCameraProvider = null
        mPreview = null
        mImageCapture = null
        mVideoCapture = null
        mCurrentRecording = null
        mLifecycleOwner = null
        cancelCameraScope()
        resetState()
    }

    override fun setFlashMode(mode: FlashMode) {
        mConfig = CameraConfig.Builder()
            .setCameraFacing(currentFacing)
            .setFlashMode(mode)
            .build()
        mImageCapture?.flashMode = convertFlashMode(mode)
    }

    private fun convertFlashMode(flashMode: FlashMode): Int {
        return when (flashMode) {
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.TORCH -> ImageCapture.FLASH_MODE_ON
        }
    }
}
