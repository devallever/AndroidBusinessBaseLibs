package app.allever.android.lib.core.camera

import java.io.File

/** 相机操作结果回调 */
interface CameraResultCallback {
    fun onSuccess(file: File)
    fun onError(message: String)
    fun onProgress(millis: Long) { } // 当前录制时长(毫秒)
}