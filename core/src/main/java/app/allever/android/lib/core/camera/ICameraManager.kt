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

    /** 拍照 */
    fun takePhoto(file: File, callback: CameraResultCallback)

    /** 开始录视频 */
    fun startRecording(file: File, callback: CameraResultCallback)

    /** 停止录视频 */
    fun stopRecording()
}