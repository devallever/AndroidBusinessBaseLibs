package app.allever.android.lib.camera.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import java.lang.ref.WeakReference

abstract class BaseCameraEngine : ICameraEngine {
    protected var previewRef: WeakReference<View>? = null
    protected var mConfig: CameraConfig = CameraConfig.Builder().build()
    protected var currentState: CameraState = CameraState.IDLE
    protected var currentFacing: CameraFacing = CameraFacing.FACE_BACK
    protected var isPreviewing = false
    protected var isCapturing = false
    protected var isRecording = false

    private var cameraThread: HandlerThread? = null
    protected var cameraHandler: Handler? = null

    protected fun startCameraThread() {
        cameraThread = HandlerThread("CameraThread").apply {
            start()
        }
        cameraThread?.looper?.let {
            cameraHandler = Handler(it)
        }
    }

    protected fun stopCameraThread() {
        cameraThread?.quitSafely()
        try {
            cameraThread?.join()
        } catch (e: InterruptedException) {
        }
        cameraThread = null
        cameraHandler = null
    }

    protected fun postCameraTask(runnable: Runnable) {
        cameraHandler?.post(runnable) ?: runnable.run()
    }

    override fun bindPreview(view: View) {
        previewRef = WeakReference(view)
    }

    override fun setConfig(config: CameraConfig) {
        this.mConfig = config
        this.currentFacing = config.cameraFacing
    }

    override fun getState(): CameraState {
        return currentState
    }

    protected fun updateState(state: CameraState) {
        currentState = state
    }

    protected fun checkCameraPermission(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    protected fun checkAudioPermission(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    protected fun getPreviewView(): View? {
        return previewRef?.get()
    }

    protected fun isPreviewViewValid(): Boolean {
        val view = previewRef?.get()
        return view != null && view.isAttachedToWindow
    }

    protected fun resetState() {
        isPreviewing = false
        isCapturing = false
        isRecording = false
        currentState = CameraState.IDLE
    }
}