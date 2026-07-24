package app.allever.android.lib.camera.core

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

abstract class BaseCameraEngine : ICameraEngine {
    protected var previewRef: WeakReference<View>? = null
    protected var mConfig: CameraConfig = CameraConfig.Builder().build()
    protected var currentState: CameraState = CameraState.IDLE
    protected var currentFacing: CameraFacing = CameraFacing.FACE_BACK
    protected var isPreviewing = false
    protected var isCapturing = false
    protected var isRecording = false

    protected var cameraScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    protected fun launchCameraTask(block: suspend () -> Unit) {
        if (!cameraScope.coroutineContext.isActive) {
            cameraScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        }
        cameraScope.launch {
            block()
        }
    }

    protected suspend fun runOnCameraThread(block: () -> Unit) {
        withContext(Dispatchers.IO) {
            block()
        }
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

    protected fun cancelCameraScope() {
        cameraScope.cancel()
    }
}