package app.allever.android.lib.core.camera

import android.content.Context
import android.hardware.Camera
import android.media.MediaRecorder
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import java.io.File

class Camera1Manager(context: Context, container: ViewGroup) : BaseCameraManager(context, container) {
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private var surfaceView: SurfaceView = SurfaceView(context).apply {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    init {
        container.addView(surfaceView)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) { openCamera() }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) { closeCamera() }
        })
    }

    override fun doOpenCamera() {
        try {
            camera = Camera.open(currentCameraId)
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(surfaceView.holder)
            camera?.startPreview()
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun doCloseCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun doSwitchCamera() {
        currentCameraId = if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK
        doCloseCamera()
        doOpenCamera()
    }

    override fun doTakePhoto(file: File, callback: CameraResultCallback) {
        camera?.takePicture(null, null, { data, _ ->
            try { file.writeBytes(data); callback.onSuccess(file) }
            catch (e: Exception) { callback.onError(e.message ?: "Save failed") }
        })
    }

    override fun doStartRecording(file: File, callback: CameraResultCallback) {
        camera?.unlock()
        mediaRecorder = MediaRecorder().apply {
            setCamera(camera)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOrientationHint(90)
            setPreviewDisplay(surfaceView.holder.surface)
            try { prepare(); start() }
            catch (e: Exception) { callback.onError(e.message ?: "Record failed"); camera?.lock() }
        }
    }

    override fun doStopRecording() {
        try { mediaRecorder?.stop() } catch (e: Exception) { e.printStackTrace() }
        mediaRecorder?.reset(); mediaRecorder?.release(); mediaRecorder = null
        camera?.lock(); camera?.startPreview()
    }
}