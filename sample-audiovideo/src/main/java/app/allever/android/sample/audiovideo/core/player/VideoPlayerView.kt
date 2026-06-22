package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.R
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

    //TAG
    private val TAG = VideoPlayerView::class.java.simpleName

    /** ViewBinding */
    protected var binding: VideoPlayerViewBinding = VideoPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)


    /** 当前倍速索引 */
    protected var currentSpeedIndex: Int = 1  // 默认 1x

    /** 当前缩放模式索引 */
    protected var currentScaleModeIndex: Int = 0  // 默认 FIT_CENTER

    /** 默认倍速列表 */
    private val SPEED_LIST = floatArrayOf(0.5f, 1f, 1.5f, 2f, 2.5f, 3f)

    /** 缩放模式列表 */
    private val SCALE_MODE_LIST = arrayOf(
        VideoScaleMode.FIT_CENTER,
        VideoScaleMode.CROP_CENTER,
        VideoScaleMode.STRETCH
    )

    private var isUserSeeking = false

    // ui 元素
    val renderContainer = binding.renderContainer

    var videoPlayer = VideoPlayer()

    val seekBar = binding.seekBarVP
    
    val tvDuration = binding.tvVPDuration
    
    val tvProgress = binding.tvVPProgress

    override fun onFinishInflate() {
        super.onFinishInflate()
        initClickListener()
        initSeekBar()
    }

    private fun initClickListener() {
        binding.touchInterceptView.setOnClickListener {
            binding.controlPanel.isVisible = !binding.controlPanel.isVisible
        }

        binding.tvVPSpeed.setOnClickListener {
            switchSpeed()
        }

        binding.ivVPScaleMode.setOnClickListener {
            switchScaleMode()
        }
    }
    
    private fun initSeekBar() {
        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && videoPlayer.duration > 0) {
                    val position = (progress.toFloat() / 100 * videoPlayer.duration).toLong()
                    binding.seekBarVP.progress = progress
                    tvDuration.text = formatTime(videoPlayer.duration)
                    tvProgress.text = formatTime(position)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (videoPlayer.duration > 0 && seekBar != null) {
                    val position = (seekBar.progress.toFloat() / 100 * videoPlayer.duration).toLong()
                    videoPlayer.seekTo(position)
                }
                binding.seekBarVP.post { isUserSeeking = false }
            }
        }

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    /**
     * 切换播放速度
     *
     * 循环切换：0.5x → 1.0x → 1.5x → 2.0x → 2.5x → 3.0x → 0.5x
     */
    private fun switchSpeed() {
        currentSpeedIndex = (currentSpeedIndex + 1) % SPEED_LIST.size
        val newSpeed = SPEED_LIST[currentSpeedIndex]

        videoPlayer.speed = newSpeed
        binding.tvVPSpeed.text = "${newSpeed}x"

        log(TAG,"speed changed to: ${newSpeed}x")
    }

    /**
     * 切换缩放模式
     *
     * 循环切换：FIT_CENTER → CROP_CENTER → STRETCH → FIT_CENTER
     */
    fun switchScaleMode() {
        currentScaleModeIndex = (currentScaleModeIndex + 1) % SCALE_MODE_LIST.size
        val newMode = SCALE_MODE_LIST[currentScaleModeIndex]

        videoPlayer.videoScaleMode = newMode

        // 更新图标（根据模式切换不同图标）
        updateScaleModeIcon(newMode)

        log(TAG, "scale mode changed to: $newMode")
    }

    /**
     * 更新缩放模式图标
     */
    private fun updateScaleModeIcon(mode: VideoScaleMode) {
        val iconRes = when (mode) {
            VideoScaleMode.FIT_CENTER -> R.drawable.ic_crop_free
            VideoScaleMode.CROP_CENTER -> R.drawable.ic_crop_free  // 可替换为裁剪图标
            VideoScaleMode.STRETCH -> R.drawable.ic_crop_free  // 可替换为拉伸图标
        }
        binding.ivVPScaleMode.setImageResource(iconRes)
    }

    /**
     * 格式化时间显示
     */
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
