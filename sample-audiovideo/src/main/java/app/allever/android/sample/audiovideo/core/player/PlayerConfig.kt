package app.allever.android.sample.audiovideo.core.player

import app.allever.android.sample.audiovideo.lib.VideoScaleMode

/**
 * 播放器UI配置
 *
 * 用于控制 VideoPlayerView 中各个按钮的可见性和功能开关。
 * 支持通过 copy() 方法灵活自定义。
 *
 * ## 使用示例
 * ```kotlin
 * // 基础配置（显示基本控制）
 * val config = PlayerConfig()
 *
 * // 自定义配置（隐藏部分按钮，开启渲染器切换）
 * val customConfig = PlayerConfig(
 *     showScaleModeButton = false,
 *     showRenderSwitchButton = true,
 *     autoHideControlDelay = 5000L
 * )
 *
 * // 使用 DSL 更新配置
 * playerView.updateConfig {
 *     showSpeedButton = false
 *     autoHideControlDelay = 4000L
 * }
 * ```
 */
data class PlayerConfig(
    /** 是否显示返回按钮 */
    var showBackButton: Boolean = true,

    /** 是否显示标题 */
    var showTitle: Boolean = true,

    /** 是否显示播放/暂停按钮 */
    var showPlayPause: Boolean = true,

    /** 是否显示进度条 */
    var showSeekBar: Boolean = true,

    /** 是否显示时间文本 */
    var showTimeText: Boolean = true,

    /** 是否显示缩放模式按钮 */
    var showScaleModeButton: Boolean = true,

    /** 是否显示倍速按钮 */
    var showSpeedButton: Boolean = true,

    /** 是否启用音量手势（左侧上下滑动）*/
    var enableVolumeGesture: Boolean = true,

    /** 是否启亮度手势（右侧上下滑动）*/
    var enableBrightnessGesture: Boolean = true,

    /** 是否启用进度调节手势（底部左右滑动）*/
    var enableSeekGesture: Boolean = true,

    /** 控制栏自动隐藏延迟（毫秒）*/
    var autoHideControlDelay: Long = 5000L
)
