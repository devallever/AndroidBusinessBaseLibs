package app.allever.android.lib.core.camera

import android.content.Context
import android.view.ViewGroup
import java.io.File

/** 相机管理基类，负责状态拦截与防重入 */
abstract class BaseCameraManager(
    protected val context: Context,
    protected val container: ViewGroup
) : ICameraManager {

    @Volatile
    protected var state: CameraState = CameraState.IDLE

    override fun openCamera() {
        if (state != CameraState.IDLE) return // 防止重复打开
        state = CameraState.OPENED
        doOpenCamera()
    }

    override fun closeCamera() {
        if (state == CameraState.IDLE) return
        if (state == CameraState.RECORDING) {
            stopRecording() // 关闭前先停止录像
        }
        doCloseCamera()
        state = CameraState.IDLE
    }

    override fun switchCamera() {
        if (state != CameraState.OPENED) return // 只有在预览状态才允许切换
        doSwitchCamera()
    }

    override fun takePhoto(file: File, callback: CameraResultCallback) {
        if (state != CameraState.OPENED) {
            callback.onError("Camera is not ready or busy")
            return
        }
        state = CameraState.TAKING_PHOTO
        doTakePhoto(file, object : CameraResultCallback {
            override fun onSuccess(file: File) {
                state = CameraState.OPENED // 恢复预览状态
                callback.onSuccess(file)
            }
            override fun onError(message: String) {
                state = CameraState.OPENED // 恢复预览状态
                callback.onError(message)
            }
        })
    }

    override fun startRecording(file: File, callback: CameraResultCallback) {
        if (state != CameraState.OPENED) {
            callback.onError("Cannot start recording in current state: $state")
            return
        }
        state = CameraState.RECORDING
        doStartRecording(file, callback)
    }

    override fun stopRecording() {
        if (state != CameraState.RECORDING) return
        doStopRecording()
        state = CameraState.OPENED // 恢复预览状态
    }

    // 交给子类具体实现的模板方法
    protected abstract fun doOpenCamera()
    protected abstract fun doCloseCamera()
    protected abstract fun doSwitchCamera()
    protected abstract fun doTakePhoto(file: File, callback: CameraResultCallback)
    protected abstract fun doStartRecording(file: File, callback: CameraResultCallback)
    protected abstract fun doStopRecording()
}