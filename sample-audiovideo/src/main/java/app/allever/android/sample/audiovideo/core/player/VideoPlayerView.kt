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

    companion object {
        private const val TAG = "VideoPlayerView"

        /** 默认倍速列表 */
        val SPEED_LIST = floatArrayOf(0.5f, 1f, 1.5f, 2f, 2.5f, 3f)

        /** 缩放模式列表 */
        val SCALE_MODE_LIST = arrayOf(
            VideoScaleMode.FIT_CENTER,
            VideoScaleMode.CROP_CENTER,
            VideoScaleMode.STRETCH
        )

        /** 触发手势的最小滑动距离（dp）*/
        const val GESTURE_THRESHOLD_DP = 10f

        /** 触摸区域比例：左侧 1/4 为音量，右侧 1/4 为亮度，底部 1/4 为进度 */
        const val GESTURE_AREA_RATIO = 0.25f
    }

    // ==================== 核心组件 ====================

    /** 内部持有的播放器实例 */
    protected val videoPlayer = VideoPlayer()

    /** ViewBinding */
    protected var binding: VideoPlayerViewBinding = VideoPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)

    /** 播放器配置 */
    var config: PlayerConfig = PlayerConfig()
        set(value) {
            field = value
            applyConfig()
        }

    /** 监听器 */
    var listener: IVideoPlayerViewListener? = null

    /** 是否在 PREPARING 状态下请求了自动播放 */
    private var pendingAutoPlay: Boolean = false

    // ==================== 状态管理 ====================

    /** 当前倍速索引 */
    protected var currentSpeedIndex: Int = 1  // 默认 1x

    /** 当前缩放模式索引 */
    protected var currentScaleModeIndex: Int = 0  // 默认 FIT_CENTER

    /** 控制栏是否可见 */
    protected var isControlVisible: Boolean = true

    /** 用户是否正在拖动进度条 */
    protected var isUserSeeking: Boolean = false

    /** 是否正在处理手势 */
    protected var isGestureProcessing: Boolean = false

    /** 手势类型 */
    protected enum class GestureType { NONE, VOLUME, BRIGHTNESS, SEEK }

    /** 当前手势类型 */
    protected var currentGestureType: GestureType = GestureType.NONE

    // ==================== 手势相关变量 ====================

    /** 首次触摸位置 X */
    protected var touchStartX: Float = 0f

    /** 首次触摸位置 Y */
    protected var touchStartY: Float = 0f

    /** 上次触摸位置 Y（用于音量和亮度）*/
    protected var touchLastY: Float = 0f

    /** 上次触摸位置 X（用于进度）*/
    protected var touchLastX: Float = 0f

    /** 手势开始时的进度位置（毫秒）*/
    protected var gestureStartPosition: Long = 0L

    /** 手势改变后的目标位置（毫秒）*/
    protected var gestureTargetPosition: Long = 0L

    /** 是否已触发手势（超过阈值）*/
    protected var isGestureTriggered: Boolean = false

    // ==================== 定时任务 ====================

    /** 主线程 Handler */
    protected val mainHandler = Handler(Looper.getMainLooper())

    /** 控制栏自动隐藏延迟任务 */
    protected var autoHideRunnable: Runnable? = null

    /** 进度更新定时任务（每秒更新一次）*/
    protected var progressUpdateRunnable: Runnable? = null

    // ==================== 初始化 ====================

    private var isInitialized = false

    /**
     * 在 XML inflate 完成后初始化
     *
     * 必须在 onFinishInflate 中初始化，因为此时子视图已经添加完成，
     * ViewBinding.bind() 才能正确绑定。
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInitialized) return
        isInitialized = true
        initView()
        setupListeners()
        setupProgressUpdate()
    }

    /**
     * 初始化视图
     */
    protected open fun initView() {
        // 将 VideoPlayer attach 到渲染容器
        // 这会：1) 初始化渲染器并添加到容器 2) 注册 Surface ready 回调
        videoPlayer.attach(binding.renderContainer)
        setupInternalPlayerListener()
        applyConfig()
        showControl(true)
    }

    /**
     * 设置内部播放器监听器
     *
     * 用于处理 VideoPlayer 的事件回调，特别是：
     * - onPrepared: 检查 pendingAutoPlay 标志，自动开始播放
     */
    private fun setupInternalPlayerListener() {
        videoPlayer.setListener(object : IVideoPlayerListener {
            override fun onPrepared(durationMs: Long) {
                Log.d(TAG, "onPrepared: duration=${formatTime(durationMs)}")
                if (pendingAutoPlay) {
                    pendingAutoPlay = false
                    Log.d(TAG, "onPrepared: 自动播放（之前请求的 play）")
                    videoPlayer.play()
                    updatePlayPauseIcon()
                    startAutoHideTimer()
                    startProgressUpdate()
                }
            }

            override fun onComplete() { }
            override fun onError(code: Int, msg: String): Boolean = true
            override fun onProgress(position: Long, duration: Long) { updateProgressUI() }
            override fun onBufferingUpdate(percent: Int) { }
            override fun onVideoSizeChanged(width: Int, height: Int) { }
            override fun onInfo(what: Int, extra: Int): Boolean = true
            override fun onStateChanged(oldState: PlayerState, newState: PlayerState) { }
        })
    }

    /**
     * 应用配置到视图
     */
    protected fun applyConfig() {
        binding.apply {
            ivBack.visibility = if (config.showBackButton) View.VISIBLE else View.GONE
            tvTitle.visibility = if (config.showTitle) View.VISIBLE else View.GONE
            ivPlayPause.visibility = if (config.showPlayPause) View.VISIBLE else View.GONE
            seekBar.visibility = if (config.showSeekBar) View.VISIBLE else View.GONE
            tvProgress.visibility = if (config.showTimeText) View.VISIBLE else View.GONE
            tvDuration.visibility = if (config.showTimeText) View.VISIBLE else View.GONE
            ivScaleMode.visibility = if (config.showScaleModeButton) View.VISIBLE else View.GONE
            tvSpeed.visibility = if (config.showSpeedButton) View.VISIBLE else View.GONE
        }
    }

    /**
     * 更新配置（DSL 方式）
     *
     * ## 使用示例
     * ```kotlin
     * playerView.updateConfig {
     *     showScaleModeButton = false
     *     autoHideControlDelay = 5000L
     * }
     * ```
     */
    fun updateConfig(block: PlayerConfig.() -> Unit) {
        config = config.copy().apply(block)
    }

    // ==================== 对外接口 ====================

    /**
     * 设置数据源并准备播放
     *
     * @param url 视频 URL 或本地路径
     */
    fun setSource(url: String) {
        Log.d(TAG, "setSource: $url")

        // 提取标题
        binding.tvTitle.text = extractTitle(url)

        // 将渲染器添加到容器
        attachRenderer()

        // 设置数据源并准备
        videoPlayer.setSource(url)
    }

    /**
     * 设置 assets 目录下的视频文件并准备播放
     *
     * @param path Assets 中的文件路径（如 "output.mp4"）
     */
    fun setAssetSource(path: String) {
        Log.d(TAG, "setAssetSource: $path")

        // 提取标题（使用文件名）
        binding.tvTitle.text = extractTitle(path)

        // 将渲染器添加到容器
        attachRenderer()

        // 设置数据源并准备
        videoPlayer.setAssetSource(path)

    }

    /**
     * 开始播放
     *
     * 如果当前处于 PREPARING 状态（Surface 未就绪），
     * 会设置 pendingAutoPlay 标志，在 onPrepared 后自动开始播放。
     */
    fun play() {
        if (videoPlayer.state == PlayerState.PREPARING) {
            Log.d(TAG, "play(): 当前 PREPARING 状态，设置自动播放标志")
            pendingAutoPlay = true
            return
        }
        videoPlayer.play()
        updatePlayPauseIcon()
        startAutoHideTimer()
        startProgressUpdate()
    }

    /**
     * 暂停播放
     */
    fun pause() {
        videoPlayer.pause()
        updatePlayPauseIcon()
        cancelAutoHideTimer()
    }

    /**
     * 切换渲染器
     *
     * @param renderName 渲染器名称（需在 RenderRegistry 中注册）
     */
    fun switchRender(renderName: String) {
        Log.d(TAG, "switchRender: $renderName")

        // 从注册表创建新的渲染器实例
        val newRender = RenderRegistry.create(renderName)
            ?: run {
                Log.w(TAG, "未找到渲染器: $renderName")
                return
            }

        // 记录当前播放状态
        val wasPlaying = videoPlayer.isPlaying
        val currentPosition = videoPlayer.currentPosition

        // 切换渲染器
        videoPlayer.safeSwitchToRender(newRender)

        // 重新附加渲染器
        attachRenderer()

        // 恢复播放状态
        if (wasPlaying) {
            play()
        } else {
            seekTo(currentPosition)
        }

        listener?.onRenderSwitched(renderName)
    }

    /**
     * 切换引擎
     *
     * @param engineType 引擎类型名称
     */
    fun switchEngine(engineType: String) {
        Log.d(TAG, "switchEngine: $engineType")

        // TODO: 实现引擎切换逻辑
        listener?.onEngineSwitched(engineType)
    }

    /**
     * 跳转到指定位置
     *
     * @param position 目标位置（毫秒）
     */
    fun seekTo(position: Long) {
        videoPlayer.seekTo(position)
        updateProgressUI()
    }

    /**
     * 获取当前播放位置（毫秒）
     */
    val currentPosition: Long
        get() = videoPlayer.currentPosition

    /**
     * 释放资源
     *
     * **重要**：必须在 Activity/Fragment 的 onDestroy/onDestroyView 中调用
     */
    fun release() {
        Log.d(TAG, "release")

        stopProgressUpdate()
        cancelAutoHideTimer()
        videoPlayer.release()
    }

    // ==================== 渲染器管理 ====================

    /**
     * 将当前渲染器添加到容器中
     *
     * 注意：在 initView() 中已通过 videoPlayer.attach() 完成初始化，
     * 此方法仅在切换渲染器等特殊场景使用。
     */
    protected fun attachRenderer() {
        // videoPlayer.attach() 已处理渲染器添加，无需重复操作
        Log.d(TAG, "attachRenderer: 渲染器已在 initView() 中 attach")
    }

    // ==================== 控制栏显示/隐藏 ====================

    /**
     * 显示或隐藏控制栏
     *
     * @param visible true 显示，false 隐藏
     * @param animate 是否使用动画
     */
    fun showControl(visible: Boolean, animate: Boolean = true) {
        isControlVisible = visible

        if (!animate) {
            binding.topBarContainer.alpha = if (visible) 1f else 0f
            binding.bottomControlContainer.alpha = if (visible) 1f else 0f
            binding.topBarContainer.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            binding.bottomControlContainer.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        } else {
            animateControlVisibility(visible)
        }

        if (visible) {
            startAutoHideTimer()
        } else {
            cancelAutoHideTimer()
        }

        listener?.onControlVisibilityChanged(visible)
    }

    /**
     * 动画方式切换控制栏可见性
     */
    protected fun animateControlVisibility(show: Boolean) {
        val targetAlpha = if (show) 1f else 0f
        val topAnimator = ObjectAnimator.ofFloat(binding.topBarContainer, "alpha",
            binding.topBarContainer.alpha, targetAlpha)
        val bottomAnimator = ObjectAnimator.ofFloat(binding.bottomControlContainer, "alpha",
            binding.bottomControlContainer.alpha, targetAlpha)

        AnimatorSet().apply {
            playTogether(topAnimator, bottomAnimator)
            duration = 200
            start()
        }

        // 动画结束后更新 visibility
        mainHandler.postDelayed({
            binding.topBarContainer.visibility = if (show) View.VISIBLE else View.INVISIBLE
            binding.bottomControlContainer.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }, 200)
    }

    /**
     * 切换控制栏显示状态
     */
    fun toggleControl() {
        showControl(!isControlVisible)
    }

    /**
     * 启动控制栏自动隐藏计时器
     */
    protected fun startAutoHideTimer() {
        if (config.autoHideControlDelay <= 0 || !isControlVisible) return

        cancelAutoHideTimer()
        autoHideRunnable = Runnable { showControl(false) }
        mainHandler.postDelayed(autoHideRunnable!!, config.autoHideControlDelay)
    }

    /**
     * 取消自动隐藏计时器
     */
    protected fun cancelAutoHideTimer() {
        autoHideRunnable?.let {
            mainHandler.removeCallbacks(it)
            autoHideRunnable = null
        }
    }

    // ==================== 进度更新 ====================

    /**
     * 设置进度更新定时任务
     */
    protected fun setupProgressUpdate() {
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                if (videoPlayer.isPlaying && !isUserSeeking && !isGestureProcessing) {
                    updateProgressUI()
                }
                mainHandler.postDelayed(this, 1000)  // 每秒更新一次
            }
        }
    }

    /**
     * 开始进度更新
     */
    protected fun startProgressUpdate() {
        stopProgressUpdate()
        progressUpdateRunnable?.let {
            mainHandler.post(it)
        }
    }

    /**
     * 停止进度更新
     */
    protected fun stopProgressUpdate() {
        progressUpdateRunnable?.let {
            mainHandler.removeCallbacks(it)
        }
    }

    /**
     * 更新进度 UI
     */
    protected fun updateProgressUI() {
        if (isUserSeeking || isGestureProcessing) return

        val position = videoPlayer.currentPosition
        val duration = videoPlayer.duration

        if (duration > 0) {
            binding.seekBar.progress = ((position.toFloat() / duration) * 100).toInt()
        }

        binding.tvProgress.text = formatTime(position)
        binding.tvDuration.text = "/ ${formatTime(duration)}"

        listener?.onProgressChanged(position, duration)
    }

    // ==================== 时间格式化 ====================

    /**
     * 格式化时间为 HH:mm:ss 或 mm:ss 格式
     *
     * @param timeMs 时间（毫秒）
     * @return 格式化的时间字符串
     */
    fun formatTime(timeMs: Long): String {
        if (timeMs < 0) return "00:00:00"

        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    // ==================== 标题提取 ====================

    /**
     * 从 URL 或路径中提取标题
     *
     * 提取规则：
     * - 取最后一个 "/" 后面的内容
     * - 移除文件扩展名
     * - 如果是纯文件名则直接返回
     *
     * @param url URL 或文件路径
     * @return 提取的标题
     */
    fun extractTitle(url: String): String {
        if (url.isBlank()) return "未知视频"

        return try {
            when {
                url.contains("/") -> {
                    val path = url.substringAfterLast("/")
                    path.substringBeforeLast(".")
                        .ifBlank { "未知视频" }
                }
                else -> url
            }
        } catch (e: Exception) {
            Log.d(TAG, "extractTitle failed: ${e.message}")
            "未知视频"
        }
    }

    // ==================== 播放/暂停按钮 ====================

    /**
     * 更新播放/暂停图标
     */
    protected fun updatePlayPauseIcon() {
        val iconRes = if (videoPlayer.isPlaying) {
            R.drawable.ic_sample_video_player_view_pause
        } else {
            R.drawable.ic_sample_video_player_view_play
        }
        binding.ivPlayPause.setImageResource(iconRes)
    }

    /**
     * 切换播放/暂停状态
     */
    fun togglePlayPause() {
        if (videoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }

        listener?.onPlayPauseChanged(videoPlayer.isPlaying)
    }

    // ==================== 倍速切换 ====================

    /**
     * 切换播放速度
     *
     * 循环切换：0.5x → 1.0x → 1.5x → 2.0x → 2.5x → 3.0x → 0.5x
     */
    fun switchSpeed() {
        currentSpeedIndex = (currentSpeedIndex + 1) % SPEED_LIST.size
        val newSpeed = SPEED_LIST[currentSpeedIndex]

        videoPlayer.speed = newSpeed
        binding.tvSpeed.text = "${newSpeed}x"

        Log.d(TAG, "speed changed to: ${newSpeed}x")
        listener?.onSpeedChanged(newSpeed)
    }

    // ==================== 缩放模式切换 ====================

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

        Log.d(TAG, "scale mode changed to: $newMode")
        listener?.onScaleModeChanged(newMode)
    }

    /**
     * 更新缩放模式图标
     */
    protected fun updateScaleModeIcon(mode: VideoScaleMode) {
        val iconRes = when (mode) {
            VideoScaleMode.FIT_CENTER -> R.drawable.ic_crop_free
            VideoScaleMode.CROP_CENTER -> R.drawable.ic_crop_free  // 可替换为裁剪图标
            VideoScaleMode.STRETCH -> R.drawable.ic_crop_free  // 可替换为拉伸图标
        }
        binding.ivScaleMode.setImageResource(iconRes)
    }

    // ==================== 事件监听设置 ====================

    /**
     * 设置所有事件监听器
     */
    protected fun setupListeners() {
        setupButtonListeners()
        setupSeekBarListener()
        setupTouchListener()
    }

    /**
     * 设置按钮点击监听器
     */
    protected fun setupButtonListeners() {
        binding.apply {
            // 返回按钮
            ivBack.setOnClickListener {
                listener?.onBackClicked()
            }

            // 播放/暂停按钮
            ivPlayPause.setOnClickListener {
                togglePlayPause()
            }

            // 缩放模式按钮
            ivScaleMode.setOnClickListener {
                switchScaleMode()
            }

            // 倍速按钮
            tvSpeed.setOnClickListener {
                switchSpeed()
            }
        }
    }

    /**
     * 设置 SeekBar 监听器
     */
    protected fun setupSeekBarListener() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || videoPlayer.duration <= 0) return

                val position = (progress.toFloat() / 100 * videoPlayer.duration).toLong()
                binding.tvProgress.text = formatTime(position)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
                cancelAutoHideTimer()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false

                val position = (seekBar!!.progress.toFloat() / 100 * videoPlayer.duration).toLong()
                videoPlayer.seekTo(position)

                startAutoHideTimer()
                listener?.onProgressChanged(position, videoPlayer.duration)
            }
        })
    }

    /**
     * 设置触摸监听器（用于手势识别）
     */
    protected fun setupTouchListener() {
        // 使用 dispatchTouchEvent 拦截所有触摸事件
        this.setOnTouchListener { _, event ->
            handleTouchEvent(event)
            true
        }
    }

    // ==================== 手势处理核心逻辑 ====================

    /**
     * 处理触摸事件
     *
     * 手势识别规则：
     * - 左侧 1/4 区域：上下滑动调整**音量**
     * - 右侧 1/4 区域：上下滑动调整**亮度**
     * - 底部 1/4 区域：左右滑动调整**进度**（实时 seekTo）
     * - 其他区域：单击显示/隐藏控制栏
     */
    protected fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onTouchDown(event)
            MotionEvent.ACTION_MOVE -> onTouchMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onTouchUp(event)
        }
        return true
    }

    /**
     * 触摸按下处理
     */
    protected fun onTouchDown(event: MotionEvent) {
        touchStartX = event.x
        touchStartY = event.y
        touchLastX = event.x
        touchLastY = event.y
        isGestureTriggered = false
        currentGestureType = GestureType.NONE

        // 记录手势开始时的播放位置
        gestureStartPosition = videoPlayer.currentPosition
        gestureTargetPosition = gestureStartPosition

        listener?.onTouchDown()
        cancelAutoHideTimer()
    }

    /**
     * 触摸移动处理
     */
    protected fun onTouchMove(event: MotionEvent) {
        val deltaX = event.x - touchStartX
        val deltaY = event.y - touchStartY
        val width = width.toFloat()
        val height = height.toFloat()

        // 检查是否达到手势阈值
        if (!isGestureTriggered) {
            val threshold = GESTURE_THRESHOLD_DP * resources.displayMetrics.density
            if (Math.abs(deltaX) < threshold && Math.abs(deltaY) < threshold) {
                return
            }
            isGestureTriggered = true
        }

        // 确定手势类型（只在首次触发时判断）
        if (currentGestureType == GestureType.NONE) {
            currentGestureType = determineGestureType(touchStartX, touchStartY, width, height)

            // 显示对应的手势提示 UI
            when (currentGestureType) {
                GestureType.VOLUME -> showGestureVolumeUI()
                GestureType.BRIGHTNESS -> showGestureBrightnessUI()
                GestureType.SEEK -> showGestureSeekUI()
                GestureType.NONE -> {}
            }
        }

        // 执行对应的手势操作
        when (currentGestureType) {
            GestureType.VOLUME -> handleVolumeGesture(event.y - touchLastY)
            GestureType.BRIGHTNESS -> handleBrightnessGesture(event.y - touchLastY)
            GestureType.SEEK -> handleSeekGesture(event.x - touchLastX)
            GestureType.NONE -> {}
        }

        // 更新上次触摸位置
        touchLastX = event.x
        touchLastY = event.y
    }

    /**
     * 触摸抬起处理
     */
    protected fun onTouchUp(event: MotionEvent) {
        isGestureProcessing = false

        if (!isGestureTriggered) {
            // 未触发手势，视为点击事件
            toggleControl()
        } else {
            // 手势结束，隐藏提示 UI
            hideAllGestureUI()

            // 如果是进度手势，执行最终的 seekTo
            if (currentGestureType == GestureType.SEEK) {
                videoPlayer.seekTo(gestureTargetPosition)
                updateProgressUI()
            }
        }

        currentGestureType = GestureType.NONE
        listener?.onTouchUp()

        if (isControlVisible) {
            startAutoHideTimer()
        }
    }

    /**
     * 确定手势类型
     *
     * @param x 触摸点 X 坐标
     * @param y 触摸点 Y 坐标
     * @param width 视图宽度
     * @param height 视图高度
     * @return 手势类型
     */
    protected fun determineGestureType(x: Float, y: Float, width: Float, height: Float): GestureType {
        val leftArea = width * GESTURE_AREA_RATIO
        val rightArea = width * (1 - GESTURE_AREA_RATIO)
        val bottomArea = height * (1 - GESTURE_AREA_RATIO)

        return when {
            x < leftArea && config.enableVolumeGesture -> GestureType.VOLUME
            x > rightArea && config.enableBrightnessGesture -> GestureType.BRIGHTNESS
            y > bottomArea && config.enableSeekGesture -> GestureType.SEEK
            else -> GestureType.NONE
        }
    }

    // ==================== 音量手势 ====================

    /**
     * 处理音量调节手势
     *
     * @param deltaY Y 轴变化量（向上为负，向下为正）
     */
    protected fun handleVolumeGesture(deltaY: Float) {
        isGestureProcessing = true

        // 向上滑动增加音量，向下滑动减小音量
        val sensitivity = 0.05f  // 灵敏度系数
        val deltaVolume = -(deltaY / height) * sensitivity
        val newVolume = (videoPlayer.volume + deltaVolume).coerceIn(0f, 1f)

        videoPlayer.volume = newVolume

        // 更新音量提示 UI
        binding.volumeProgressBar.progress = (newVolume * 100).toInt()
        binding.ivVolumeIcon.setImageResource(
            if (newVolume > 0.5f) R.drawable.ic_volume_up else R.drawable.ic_volume_up
        )
    }

    /**
     * 显示音量手势提示 UI
     */
    protected fun showGestureVolumeUI() {
        binding.gestureVolumeContainer.apply {
            visibility = View.VISIBLE
            alpha = 1f
        }
        binding.volumeProgressBar.progress = (videoPlayer.volume * 100).toInt()
    }

    // ==================== 亮度手势 ====================

    /**
     * 处理亮度调节手势
     *
     * @param deltaY Y 轴变化量（向上为负，向下为正）
     */
    protected fun handleBrightnessGesture(deltaY: Float) {
        isGestureProcessing = true

        // 向上滑动增加亮度，向下滑动减小亮度
        val sensitivity = 0.03f  // 灵敏度系数
        val deltaBrightness = -(deltaY / height) * sensitivity

        // 获取当前窗口亮度
        val window = (context as? android.app.Activity)?.window ?: return
        val layoutParams = window.attributes
        var brightness = layoutParams.screenBrightness

        // 如果未手动设置过亮度，默认值可能是 -1（系统默认）
        if (brightness < 0) {
            brightness = 0.5f  // 假设默认亮度为 50%
        }

        // 计算新的亮度值（范围 0.01 ~ 1.0）
        val newBrightness = (brightness + deltaBrightness).coerceIn(0.01f, 1f)

        // 应用新的亮度
        layoutParams.screenBrightness = newBrightness
        window.attributes = layoutParams

        // 更新亮度提示 UI
        binding.brightnessProgressBar.progress = (newBrightness * 100).toInt()
    }

    /**
     * 显示亮度手势提示 UI
     */
    protected fun showGestureBrightnessUI() {
        binding.gestureBrightnessContainer.apply {
            visibility = View.VISIBLE
            alpha = 1f
        }

        // 获取当前亮度
        val window = (context as? android.app.Activity)?.window
        val brightness = window?.attributes?.screenBrightness ?: 0.5f
        binding.brightnessProgressBar.progress = ((if (brightness < 0) 0.5f else brightness) * 100).toInt()
    }

    // ==================== 进度手势（实时 seekTo）====================

    /**
     * 处理进度调节手势（实时 seekTo）
     *
     * @param deltaX X 轴变化量（向左为负，向右为正）
     */
    protected fun handleSeekGesture(deltaX: Float) {
        isGestureProcessing = true

        val duration = videoPlayer.duration
        if (duration <= 0) return

        // 根据滑动距离计算时间偏移（每滑动屏幕宽度的 1% = 1 秒）
        val pixelsPerSecond = width * 0.01f
        val deltaTimeMs = ((deltaX / pixelsPerSecond) * 1000).toLong()

        // 计算目标位置（限制在有效范围内）
        gestureTargetPosition = (gestureStartPosition + deltaTimeMs).coerceIn(0, duration)

        // **关键：实时调用 seekTo**
        videoPlayer.seekTo(gestureTargetPosition)

        // 更新进度提示 UI
        val diffSeconds = (gestureTargetPosition - gestureStartPosition) / 1000
        val prefix = if (diffSeconds >= 0) "+" else ""
        binding.tvGestureSeekTime.text = "$prefix${formatDiffTime(Math.abs(diffSeconds))}"

        // 同步更新进度条和时间文本
        updateSeekProgressUI(gestureTargetPosition, duration)
    }

    /**
     * 显示进度手势提示 UI
     */
    protected fun showGestureSeekUI() {
        binding.tvGestureSeekTime.apply {
            visibility = View.VISIBLE
            alpha = 1f
            text = "+00:00"
        }
    }

    /**
     * 更新手势过程中的进度 UI
     */
    protected fun updateSeekProgressUI(position: Long, duration: Long) {
        if (duration > 0) {
            binding.seekBar.progress = ((position.toFloat() / duration) * 100).toInt()
        }
        binding.tvProgress.text = formatTime(position)
    }

    /**
     * 格式化时间差（用于快进/快退提示）
     *
     * @param seconds 秒数
     * @return 格式化的字符串（如 "+01:30" 或 "-00:45"）
     */
    protected fun formatDiffTime(seconds: Long): String {
        val absSeconds = Math.abs(seconds)
        val minutes = absSeconds / 60
        val secs = absSeconds % 60

        return if (minutes > 0) {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        } else {
            String.format(Locale.getDefault(), "00:%02d", secs)
        }
    }

    // ==================== 手势 UI 管理 ====================

    /**
     * 隐藏所有手势提示 UI
     */
    protected fun hideAllGestureUI() {
        binding.gestureVolumeContainer.visibility = View.GONE
        binding.gestureBrightnessContainer.visibility = View.GONE
        binding.tvGestureSeekTime.visibility = View.GONE
    }

    // ==================== 生命周期管理 ====================

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}
