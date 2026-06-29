package app.allever.android.lib.core.camera

import java.io.File

/** 录像结果与进度回调 */
interface RecordCallback {
    fun onProgress(millis: Long) // 当前录制时长(毫秒)
    fun onSuccess(file: File)
    fun onError(message: String)
}