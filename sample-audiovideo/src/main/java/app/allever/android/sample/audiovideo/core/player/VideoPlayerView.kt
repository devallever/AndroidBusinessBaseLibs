package app.allever.android.sample.audiovideo.core.player

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.core.render.RenderRegistry
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.databinding.VideoPlayerViewBinding
import java.util.Locale

/**
 * 视频播放器UI控制组件
 *
 * 封装了视频播放器的完整UI交互逻辑，包括：
 * - 控制栏显示/隐藏（自动隐藏 + 点击切换）
 * - 进度条实时 seekTo
 * - 手势控制（音量、亮度、进度）
 * - 倍速切换 (0.5x ~ 3.0x)
 * - 缩放模式切换
 * - 渲染器/引擎动态切换
 *
 * ## 设计原则
 * - **组合模式**：内部持有 VideoPlayer 实例，委托播放逻辑
 * - **开闭原则**：支持继承扩展，通过 PlayerConfig 控制可见性
 * - **单一职责**：只负责 UI 展示和用户交互，播放逻辑由 VideoPlayer 处理
 *
 * ## 使用示例
 * ```kotlin
 * // 基础使用
 * val playerView = VideoPlayerView(context).apply {
 *     setSource("https://example.com/video.mp4")
 *     play()
 * }
 *
 * // 自定义配置
 * val customView = VideoPlayerView(context).apply {
 *     updateConfig {
 *         showScaleModeButton = false
 *         showRenderSwitchButton = true
 *     }
 *     setListener(object : IVideoPlayerViewListener {
 *         override fun onBackClicked() { activity?.onBackPressed() }
 *     })
 * }
 * ```
 */
class VideoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /** ViewBinding */
    protected var binding: VideoPlayerViewBinding = VideoPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)

    val renderContainer = binding.renderContainer

    val videoPlayer = VideoPlayer()

}
