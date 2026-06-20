package app.allever.android.sample.audiovideo.lib

/**
 * 音频播放事件监听接口
 *
 * 所有回调均在主线程触发。
 * onError 返回 true 表示错误已被消费，播放器不再处理。
 */
interface IPlayerListener {
    /** 状态变化 */
    fun onStateChanged(from: PlayerState, to: PlayerState) {}

    /** 准备就绪（此时可获取 duration，需调用 play() 才会开始播放） */
    fun onPrepared(durationMs: Long) {}

    /** 进度更新（定时回调） */
    fun onProgress(currentMs: Long, durationMs: Long) {}

    /** 播放完成 */
    fun onComplete() {}

    /**
     * 出错
     *
     * @param errorCode 错误代码（使用 [PlayerErrorCode] 中定义的常量）
     * @param msg 错误消息（可读的错误描述）
     * @return true 表示错误已被消费，播放器不再处理；false 表示播放器继续处理
     */
    fun onError(errorCode: Int, msg: String): Boolean = false

    /** 缓冲进度 (0~100) */
    fun onBufferingUpdate(percent: Int) {}
}