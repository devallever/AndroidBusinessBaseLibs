package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
import app.allever.android.sample.audiovideo.databinding.VideoPlayerViewBinding
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import java.util.Locale

/**
 * 视频播放器UI控制组件
 *
 * 封装了视频播放器的完整UI交互逻辑，包括：
 * - 控制栏显示/隐藏（点击切换）
 * - 进度条实时 seekTo
 * - 手势控制（左侧1/3音量、右侧1/3亮度、底部1/3进度）
 * - 倍速切换 (0.5x ~ 3.0x)
 * - 缩放模式切换
 * - 渲染器/引擎动态切换
 */
class VideoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = VideoPlayerView::class.java.simpleName

    /** 手势触发阈值（dp）*/
    private val GESTURE_THRESHOLD_DP = 10f

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

    // 手势相关变量
    private var gestureThresholdPx = 0f
    private var isGestureProcessing = false
    private var gestureType: GestureType? = null
    private var gestureStartY = 0f
    private var gestureStartX = 0f
    private var gestureLastY = 0f
    private var gestureLastX = 0f
    private var gestureStartPosition = 0L
    private var gestureTargetPosition = 0L
    private var initialVolume = getVideoVolume()
    private var initialBrightness = getCurrentBrightness()

    /** 手势类型枚举 */
    enum class GestureType {
        VOLUME,      // 音量调节
        BRIGHTNESS,  // 亮度调节
        SEEK         // 进度调节
    }

    // ui 元素
    val renderContainer = binding.renderContainer

    var videoPlayer = VideoPlayer()

    val seekBar = binding.seekBarVP

    val tvDuration = binding.tvVPDuration

    val tvProgress = binding.tvVPProgress

    val tvTitle = binding.tvVPTitle

    private var listener: IVideoPlayerViewListener? = null

    init {
        // 计算手势阈值（转换为像素）
        gestureThresholdPx = GESTURE_THRESHOLD_DP * resources.displayMetrics.density
    }

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
     * 设置视频源并准备播放
     *
     * @param uri 视频 URL 或文件路径
     */
    fun setSource(uri: Uri) {
        // 提取标题并显示
        tvTitle.text = extractTitle(uri)
        videoPlayer.setSource(uri)
        appendLog("setSource: $uri")
    }

    /**
     * 从 URL 或路径中提取标题
     */
    private fun extractTitle(uri: Uri): String {
        return uri.toString()
    }

    /**
     * 切换引擎
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
                setSource(currentUri)
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
        // 返回按钮
        binding.ivVPBack.setOnClickListener {
            listener?.onBackClicked()
        }

        // 点击画面切换控制栏
        binding.touchInterceptView.setOnClickListener {
            toggleControlVisibility()
        }

        // 设置触摸手势监听
        binding.touchInterceptView.setOnTouchListener { _, event ->
            handleTouchEvent(event)
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

    /**
     * 切换控制栏可见性
     */
    private fun toggleControlVisibility() {
        val isVisible = binding.controlPanel.isVisible
        binding.controlPanel.isVisible = !isVisible
        listener?.onControlVisibilityChanged(!isVisible)
    }

    // ==================== 手势处理系统 ====================

    /**
     * 处理触摸事件
     */
    private fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onTouchDown(event)
            MotionEvent.ACTION_MOVE -> onTouchMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onTouchUp()
        }
        return true
    }

    /**
     * 触摸按下事件
     */
    private fun onTouchDown(event: MotionEvent) {
        isGestureProcessing = false
        gestureType = null
        gestureStartY = event.y
        gestureStartX = event.x
        gestureLastY = event.y
        gestureLastX = event.x
        gestureStartPosition = videoPlayer.currentPosition
        gestureTargetPosition = gestureStartPosition

        // 显示控制栏
        if (!binding.controlPanel.isVisible) {
            binding.controlPanel.isVisible = true
        }
    }

    /**
     * 触摸移动事件
     */
    private fun onTouchMove(event: MotionEvent) {
        val deltaY = event.y - gestureLastY
        val deltaX = event.x - gestureLastX
        val totalDeltaY = kotlin.math.abs(event.y - gestureStartY)
        val totalDeltaX = kotlin.math.abs(event.x - gestureStartX)

        // 如果还未确定手势类型，根据滑动方向和位置判断
        if (!isGestureProcessing) {
            // 检查是否超过阈值
            if (totalDeltaY < gestureThresholdPx && totalDeltaX < gestureThresholdPx) {
                return
            }

            isGestureProcessing = true

            // 判断手势类型：根据触摸位置和滑动方向
            when {
                // 底部1/3区域，且水平滑动为主 → 进度调节
                event.y > height * 2 / 3f && totalDeltaX > totalDeltaY -> {
                    gestureType = GestureType.SEEK
                }
                // 左侧1/3区域 → 音量调节
                event.x < width / 3f -> {
                    gestureType = GestureType.VOLUME
                }
                // 右侧1/3区域 → 亮度调节
                else -> {
                    gestureType = GestureType.BRIGHTNESS
                }
            }
        }

        // 执行对应的手势操作（基于触摸距离占容器百分比）
        when (gestureType) {
            GestureType.VOLUME -> handleVolumeGesture(deltaY, height.toFloat())
            GestureType.BRIGHTNESS -> handleBrightnessGesture(deltaY, height.toFloat())
            GestureType.SEEK -> handleSeekGesture(deltaX, width.toFloat())
            null -> {}
        }

        gestureLastY = event.y
        gestureLastX = event.x
    }

    /**
     * 触摸抬起事件
     */
    private fun onTouchUp() {
        if (!isGestureProcessing) {
            // 没有执行手势操作，不处理（点击事件由 OnClickListener 处理）
            return
        }

        // 隐藏手势提示浮层
        hideAllGestureOverlays()

        isGestureProcessing = false
        gestureType = null
    }

    /**
     * 处理音量手势（左侧1/3区域上下滑动）
     *
     * @param deltaY 垂直滑动距离（像素）
     * @param containerHeight 容器高度（像素）
     *
     * 修改值基于 deltaY / containerHeight 的百分比：
     * - 向上滑动 → 音量增加
     * - 向下滑动 → 音量减少
     */
    private fun handleVolumeGesture(deltaY: Float, containerHeight: Float) {
        // 计算变化百分比：向上滑为负值（增加），向下滑为正值（减少）
        val deltaPercent = -deltaY / containerHeight

        // 计算新音量 (0-1)，基于初始值 + 变化量
        val newVolume = (videoPlayer.volume + deltaPercent).coerceIn(0f, 1f)

        // 设置视频音量（不是系统音量）
        setVideoVolume(newVolume)

        // 更新手势提示 UI
        showVolumeOverlay(newVolume)
    }

    /**
     * 处理亮度手势（右侧1/3区域上下滑动）
     *
     * @param deltaY 垂直滑动距离（像素）
     * @param containerHeight 容器高度（像素）
     */
    private fun handleBrightnessGesture(deltaY: Float, containerHeight: Float) {
        // 计算变化百分比
        val deltaPercent = -deltaY / containerHeight

        // 计算新亮度 (0.01-1.0)
        val newBrightness = (initialBrightness + deltaPercent).coerceIn(0.01f, 1f)
        initialBrightness = newBrightness

        // 设置屏幕亮度
        setScreenBrightness(newBrightness)

        // 更新手势提示 UI
        showBrightnessOverlay(newBrightness)
    }

    /**
     * 处理进度手势（底部1/3区域左右滑动）
     *
     * @param deltaX 水平滑动距离（像素）
     * @param containerWidth 容器宽度（像素）
     *
     * 修改值基于 deltaX / containerWidth 的百分比乘以总时长：
     * - 向右滑动 → 快进
     * - 向左滑动 → 快退
     */
    private fun handleSeekGesture(deltaX: Float, containerWidth: Float) {
        if (videoPlayer.duration <= 0) return

        // 计算变化百分比
        val deltaPercent = deltaX / containerWidth

        // 计算目标位置（毫秒）
        val deltaTime = (deltaPercent * videoPlayer.duration).toLong()
        gestureTargetPosition = (gestureTargetPosition + deltaTime).coerceIn(0, videoPlayer.duration)

        // 实时 seekTo
        videoPlayer.seekTo(gestureTargetPosition)

        // 同步更新进度条和时间显示
        if (videoPlayer.duration > 0) {
            val progress = (gestureTargetPosition.toFloat() / videoPlayer.duration * 100).toInt()
            seekBar.progress = progress
            tvProgress.text = formatTime(gestureTargetPosition)
        }

        // 显示进度提示（显示时间差和当前位置）
        showSeekOverlay(gestureTargetPosition, gestureStartPosition)
    }

    /**
     * 显示音量手势提示浮层
     */
    private fun showVolumeOverlay(volume: Float) {
        binding.gestureVolumeContainer.visibility = View.VISIBLE
        binding.gestureVolumeContainer.alpha = 1f
        binding.volumeProgressBar.progress = (volume * 100).toInt()

        // 根据音量更新图标
        val iconRes = when {
            volume <= 0f -> R.drawable.ic_volume_mute
            volume < 0.33f -> R.drawable.ic_volume_low
            volume < 0.66f -> R.drawable.ic_volume_medium
            else -> R.drawable.ic_volume_up
        }
        binding.ivVolumeIcon.setImageResource(iconRes)
    }

    /**
     * 显示亮度手势提示浮层
     */
    private fun showBrightnessOverlay(brightness: Float) {
        binding.gestureBrightnessContainer.visibility = View.VISIBLE
        binding.gestureBrightnessContainer.alpha = 1f
        binding.brightnessProgressBar.progress = (brightness * 100).toInt()
    }

    /**
     * 显示进度手势提示浮层
     *
     * @param currentPosition 当前目标位置（毫秒）
     * @param startPosition 手势开始时的位置（毫秒）
     */
    private fun showSeekOverlay(currentPosition: Long, startPosition: Long) {
        binding.tvGestureSeekTime.visibility = View.VISIBLE
        binding.tvGestureSeekTime.alpha = 1f

        // 计算时间差
        val diffMs = currentPosition - startPosition
        val diffText = if (diffMs >= 0) "+${formatTime(diffMs)}" else "-${formatTime(kotlin.math.abs(diffMs))}"

        // 显示格式：时间差\n当前时间
        binding.tvGestureSeekTime.text = "$diffText\n${formatTime(currentPosition)}"
    }

    /**
     * 隐藏所有手势提示浮层（带淡出动画）
     */
    private fun hideAllGestureOverlays() {
        binding.gestureVolumeContainer.animate().alpha(0f).withEndAction {
            binding.gestureVolumeContainer.visibility = View.GONE
        }.start()

        binding.gestureBrightnessContainer.animate().alpha(0f).withEndAction {
            binding.gestureBrightnessContainer.visibility = View.GONE
        }.start()

        binding.tvGestureSeekTime.animate().alpha(0f).withEndAction {
            binding.tvGestureSeekTime.visibility = View.GONE
        }.start()
    }

    // ==================== 音量和亮度控制 ====================

    /**
     * 获取当前视频音量 (0-1)
     *
     * 注意：这里返回的是视频播放器的音量，不是系统音量。
     * 如果 VideoPlayer 支持 volume 属性则使用它，否则返回默认值。
     */
    private fun getVideoVolume(): Float {
        return try {
            videoPlayer.volume
        } catch (e: Exception) {
            1f  // 默认最大音量
        }
    }

    /**
     * 设置视频音量 (0-1)
     *
     * 只影响视频播放器的音量，不影响系统音量。
     */
    private fun setVideoVolume(volume: Float) {
        try {
            videoPlayer.volume = volume.coerceIn(0f, 1f)
        } catch (e: Exception) {
            log(TAG, "setVideoVolume error: ${e.message}")
        }
    }

    /**
     * 获取当前屏幕亮度 (0-1)
     */
    private fun getCurrentBrightness(): Float {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            128
        ) / 255f
    }

    /**
     * 设置屏幕亮度 (0.01-1.0)
     */
    private fun setScreenBrightness(brightness: Float) {
        val window = (context as? android.app.Activity)?.window ?: return
        window.attributes = window.attributes.apply {
            screenBrightness = brightness.coerceIn(0.01f, 1f)
        }
    }

    fun appendLog(msg: String) {
        listener?.onLog(msg)
        log(TAG, msg)
    }

}
