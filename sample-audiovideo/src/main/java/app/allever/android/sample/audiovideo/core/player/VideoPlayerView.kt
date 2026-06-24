package app.allever.android.sample.audiovideo.core.player

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
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
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import java.util.Locale
import kotlin.math.abs

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

    /** 循环模式图标映射 */
    private val LOOP_MODE_LIST = arrayOf(
        LoopMode.NONE,
        LoopMode.SINGLE,
        LoopMode.ALL
    )

    /** 当前循环模式 */
    private var currentLoopModeIndex = 0 // 默认 NONE

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

    // 双击相关变量
    /** 上次点击时间（毫秒）*/
    private var lastClickTime: Long = 0

    /** 双击时间阈值（毫秒）- 300ms 内的两次点击视为双击 */
    private val DOUBLE_CLICK_TIME_THRESHOLD_MS = 300L

    /** 双击位置阈值（像素）- 两次点击位置差值在此范围内视为双击 */
    private val DOUBLE_CLICK_DISTANCE_THRESHOLD_PX = 50f

    /** 上次点击的 X 坐标 */
    private var lastClickX: Float = 0f

    /** 上次点击的 Y 坐标 */
    private var lastClickY: Float = 0f

    // 长按变速相关变量
    /** 是否正在长按加速 */
    private var isLongPressSpeeding: Boolean = false

    /** 长按前的原始速度 */
    private var speedBeforeLongPress: Float = 1f

    /** 长按开始时间（毫秒）*/
    private var longPressStartTime: Long = 0

    /** 长按触发阈值（毫秒）- 按住 500ms 后触发长按加速 */
    private val LONG_PRESS_TIME_THRESHOLD_MS = 500L

    /** 长按是否已触发（避免重复触发）*/
    private var isLongPressTriggered: Boolean = false

    // 控制层自动隐藏相关变量
    /** 控制层自动隐藏延迟时间（毫秒）- 5秒 */
    private val AUTO_HIDE_DELAY_MS = 5000L

    /** 自动隐藏的 Handler */
    private val autoHideHandler = android.os.Handler(Looper.getMainLooper())

    /** 自动隐藏任务 */
    private val autoHideRunnable = Runnable {
        appendLog("控制层: 5秒无操作，自动隐藏")
        showOrHideControlPanel(false)
    }

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

    /***
     * 设置网络资源
     *
     * @param url 视频网络地址（缓存功能已在 VideoPlayer 内部自动处理）
     */
    fun setSource(url: String) {
        tvTitle.text = extractTitle(Uri.parse(url))
        videoPlayer.setSource(url)
        appendLog("setSource: $url")
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

    fun setAssetSource(path: String) {
        // 提取标题并显示（支持 assets 路径格式）
        tvTitle.text = extractAssetTitle(path)
        videoPlayer.setAssetSource(path)
        appendLog("setAssetSource: $path")
    }

    /**
     * 从 Assets 路径中提取标题
     *
     * 支持格式：
     * - "sample.mp4" → "sample"
     * - "videos/test_video.mp4" → "test video"
     *
     * @param path Assets 文件路径
     * @return 提取的标题
     */
    private fun extractAssetTitle(path: String): String {
        // 获取路径的最后一部分作为文件名
        val fileName = when {
            path.contains("/") -> path.substringAfterLast("/")
            path.contains("\\") -> path.substringAfterLast("\\")
            else -> path
        }

        // 清理文件名：去除扩展名，美化显示
        return fileName
            .substringBeforeLast(".")  // 去除扩展名
            .replace("_", " ")       // 下划线转空格
            .replace("-", " ")       // 连字符转空格
            .trim()
            .ifEmpty { "视频" }      // 如果为空，使用默认标题
    }

    /**
     * 从 Uri 或路径中提取标题（文件名）
     *
     * 支持的格式：
     * - URL: https://example.com/video.mp4 → "video"
     * - 文件路径: /sdcard/Movies/my_video.mp4 → "my video"
     * - Assets: assets://sample.mp4 或 sample.mp4 → "sample"
     * - Content URI: content://media/... → 查询 MediaStore 获取原始文件名
     *
     * @param uri 视频 Uri
     * @return 提取的标题（去除扩展名，替换下划线和连字符为空格）
     */
    private fun extractTitle(uri: Uri): String {
        val source = uri.toString()

        // 优先处理 Content URI（如从相册、文件选择器获取的 URI）
        if (source.startsWith("content://")) {
            val fileNameFromContent = queryFileNameFromContentUri(uri)
            if (fileNameFromContent != null) {
                return cleanFileName(fileNameFromContent)
            }
        }

        // 尝试从 Uri 的 lastPathSegment 提取
        val fileName = uri.lastPathSegment

        // 如果 lastPathSegment 为空，尝试从完整路径提取
        val name = if (fileName.isNullOrEmpty()) {
            // 处理各种路径格式
            when {
                source.contains("/") -> source.substringAfterLast("/")
                source.contains("\\") -> source.substringAfterLast("\\")
                else -> source
            }
        } else {
            fileName
        }

        return cleanFileName(name)
    }

    /**
     * 从 Content URI 查询原始文件名
     *
     * 通过 ContentResolver 查询 MediaStore 的 DISPLAY_NAME 字段，
     * 获取文件的真正名称（而非 ID）。
     *
     * 适用于：
     * - content://media/external/video/media/123
     * - content://com.android.providers.media.documents/document/video%3A123
     * - 其他 Content Provider 提供的 URI
     *
     * @param uri Content URI
     * @return 文件名，如果查询失败返回 null
     */
    private fun queryFileNameFromContentUri(uri: Uri): String? {
        return try {
            var result: String? = null

            // 尝试直接从 URI 获取 DISPLAY_NAME
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }

            // 如果第一次查询失败，尝试通过 MediaStore Video 查询
            if (result == null || result!!.isEmpty()) {
                val projection = arrayOf(
                    android.provider.MediaStore.Video.Media.DISPLAY_NAME
                )

                context.contentResolver.query(
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    "${android.provider.MediaStore.Video.Media._ID} = ?",
                    arrayOf(uri.lastPathSegment),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            result = cursor.getString(nameIndex)
                        }
                    }
                }
            }

            result

        } catch (e: Exception) {
            log(TAG, "queryFileNameFromContentUri error: ${e.message}")
            null
        }
    }

    /**
     * 清理文件名：去除扩展名，美化显示
     *
     * @param fileName 原始文件名
     * @return 清理后的标题
     */
    private fun cleanFileName(fileName: String): String {
        return fileName
            .substringBeforeLast(".")  // 去除扩展名
            .replace("_", " ")       // 下划线转空格
            .replace("-", " ")       // 连字符转空格
            .trim()
            .ifEmpty { "视频" }      // 如果为空，使用默认标题
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

        // 注意：touchInterceptView 不设置 onClickListener
        // 因为 onClick 和 onTouch 会冲突（onTouch 返回 true 后 onClick 不会触发）
        // 点击事件在 onTouchUp 中处理（当没有执行手势操作时）

        // 设置触摸手势监听
        binding.touchInterceptView.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        // 循环模式按钮
        binding.ivVPLoopMode.setOnClickListener {
            switchLoopMode()
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
            VideoScaleMode.FIT_CENTER -> R.drawable.ic_scale_fit
            VideoScaleMode.CROP_CENTER -> R.drawable.ic_scale_crop
            VideoScaleMode.STRETCH -> R.drawable.ic_scale_stretch
        }
        binding.ivVPScaleMode.setImageResource(iconRes)
    }

    /**
     * 切换循环模式
     *
     * 循环切换：ALL → SINGLE → NONE → ALL
     */
    private fun switchLoopMode() {
        currentLoopModeIndex = (currentLoopModeIndex + 1) % LOOP_MODE_LIST.size
        val newMode = LOOP_MODE_LIST[currentLoopModeIndex]

        videoPlayer.loopMode = newMode

        // 更新图标
        updateLoopModeIcon(newMode)

        appendLog("循环模式切换为: ${newMode.name}")
    }

    /**
     * 更新缩放模式图标
     */
    private fun updateLoopModeIcon(mode: LoopMode) {
        val iconRes = when (mode) {
            LoopMode.NONE -> R.drawable.ic_loop_none
            LoopMode.SINGLE -> R.drawable.ic_loop_single
            LoopMode.ALL -> R.drawable.ic_loop_all
        }
        binding.ivVPLoopMode.setImageResource(iconRes)
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
        showOrHideControlPanel(!isVisible)
    }

    private fun showOrHideControlPanel(show: Boolean) {
        binding.controlPanel.isVisible = show
        listener?.onControlVisibilityChanged(show)

        // ★ 控制层显示时启动自动隐藏计时器，5秒后无操作则自动隐藏
        if (show) {
            startAutoHideTimer()
        } else {
            stopAutoHideTimer()
        }
    }

    /**
     * 启动控制层自动隐藏计时器（5秒后无操作则隐藏）
     */
    private fun startAutoHideTimer() {
        // 先停止之前的计时器（避免重复）
        stopAutoHideTimer()
        // 启动新的计时器
        autoHideHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS)
    }

    /**
     * 停止控制层自动隐藏计时器
     */
    private fun stopAutoHideTimer() {
        autoHideHandler.removeCallbacks(autoHideRunnable)
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

        // 初始化长按检测状态
        longPressStartTime = System.currentTimeMillis()
        isLongPressTriggered = false

        // 注意：不要在这里强制显示控制栏
        // 否则会导致：按下时显示 → 抬起时 toggleControlVisibility() 又切换隐藏 → 闪烁
        // 控制栏的显示/隐藏只由点击事件（在 onTouchUp 中处理）决定
    }

    /**
     * 触摸移动事件
     */
    private fun onTouchMove(event: MotionEvent) {
        // ★ 如果正在长按加速，禁止所有手势操作（音量/亮度/进度）
        if (isLongPressSpeeding) {
            return
        }

        val deltaY = event.y - gestureLastY
        val deltaX = event.x - gestureLastX
        val totalDeltaY = abs(event.y - gestureStartY)
        val totalDeltaX = abs(event.x - gestureStartX)

        // 如果还未确定手势类型，根据滑动方向和位置判断
        if (!isGestureProcessing) {
            // 检查是否超过手势阈值
            if (totalDeltaY < gestureThresholdPx && totalDeltaX < gestureThresholdPx) {
                // 未超过手势阈值，检查是否触发长按加速
                checkLongPressSpeed()
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
        // 检查是否正在长按加速，如果是则恢复原速度
        if (isLongPressSpeeding) {
            endLongPressSpeed()
            // 长按结束后不触发其他事件
            isGestureProcessing = false
            gestureType = null
            return
        }

        if (!isGestureProcessing) {
            // 没有执行手势操作，说明这是一个点击事件
            // 检测是否为双击
            if (checkDoubleClick()) {
                // 符合双击条件
                handleDoubleClick()
                showOrHideControlPanel(false)
                return
            }

            // 单击：切换控制栏可见性（替代 OnClickListener）
            toggleControlVisibility()
            return
        }

        // 隐藏手势提示浮层
        hideAllGestureOverlays()

        isGestureProcessing = false
        gestureType = null
    }

    /**
     * 检测并处理双击事件
     *
     * 双击判断条件：
     * 1. 两次点击时间间隔 < DOUBLE_CLICK_TIME_THRESHOLD_MS (300ms)
     * 2. 两次点击位置距离 < DOUBLE_CLICK_DISTANCE_THRESHOLD_PX (50px)
     *
     * @return true 表示是双击事件且已处理；false 表示不是双击
     */
    private fun checkDoubleClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        val clickX = gestureStartX  // 使用 onTouchDown 中记录的起始位置作为点击位置
        val clickY = gestureStartY

        // 检查是否在双击时间阈值内
        val timeDiff = currentTime - lastClickTime
        if (timeDiff < DOUBLE_CLICK_TIME_THRESHOLD_MS && lastClickTime > 0) {
            // 检查是否在双击位置阈值内
            val distance = kotlin.math.sqrt(
                (clickX - lastClickX) * (clickX - lastClickX) +
                        (clickY - lastClickY) * (clickY - lastClickY)
            )

            if (distance < DOUBLE_CLICK_DISTANCE_THRESHOLD_PX) {
                // 重置上次点击时间，避免三击被误判为两次双击
                lastClickTime = 0
                return true
            }
        }

        // 更新上次点击信息
        lastClickTime = currentTime
        lastClickX = clickX
        lastClickY = clickY

        return false
    }

    /**
     * 处理双击事件：切换播放/暂停状态
     */
    private fun handleDoubleClick() {
        appendLog("双击: 切换播放/暂停")

        if (videoPlayer.isPlaying) {
            videoPlayer.pause()
            appendLog("双击: 暂停播放")
        } else {
            videoPlayer.play()
            appendLog("双击: 开始播放")
        }

        // 更新按钮状态
        updateButtonStates()
        listener?.onPlayPauseChanged(videoPlayer.isPlaying)
    }

    // ==================== 长按变速功能 ====================

    /**
     * 检测是否触发长按加速
     *
     * 长按加速条件：
     * 1. 按住时间 > LONG_PRESS_TIME_THRESHOLD_MS (500ms)
     * 2. 未触发过手势操作（手指未移动超过阈值）
     * 3. 未触发过长按加速（避免重复触发）
     */
    private fun checkLongPressSpeed() {
        // 如果已经触发过长按或正在长按加速中，不再检测
        if (isLongPressTriggered || isLongPressSpeeding) {
            return
        }

        val currentTime = System.currentTimeMillis()
        val pressDuration = currentTime - longPressStartTime

        // 检查是否达到长按阈值
        if (pressDuration >= LONG_PRESS_TIME_THRESHOLD_MS) {
            startLongPressSpeed()
        }
    }

    /**
     * 开始长按加速：在当前速度基础上 +1 倍速
     *
     * 示例：
     * - 原速度 0.5x → 加速后 1.5x
     * - 原速度 1.0x → 加速后 2.0x
     * - 原速度 1.5x → 加速后 2.5x
     * - 原速度 3.0x → 加速后 4.0x（如果支持的话）
     */
    private fun startLongPressSpeed() {
        isLongPressTriggered = true
        isLongPressSpeeding = true

        // 记录当前速度作为原始速度
        speedBeforeLongPress = videoPlayer.speed

        // 计算加速后的速度（+1 倍速）
        val acceleratedSpeed = speedBeforeLongPress + 1f

        // 应用加速后的速度
        videoPlayer.speed = acceleratedSpeed

        appendLog("长按加速: ${speedBeforeLongPress}x → ${acceleratedSpeed}x")

        // 更新 UI 显示当前速度（可选）
        listener?.onSpeedChanged(acceleratedSpeed)
    }

    /**
     * 结束长按加速：恢复到原来的速度
     *
     * 当用户松开手指时调用，将播放器速度恢复到长按前的值。
     */
    private fun endLongPressSpeed() {
        if (!isLongPressSpeeding) {
            return
        }

        // 恢复到原始速度
        videoPlayer.speed = speedBeforeLongPress

        appendLog("长按结束: 恢复原速度 ${speedBeforeLongPress}x")

        // 更新 UI 显示原速度
        listener?.onSpeedChanged(speedBeforeLongPress)

        // 重置状态
        isLongPressSpeeding = false
        isLongPressTriggered = false
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
        binding.gestureVolumeContainer.visibility = VISIBLE
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
        binding.gestureBrightnessContainer.visibility = VISIBLE
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
        binding.tvGestureSeekTime.visibility = VISIBLE
        binding.tvGestureSeekTime.alpha = 1f

        // 计算时间差
        val diffMs = currentPosition - startPosition
        val diffText = if (diffMs >= 0) "+${formatTime(diffMs)}" else "-${formatTime(abs(diffMs))}"

        // 显示格式：时间差\n当前时间
        binding.tvGestureSeekTime.text = "$diffText\n${formatTime(currentPosition)}"
    }

    /**
     * 隐藏所有手势提示浮层（带淡出动画）
     */
    private fun hideAllGestureOverlays() {
        binding.gestureVolumeContainer.animate().alpha(0f).withEndAction {
            binding.gestureVolumeContainer.visibility = GONE
        }.start()

        binding.gestureBrightnessContainer.animate().alpha(0f).withEndAction {
            binding.gestureBrightnessContainer.visibility = GONE
        }.start()

        binding.tvGestureSeekTime.animate().alpha(0f).withEndAction {
            binding.tvGestureSeekTime.visibility = GONE
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
        val window = (context as? Activity)?.window ?: return
        window.attributes = window.attributes.apply {
            screenBrightness = brightness.coerceIn(0.01f, 1f)
        }
    }

    fun appendLog(msg: String) {
        listener?.onLog(msg)
        log(TAG, msg)
    }

}
