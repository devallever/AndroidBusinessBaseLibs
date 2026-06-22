package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.core.engine.EngineRegistry
import app.allever.android.sample.audiovideo.core.engine.MediaPlayerEngine
import app.allever.android.sample.audiovideo.core.render.RenderRegistry
import app.allever.android.sample.audiovideo.core.render.SurfaceViewRender
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.databinding.VideoPlayerViewBinding
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerState
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


    /** 当前使用的渲染器名称 */
    var currentRenderName: String = SurfaceViewRender.NAME

    /** 当前引擎类型 */
    var currentEngineType = MediaPlayerEngine.NAME

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    private var isUserSeeking = false

    // ui 元素
    val renderContainer = binding.renderContainer

    var videoPlayer = VideoPlayer()

    val seekBar = binding.seekBarVP
    
    val tvDuration = binding.tvVPDuration
    
    val tvProgress = binding.tvVPProgress

    private var listener: IVideoPlayerViewListener? = null

    /**
     * 播放器事件监听器
     */
    private val playerListener = object : IVideoPlayerListener {

        override fun onPrepared(durationMs: Long) {
            appendLog("onPrepared: 时长: ${formatTime(durationMs)}")

            post {
                updateButtonStates()
                listener?.debugUpdateState()

                if (autoPlayOnPrepared) {
                    videoPlayer.play()
                    appendLog("自动开始播放")
                }
            }
        }

        override fun onComplete() {
            appendLog("onComplete: 播放完成")

            post {
                updateButtonStates()
                listener?.debugUpdateState()
            }
        }

        override fun onError(code: Int, msg: String): Boolean {
            appendLog("onError: 错误码=$code, 消息: $msg")

            post {
                updateButtonStates()
                listener?.debugUpdateState()
            }
            return true
        }

        override fun onProgress(position: Long, duration: Long) {
            if (!isUserSeeking && duration > 0) {
                post {
                    val progress = (position.toFloat() / duration * 100).toInt()
                    seekBar.progress = progress
                    tvProgress.text = formatTime(position)
                    tvDuration.text = formatTime(duration)
                    listener?.onProgressChanged(position, duration)
                }
            }
        }

        override fun onBufferingUpdate(percent: Int) {
            // 可在此更新缓冲进度 UI
        }

        override fun onStateChanged(oldState: PlayerState, newState: PlayerState) {
            appendLog("onStateChanged: $oldState -> $newState")

            post {
                updateButtonStates()
                listener?.debugUpdateState()
            }
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            appendLog("onVideoSizeChanged: 尺寸: ${width}x${height}")
//            mBinding.tvVideoSize.text = "${width}x${height}"
        }

        override fun onInfo(what: Int, extra: Int): Boolean {
            // 信息日志，通常不需要显示给用户
            return true
        }
    }

    fun setListener(listener: IVideoPlayerViewListener?) {
        this.listener = listener
    }

    /**
     * 切换引擎（新架构核心功能演示）
     *
     * 展示运行时动态切换播放引擎的能力，
     * 这是组合模式相比继承模式的核心优势。
     *
     * 注意：切换引擎需要重建整个播放器实例，
     * 因为不同引擎的内部状态不兼容。
     */
    fun switchEngine(targetType: String) {
        if (currentEngineType == targetType) {
            appendLog("当前已是 $targetType 引擎")
            return
        }

        val wasPlaying = videoPlayer.isPlaying || videoPlayer.state == PlayerState.PAUSED
        val savedPosition = videoPlayer.currentPosition

        appendLog("开始切换引擎: ${currentEngineType} -> $targetType" +
                (if (wasPlaying) " (正在播放，位置=${formatTime(savedPosition)})" else ""))

        // 保存当前数据源信息
        val currentUri = videoPlayer.engine.getCurrentUri()

        // 释放旧播放器
        try {
            videoPlayer.release()
            appendLog("已释放旧引擎")
        } catch (_: Exception) {}

        // 创建新引擎
        val newEngine = EngineRegistry.create(targetType)?: return

        // 重建播放器（使用当前渲染器）
        val currentRender = RenderRegistry.create(currentRenderName) ?: SurfaceViewRender()

        videoPlayer = VideoPlayer(
            engine = newEngine,
            render = currentRender
        ).apply {
            attach(renderContainer)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }

        currentEngineType = targetType
        listener?.debugUpdateState()
        updateArchInfo()

        appendLog("已切换到: $targetType")

        // 如果之前在播放，尝试恢复
        if (wasPlaying && savedPosition > 0) {
            autoPlayOnPrepared = true
            if (currentUri == null) {
                toast("无播放源")
                appendLog("无播放源")
            } else {
                videoPlayer.setSource(currentUri)
            }
            appendLog("恢复播放: ${formatTime(savedPosition)} -> $currentUri")
        }
    }

    /**
     * 切换渲染器（新架构核心功能演示）
     *
     * 展示运行时动态切换渲染方式的能力，
     * 这是组合模式相比继承模式的最大优势。
     */
    fun switchRender(renderName: String) {
        if (currentRenderName == renderName) {
            appendLog("当前已是 $renderName 渲染模式")
            return
        }

        val wasPlaying = videoPlayer.isPlaying || videoPlayer.state == PlayerState.PAUSED
        val savedPosition = videoPlayer.currentPosition

        appendLog("开始切换渲染器: $currentRenderName -> $renderName" +
                (if (wasPlaying) " (正在播放，位置=${formatTime(savedPosition)})" else ""))

        // 使用 RenderRegistry 创建新的渲染器实例
        val newRender = RenderRegistry.create(renderName)
        if (newRender == null) {
            appendLog("未注册的渲染器: $renderName")
            toast("未注册的渲染器: $renderName")
            return
        }

        // 执行安全切换（VideoPlayer 内部已处理 PlayerView 绑定）
        videoPlayer.safeSwitchToRender(newRender)

        currentRenderName = renderName
        listener?.debugUpdateState()
        updateButtonStates()
        updateArchInfo()

        appendLog("已切换到: $renderName")
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initPlayer()
        initClickListener()
        initSeekBar()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        videoPlayer.detach()
        videoPlayer.release()
        appendLog("onDetachedFromWindow: 解绑视图")
        appendLog("onDetachedFromWindow: 释放资源")
    }

    /**
     * 初始化播放器（使用新架构的组合模式）
     */
    private fun initPlayer() {
        // 使用默认配置：MediaPlayerEngine + SurfaceViewRender
        videoPlayer = VideoPlayer(
            engine = MediaPlayerEngine(),
            render = SurfaceViewRender()
        ).apply {
            attach(binding.renderContainer)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }

        appendLog("初始化播放器: MediaPlayerEngine + SurfaceViewRender")
        updateArchInfo()
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

        binding.ivVPPlayPause.setOnClickListener {
            if (videoPlayer.isPlaying) {
                videoPlayer.pause()
            } else {
                videoPlayer.play()
            }
            updateButtonStates()
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
     * 更新架构信息显示
     */
    private fun updateArchInfo() {
        val engineName = currentEngineType
        // 使用 RenderRegistry 获取渲染器的显示名称
        val renderDisplayName = try {
            RenderRegistry.create(currentRenderName)?.renderName ?: currentRenderName
        } catch (e: Exception) {
            currentRenderName
        }
        appendLog("当前组合: $engineName + $renderDisplayName")
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

    /**
     * 更新按钮启用/禁用状态
     */
    private fun updateButtonStates() {
        val state = videoPlayer.state
        val canPlay = state in listOf(
            PlayerState.IDLE,
            PlayerState.STOPPED,
            PlayerState.PREPARED,
            PlayerState.PAUSED,
            PlayerState.COMPLETED,
            PlayerState.ERROR
        )
        val isPlaying = state == PlayerState.PLAYING
        if (isPlaying) {
            binding.ivVPPlayPause.setImageResource(R.drawable.ic_sample_video_player_view_pause)
        } else {
            binding.ivVPPlayPause.setImageResource(R.drawable.ic_sample_video_player_view_play)
        }
    }

    fun appendLog(msg: String) {
        listener?.onLog(msg)
    }

}
