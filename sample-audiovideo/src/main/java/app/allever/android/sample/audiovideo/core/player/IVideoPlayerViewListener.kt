package app.allever.android.sample.audiovideo.core.player

import app.allever.android.sample.audiovideo.lib.VideoScaleMode

/**
 * VideoPlayerView 监听器接口
 *
 * 提供播放器UI交互事件的回调，用于与外部组件通信。
 *
 * ## 使用示例
 * ```kotlin
 * playerView.setListener(object : IVideoPlayerViewListener {
 *     override fun onBackClicked() {
 *         activity?.onBackPressed()
 *     }
 *
 *     override fun onPlayPauseChanged(isPlaying: Boolean) {
 *         Log.d(TAG, "播放状态: $isPlaying")
 *     }
 *
 *     override fun onProgressChanged(position: Long, duration: Long) {
 *         // 更新外部进度显示
 *     }
 * })
 * ```
 */
interface IVideoPlayerViewListener {

    /**
     * 返回按钮被点击
     */
    fun onBackClicked() {}

    /**
     * 播放/暂停状态改变
     *
     * @param isPlaying true 正在播放，false 已暂停
     */
    fun onPlayPauseChanged(isPlaying: Boolean) {}

    /**
     * 进度改变（用户拖动或播放中）
     *
     * @param position 当前位置（毫秒）
     * @param duration 总时长（毫秒）
     */
    fun onProgressChanged(position: Long, duration: Long) {}

    /**
     * 缩放模式改变
     *
     * @param mode 新的缩放模式
     */
    fun onScaleModeChanged(mode: VideoScaleMode) {}

    /**
     * 播放速度改变
     *
     * @param speed 新的倍速 (0.5f ~ 3.0f)
     */
    fun onSpeedChanged(speed: Float) {}

    /**
     * 渲染器切换
     *
     * @param renderName 新的渲染器名称
     */
    fun onRenderSwitched(renderName: String) {}

    /**
     * 引擎切换
     *
     * @param engineType 新的引擎类型名称
     */
    fun onEngineSwitched(engineType: String) {}

    /**
     * 控制栏显示/隐藏状态改变
     *
     * @param isVisible true 显示，false 隐藏
     */
    fun onControlVisibilityChanged(isVisible: Boolean) {}

    /**
     * 触摸事件开始（用于处理需要暂停其他逻辑的场景）
     */
    fun onTouchDown() {}

    /**
     * 触摸事件结束
     */
    fun onTouchUp() {}
}
