package app.allever.android.lib.camera.proxy.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
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
import app.allever.android.lib.camera.core.CameraFacing
import app.allever.android.lib.camera.core.CameraState
import app.allever.android.lib.camera.core.FlashMode
import app.allever.android.lib.camera.core.ResultCallback
import app.allever.android.lib.camera.core.VideoQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraXEngine : BaseCameraEngine() {
    private val TAG = "CameraXEngine"

    private var mContext: Context? = null
    private var mLifecycleOwner: LifecycleOwner? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private var mCameraSelector: CameraSelector? = null
    private var mPreview: Preview? = null
    private var mImageCapture: ImageCapture? = null
    private var mVideoCapture: VideoCapture<Recorder>? = null
    private var mCurrentRecording: Recording? = null

    private var mCameraExecutor: Executor? = null
    private var mVideoCallback: ResultCallback? = null
    private var mCurrentVideoFile: File? = null

    override fun bindPreview(view: View) {
        super.bindPreview(view)
        mContext = view.context
        mLifecycleOwner = view.context as? LifecycleOwner
        mCameraExecutor = ContextCompat.getMainExecutor(view.context)
    }

    override fun openCamera(cameraFacing: CameraFacing) {
        currentFacing = cameraFacing

        launchCameraTask {
            closeCameraInternal()

            val context = mContext ?: return@launchCameraTask
            val lifecycleOwner = mLifecycleOwner ?: return@launchCameraTask

            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    mCameraProvider = cameraProviderFuture.get()
                    bindUseCases(lifecycleOwner)
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open camera", e)
                updateState(CameraState.IDLE)
            }
        }
    }

    fun openCamera() {
        openCamera(mConfig.cameraFacing)
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner) {
        val provider = mCameraProvider ?: return
        provider.unbindAll()

        mCameraSelector = if (currentFacing == CameraFacing.FACE_BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val previewView = getPreviewView() as? PreviewView ?: return

        val aspectRatio = AspectRatio.RATIO_16_9

        mPreview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        mImageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(convertFlashMode(mConfig.flashMode))
            .build()

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

        try {
            provider.bindToLifecycle(
                lifecycleOwner,
                mCameraSelector!!,
                mPreview,
                mImageCapture,
                mVideoCapture
            )

            isPreviewing = true
            updateState(CameraState.OPENED)
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            isPreviewing = false
            updateState(CameraState.IDLE)
        }
    }

    override fun closeCamera() {
        launchCameraTask {
            closeCameraInternal()
            updateState(CameraState.IDLE)
        }
    }

    private fun closeCameraInternal() {
        try {
            mCameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close camera", e)
        }
        isPreviewing = false
    }

    override fun switchCamera() {
        val newFacing = if (currentFacing == CameraFacing.FACE_BACK) CameraFacing.FACE_FRONT else CameraFacing.FACE_BACK
        openCamera(newFacing)
    }

    override fun takePicture(resultCallback: ResultCallback?) {
        if (!isPreviewing || mImageCapture == null || isCapturing) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        isCapturing = true
        updateState(CameraState.TAKING_PHOTO)

        val context = mContext ?: return

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        val fileName = "IMG_$timestamp.jpg"

        val photoFile = File(mConfig.photoSavePath, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        mImageCapture?.takePicture(
            outputOptions,
            mCameraExecutor!!,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    launchCameraTask {
                        try {
                            if (currentFacing == CameraFacing.FACE_FRONT) {
                                rotatePhotoFile(photoFile, 180)
                            }
                            scanPhotoToGallery(photoFile)
                            withContext(Dispatchers.Main) {
                                resultCallback?.onSuccess(photoFile)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                resultCallback?.onFailure("Failed to save photo: ${e.message}")
                            }
                        } finally {
                            isCapturing = false
                            updateState(CameraState.OPENED)
                        }
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exc)
                    isCapturing = false
                    updateState(CameraState.OPENED)
                    resultCallback?.onFailure(exc.message ?: "Capture failed")
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    override fun startRecordVideo(resultCallback: ResultCallback?) {
        if (!isPreviewing || mVideoCapture == null || isRecording) {
            resultCallback?.onFailure("Camera not ready")
            return
        }

        val context = mContext ?: return

        mVideoCallback = resultCallback
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        val fileName = "VID_$timestamp.mp4"

        val videoFile = File(mConfig.videoSavePath, fileName)
        mCurrentVideoFile = videoFile

        try {
            val options = FileOutputOptions.Builder(videoFile).build()
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
                                mVideoCallback?.onFailure("Record failed")
                            } else {
                                mCurrentVideoFile?.let { file ->
                                    scanVideoToGallery(file)
                                    mVideoCallback?.onSuccess(file)
                                }
                            }
                            isRecording = false
                            updateState(CameraState.OPENED)
                            mCurrentRecording = null
                            mVideoCallback = null
                            mCurrentVideoFile = null
                        }
                    }
                }

            isRecording = true
            updateState(CameraState.RECORDING)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            resultCallback?.onFailure("Failed to start recording: ${e.message}")
            mCurrentRecording = null
            mVideoCallback = null
            mCurrentVideoFile = null
        }
    }

    override fun stopRecordVideo() {
        if (!isRecording || mCurrentRecording == null) {
            return
        }

        try {
            mCurrentRecording?.stop()
            mCurrentRecording = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            mVideoCallback?.onFailure("Failed to stop recording: ${e.message}")
            isRecording = false
            updateState(CameraState.OPENED)
            mVideoCallback = null
            mCurrentVideoFile = null
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
        mVideoCallback = null
        mCurrentVideoFile = null
        mContext = null
        mLifecycleOwner = null
        cancelCameraScope()
        resetState()
    }

    private fun convertFlashMode(flashMode: FlashMode): Int {
        return when (flashMode) {
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.TORCH -> ImageCapture.FLASH_MODE_ON
        }
    }

    private fun rotatePhotoFile(file: File, degree: Int): File {
        if (degree % 360 == 0) {
            return file
        }

        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degree.toFloat())

        val rotatedBitmap = android.graphics.Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        bitmap.recycle()

        FileOutputStream(file).use { fos ->
            rotatedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
        }

        rotatedBitmap.recycle()

        return file
    }

    private fun scanPhotoToGallery(file: File) {
        val context = mContext ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    private fun scanVideoToGallery(file: File) {
        val context = mContext ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
        }
    }
}