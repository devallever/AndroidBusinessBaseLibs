package app.allever.android.lib.camera.core

import android.content.Context
import android.view.View

interface ICameraEngine {
    fun bindPreview(view: View)
    fun openCamera(cameraFacing: CameraFacing)
    fun closeCamera()
    fun switchCamera()
    fun takePicture(callback: PhotoResultCallback?)
    fun startRecordVideo(callback: VideoResultCallback?)
    fun stopRecordVideo()
    fun release()
    fun setConfig(config: CameraConfig)
    fun getState(): CameraState

    /** 设置 Context，用于无预览场景。默认空实现，子类按需覆盖。 */
    fun setContext(context: Context) {}

    /** 运行时切换闪光灯模式，无需重新打开相机。默认空实现，子类按需覆盖。 */
    fun setFlashMode(mode: FlashMode) {}

    /** 设置变焦比例，0f~1f。默认空实现，子类按需覆盖。 */
    fun setZoom(zoom: Float) {}
}
