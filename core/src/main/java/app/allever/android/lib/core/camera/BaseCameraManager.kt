package app.allever.android.lib.core.camera

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.io.File

/** 相机管理基类，负责状态拦截与防重入 */
abstract class BaseCameraManager(
    protected val context: Context,
    protected val container: ViewGroup
) : ICameraManager {

    @Volatile
    protected var state: CameraState = CameraState.IDLE
    protected var currentFlashMode: FlashMode = FlashMode.OFF
    protected var currentAspectRatio: AspectRatio = AspectRatio.RATIO_3_4

    /** 供子类调用的预览View尺寸更新方法 */
    protected fun updatePreviewViewSize(view: View) {
        view.post {
            val containerWidth = container.width
            val containerHeight = container.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val targetWidth: Int
            val targetHeight: Int

            when (currentAspectRatio) {
                AspectRatio.RATIO_1_1 -> {
                    targetWidth = containerWidth
                    targetHeight = containerWidth
                }
                AspectRatio.RATIO_3_4 -> {
                    targetWidth = containerWidth
                    targetHeight = containerWidth * 4 / 3
                }
                AspectRatio.RATIO_16_9 -> {
                    targetWidth = containerWidth
                    targetHeight = containerWidth * 16 / 9
                }
                AspectRatio.FULL_SCREEN -> {
                    // 全屏模式直接填满容器
                    targetWidth = containerWidth
                    targetHeight = containerHeight
                }
            }

            view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                width = targetWidth
                height = targetHeight
            }
        }
    }

    override fun openCamera() {
        if (state != CameraState.IDLE) return
        state = CameraState.OPENED
        doOpenCamera()
    }

    override fun closeCamera() {
        if (state == CameraState.IDLE) return
        if (state == CameraState.RECORDING) stopRecording()
        doCloseCamera()
        state = CameraState.IDLE
    }

    override fun switchCamera() {
        if (state != CameraState.OPENED) return
        doSwitchCamera()
    }

    override fun setFlashMode(mode: FlashMode) {
        currentFlashMode = mode
        if (state == CameraState.OPENED || state == CameraState.RECORDING) {
            doSetFlashMode(mode)
        }
    }

    override fun setAspectRatio(ratio: AspectRatio) {
        if (state != CameraState.OPENED) return
        currentAspectRatio = ratio
        doSetAspectRatio(ratio)
    }

    override fun takePhoto(file: File, callback: CameraResultCallback) {
        if (state != CameraState.OPENED) {
            callback.onError("Camera is not ready")
            return
        }
        state = CameraState.TAKING_PHOTO
        doTakePhoto(file, object : CameraResultCallback {
            override fun onSuccess(file: File) { state = CameraState.OPENED; callback.onSuccess(file) }
            override fun onError(message: String) { state = CameraState.OPENED; callback.onError(message) }
        })
    }

    override fun startRecording(file: File, maxDurationMillis: Long, callback: RecordCallback) {
        if (state != CameraState.OPENED) {
            callback.onError("Cannot start recording in current state: $state")
            return
        }
        state = CameraState.RECORDING
        doStartRecording(file, maxDurationMillis, callback)
    }

    override fun stopRecording() {
        if (state != CameraState.RECORDING) return
        doStopRecording()
        state = CameraState.OPENED
    }

    protected abstract fun doOpenCamera()
    protected abstract fun doCloseCamera()
    protected abstract fun doSwitchCamera()
    protected abstract fun doSetFlashMode(mode: FlashMode)
    protected abstract fun doSetAspectRatio(ratio: AspectRatio)
    protected abstract fun doTakePhoto(file: File, callback: CameraResultCallback)
    protected abstract fun doStartRecording(file: File, maxDurationMillis: Long, callback: RecordCallback)
    protected abstract fun doStopRecording()
}