package app.allever.android.lib.core.camera

import java.io.File

/** 统一相机管理接口 */
interface ICameraManager {
    /** 打开相机并绑定预览 */
    fun openCamera()

    /** 关闭相机并释放资源 */
    fun closeCamera()

    /** 切换前后置摄像头 */
    fun switchCamera()

    /** 设置闪光灯模式 */
    fun setFlashMode(mode: FlashMode)

    /** 设置预览比例 */
    fun setAspectRatio(ratio: AspectRatio)

    /** 拍照 */
    fun takePhoto(file: File, callback: CameraResultCallback)

    /** 开始录视频 */
    fun startRecording(file: File, maxDurationMillis: Long, callback: RecordCallback)

    /** 停止录视频 */
    fun stopRecording()
}