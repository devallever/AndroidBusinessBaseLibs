package app.allever.android.lib.player.core

/**
* 视频播放事件监听接口（扩展音频监听器，增加视频特有回调）
*
* 所有回调均在主线程触发。
* onError 返回 true 表示错误已被消费，播放器不再处理。
*/
interface IVideoPlayerListener : IPlayerListener {
    /** 视频尺寸变化 */
    fun onVideoSizeChanged(width: Int, height: Int) {}

    /** 播放器信息回调（如缓冲、渲染等），返回 true 表示已消费 */
    fun onInfo(what: Int, extra: Int): Boolean = false

    /** 首帧渲染完成（视频开始显示） */
    fun onFirstFrameRendered() {}

    /** 开始缓冲 */
    fun onBufferingStart() {}

    /** 缓冲结束 */
    fun onBufferingEnd() {}

    /** 网络带宽信息（bps） */
    fun onNetworkBandwidth(bps: Long) {}

    /** 单曲循环重启 */
    fun onLoopRestart() {}
}