package app.allever.android.lib.camera.proxy.camera2
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import app.allever.android.lib.core.camera.BaseCameraManager
import app.allever.android.lib.core.camera.CameraResultCallback
import app.allever.android.lib.core.camera.ICameraManager
import java.io.File

class Camera2Manager(context: Context, container: ViewGroup) : BaseCameraManager(context, container) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var mediaRecorder: MediaRecorder? = null
    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null
    private var cameraId = "0"
    private val previewSize = Size(1920, 1080)
    private var textureView: TextureView = TextureView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    init {
        container.addView(textureView)
        bgThread = HandlerThread("Camera2Bg").apply { start() }
        bgHandler = Handler(bgThread!!.looper)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(s: android.graphics.SurfaceTexture, w: Int, h: Int) { openCamera() }
            override fun onSurfaceTextureSizeChanged(s: android.graphics.SurfaceTexture, w: Int, h: Int) {}
            override fun onSurfaceTextureDestroyed(s: android.graphics.SurfaceTexture): Boolean = false
            override fun onSurfaceTextureUpdated(s: android.graphics.SurfaceTexture) {}
        }
    }

    @SuppressLint("MissingPermission")
    override fun doOpenCamera() {
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startPreview()
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close() }
                override fun onError(camera: CameraDevice, p1: Int) { camera.close() }
            }, bgHandler)
        } catch (e: CameraAccessException) { e.printStackTrace() }
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
                reqBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                session.setRepeatingRequest(reqBuilder!!.build(), null, bgHandler)
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
        captureSession?.capture(reqBuilder!!.build(), null, bgHandler)
    }

    override fun doStartRecording(file: File, callback: CameraResultCallback) {
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

        cameraDevice?.createCaptureSession(listOf(previewSurface, recordSurface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                reqBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
                session.setRepeatingRequest(reqBuilder!!.build(), null, bgHandler)
                mediaRecorder?.start()
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, bgHandler)
    }

    override fun doStopRecording() {
        try { mediaRecorder?.stop() } catch (e: Exception) { e.printStackTrace() }
        mediaRecorder?.reset(); mediaRecorder?.release(); mediaRecorder = null
        startPreview()
    }
}