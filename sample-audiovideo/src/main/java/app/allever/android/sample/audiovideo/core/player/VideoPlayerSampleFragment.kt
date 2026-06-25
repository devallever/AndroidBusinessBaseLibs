package app.allever.android.sample.audiovideo.core.player

import android.view.ViewGroup
import android.widget.SeekBar
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.player.core.IVideoPlayerListener
import app.allever.android.lib.player.core.LoopMode
import app.allever.android.sample.audiovideo.VideoSource
import app.allever.android.lib.player.core.PlayerState
import app.allever.android.lib.player.core.VideoScaleMode
import app.allever.android.lib.player.core.engine.MediaPlayerEngine
import app.allever.android.lib.player.core.engine.ijk.IjkPlayerEngine
import app.allever.android.lib.player.core.engine.media3.ExoPlayerViewRender
import app.allever.android.lib.player.core.engine.media3.Media3PlayerEngine
import app.allever.android.lib.player.core.player.IVideoPlayerViewListener
import app.allever.android.lib.player.core.player.VideoPlayer
import app.allever.android.lib.player.core.render.RenderRegistry
import app.allever.android.lib.player.core.render.SurfaceViewRender
import app.allever.android.lib.player.core.render.TextureViewRender
import app.allever.android.lib.player.core.render.VideoViewRender
import app.allever.android.sample.audiovideo.databinding.FragmentVideoPlayerSampleBinding

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * VideoPlayer 示例 Fragment（新架构：引擎与渲染分离）
 *
 * ## 演示内容
 * 1. **组合模式**：展示 Engine + Render 的灵活组合
 * 2. **渲染器切换**：运行时切换 SurfaceView/TextureView/VideoView
 * 3. **完整功能**：播放控制、进度追踪、变速、音量等
 * 4. **多种数据源**：URL、本地文件、Assets
 *
 * ## 架构说明
 * ```
 * VideoPlayer (协调器)
 * ├── IPlayerEngine: MediaPlayerEngine (播放逻辑)
 * └── IVideoRender: SurfaceViewRender/TextureViewRender/VideoViewRender (渲染)
 * ```
 *
 * @see VideoPlayer 新架构协调器
 */
class VideoPlayerSampleFragment :
    BaseFragment<FragmentVideoPlayerSampleBinding, BaseViewModel>() {

    /** 播放器实例（使用新架构）*/
    private lateinit var player: VideoPlayer

    /** 当前使用的渲染器名称 */
    private var currentRenderName: String = "SurfaceView"

    /** 当前使用的引擎类型 */
    private enum class EngineType {
        MEDIA_PLAYER,   // Android MediaPlayer
        MEDIA3,         // Google Media3 (ExoPlayer)
        IJK_PLAYER      // Bilibili IJKPlayer
    }

    /** 当前引擎类型 */
    private var currentEngineType: EngineType = EngineType.MEDIA_PLAYER

    /** 视频选择器 */
    private val videoPickerLauncher = MediaPickerCore.registerPickerLauncher(this) { items ->
        items.firstOrNull()?.let { mediaItem ->
            if (mediaItem is MediaItem.Video) {
                mBinding.etUrl.setText(mediaItem.uri.toString())
                appendLog("选择本地视频: ${mediaItem.name} (${mediaItem.uri})")
                autoPlayOnPrepared = true
                player.setSource(mediaItem.uri)
            }
        }
    }

    private var isUserSeeking = false

    /** 默认测试视频 URL */
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    override fun inflate(): FragmentVideoPlayerSampleBinding =
        FragmentVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initVideoPicker()
        initPlayer()
        initViews()
        updateStateUI(PlayerState.IDLE)
        updateButtonStates()
    }

    /**
     * 初始化视频选择器
     */
    private fun initVideoPicker() {
        // 视频选择器配置
    }

    /**
     * 初始化播放器（使用新架构的组合模式）
     */
    private fun initPlayer() {
        // 使用默认配置：MediaPlayerEngine + SurfaceViewRender
        player = VideoPlayer(
            engine = MediaPlayerEngine(),
            render = SurfaceViewRender()
        ).apply {
            attach(mBinding.containerVideo)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }

        appendLog("初始化播放器: MediaPlayerEngine + SurfaceViewRender")
        updateArchInfo()
    }

    /**
     * 初始化视图和事件监听
     */
    private fun initViews() {
        // ==================== 播放控制按钮 ====================

        // 播放/继续按钮
        mBinding.btnPlay.setOnClickListener {
            when (player.state) {
                PlayerState.PAUSED -> {
                    player.play()
                    appendLog("继续播放")
                }
                else -> {
                    // 播放网络视频
                    mBinding.btnPlayNet.setOnClickListener {
                        val url = mBinding.etUrl.text.toString().trim()
                        val sourceUrl = url.ifEmpty {
                            VideoSource.testUrlList.random()
                        }
                        autoPlayOnPrepared = true
                        player.setSource(sourceUrl)
                        appendLog("设置数据源: ${mBinding.etUrl.text.ifEmpty { sourceUrl }}")
                    }
                }
            }
        }

        // 暂停按钮
        mBinding.btnPause.setOnClickListener {
            player.pause()
            appendLog("暂停播放")
        }

        // 停止按钮
        mBinding.btnStop.setOnClickListener {
            player.stop()
            appendLog("停止播放")
            resetProgressUI()
        }

        // 选择本地视频
        mBinding.btnPickLocal.setOnClickListener {
            MediaPickerCore.launchVideo(videoPickerLauncher)
        }

        // 播放网络视频
        mBinding.btnPlayNet.setOnClickListener {
            mBinding.btnPlay.performClick()
        }

        // 播放 Assets 文件
        mBinding.btnPlayAsset.setOnClickListener {
            val assetPath = mBinding.etAssetPath.text.toString().trim()
            if (assetPath.isNotEmpty()) {
                autoPlayOnPrepared = true
                player.setAssetSource(assetPath)
                appendLog("播放 Assets 文件: $assetPath")
            } else {
                appendLog("请输入 Assets 文件路径")
            }
        }

        // ==================== 渲染器切换按钮（新架构特性）====================

        // 切换到 SurfaceView
        mBinding.btnSwitchSurfaceView.setOnClickListener {
            switchRender(SurfaceViewRender.NAME)
        }

        // 切换到 TextureView
        mBinding.btnSwitchTextureView.setOnClickListener {
            switchRender( TextureViewRender.NAME)
        }

        // 切换到 VideoView
        mBinding.btnSwitchVideoView.setOnClickListener {
            switchRender( VideoViewRender.NAME)
        }

        // 切换到 PlayerView (ExoPlayer)
        mBinding.btnSwitchPlayerView.setOnClickListener {
            if (currentEngineType != EngineType.MEDIA3) {
                toast("请先切换到 ExoPlayer")
                return@setOnClickListener
            }
            switchRender(ExoPlayerViewRender.NAME)
        }

        // ==================== 引擎切换按钮（新架构核心功能演示）====================

        // 切换到 MediaPlayer
        mBinding.btnSwitchMediaPlayer.setOnClickListener {
            if(currentRenderName == ExoPlayerViewRender.NAME) {
                toast("请先切换到其他渲染")
                return@setOnClickListener
            }
            switchEngine(EngineType.MEDIA_PLAYER)
        }

        // 切换到 Media3 (ExoPlayer)
        mBinding.btnSwitchMedia3.setOnClickListener {
            switchEngine(EngineType.MEDIA3)
        }

        // 切换到 IJKPlayer
        mBinding.btnSwitchIjkPlayer.setOnClickListener {
            if(currentRenderName == ExoPlayerViewRender.NAME) {
                toast("请先切换到其他渲染")
                return@setOnClickListener
            }
            switchEngine(EngineType.IJK_PLAYER)
        }

        // ==================== 进度条控制 ====================

        mBinding.seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && player.duration > 0) {
                    val position = (progress.toFloat() / 100 * player.duration).toLong()
                    mBinding.tvProgress.text = "${formatTime(position)} / ${formatTime(player.duration)}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (player.duration > 0 && seekBar != null) {
                    val position = (seekBar.progress.toFloat() / 100 * player.duration).toLong()
                    player.seekTo(position)
                    appendLog("跳转到: ${formatTime(position)}")
                }
                mBinding.seekBarProgress.post { isUserSeeking = false }
            }
        })

        // ==================== 变速控制 ====================

        mBinding.seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = 0.5f + (progress.toFloat() / 50 * 2.5f)
                mBinding.tvSpeed.text = String.format(Locale.US, "%.1fx", speed)
                if (fromUser) {
                    player.speed = speed
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 设置初始速度为 1.0x
        mBinding.seekBarSpeed.progress = 10

        // ==================== 音量控制 ====================

        mBinding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress.toFloat() / 100
                mBinding.tvVolume.text = String.format(Locale.US, "%.0f%%", volume * 100)
                if (fromUser) {
                    player.volume = volume
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 设置初始音量为 100%
        mBinding.seekBarVolume.progress = 100

        // ==================== 缩放模式切换 ====================
        mBinding.radioGroupScaleMode?.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                mBinding.rbScaleFitCenter.id -> VideoScaleMode.FIT_CENTER
                mBinding.rbScaleCropCenter.id -> VideoScaleMode.CROP_CENTER
                mBinding.rbScaleStretch.id -> VideoScaleMode.STRETCH
                else -> VideoScaleMode.FIT_CENTER
            }
            player.videoScaleMode = mode
            appendLog("切换缩放模式: $mode")
        }

        // ==================== 循环模式切换 ====================
        mBinding.radioGroupLoop?.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                mBinding.rbLoopNone.id -> LoopMode.NONE
                mBinding.rbLoopSingle.id -> LoopMode.SINGLE
                mBinding.rbLoopAll.id -> LoopMode.ALL
                else -> LoopMode.NONE
            }
            player.loopMode = mode
            appendLog("切换循环模式: $mode")
        }

        // 清空日志
        mBinding.btnClearLog.setOnClickListener {
            mBinding.tvLog.text = ""
        }
    }

    /**
     * 切换渲染器（新架构核心功能演示）
     *
     * 展示运行时动态切换渲染方式的能力，
     * 这是组合模式相比继承模式的最大优势。
     */
    private fun switchRender(renderName: String) {
        if (currentRenderName == renderName) {
            appendLog("当前已是 $renderName 渲染模式")
            return
        }

        val wasPlaying = player.isPlaying || player.state == PlayerState.PAUSED
        val savedPosition = player.currentPosition

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
        player.safeSwitchToRender(newRender)

        currentRenderName = renderName
        updateRenderButtonState()
        updateArchInfo()

        appendLog("已切换到: $renderName")
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
    private fun switchEngine(targetType: EngineType) {
        if (currentEngineType == targetType) {
            appendLog("当前已是 $targetType 引擎")
            return
        }

        val wasPlaying = player.isPlaying || player.state == PlayerState.PAUSED
        val savedPosition = player.currentPosition

        appendLog("开始切换引擎: ${currentEngineType} -> $targetType" +
                (if (wasPlaying) " (正在播放，位置=${formatTime(savedPosition)})" else ""))

        // 保存当前数据源信息
        val currentUrl = mBinding.etUrl.text.toString().trim()

        // 释放旧播放器
        try {
            player.release()
            appendLog("已释放旧引擎")
        } catch (_: Exception) {}

        // 创建新引擎
        val newEngine = when (targetType) {
            EngineType.MEDIA_PLAYER -> MediaPlayerEngine()
            EngineType.MEDIA3 -> Media3PlayerEngine()
            EngineType.IJK_PLAYER -> IjkPlayerEngine()
        }

        // 重建播放器（使用当前渲染器）
        val currentRender = RenderRegistry.create(currentRenderName) ?: SurfaceViewRender()

        player = VideoPlayer(
            engine = newEngine,
            render = currentRender
        ).apply {
            attach(mBinding.containerVideo)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }

        currentEngineType = targetType
        updateEngineButtonState()
        updateArchInfo()

        appendLog("已切换到: $targetType")

        // 如果之前在播放，尝试恢复
        if (wasPlaying && savedPosition > 0) {
            autoPlayOnPrepared = true
            if (currentUrl.isNotEmpty()) {
                player.setSource(currentUrl)
            } else {
                player.setSource(defaultTestUrl)
            }
            appendLog("恢复播放: ${formatTime(savedPosition)}")
        }
    }

    /**
     * 更新渲染器切换按钮状态
     */
    private fun updateRenderButtonState() {
        mBinding.btnSwitchSurfaceView.isEnabled = currentRenderName != SurfaceViewRender.NAME
        mBinding.btnSwitchTextureView.isEnabled = currentRenderName != TextureViewRender.NAME
        mBinding.btnSwitchVideoView.isEnabled = currentRenderName != VideoViewRender.NAME
        mBinding.btnSwitchPlayerView.isEnabled = currentRenderName != ExoPlayerViewRender.NAME
    }

    /**
     * 更新引擎切换按钮状态
     */
    private fun updateEngineButtonState() {
        mBinding.btnSwitchMediaPlayer.isEnabled = currentEngineType != EngineType.MEDIA_PLAYER
        mBinding.btnSwitchMedia3.isEnabled = currentEngineType != EngineType.MEDIA3
        mBinding.btnSwitchIjkPlayer.isEnabled = currentEngineType != EngineType.IJK_PLAYER
    }

    /**
     * 更新架构信息显示
     */
    private fun updateArchInfo() {
        val engineName = when (currentEngineType) {
            EngineType.MEDIA_PLAYER -> "MediaPlayerEngine"
            EngineType.MEDIA3 -> "Media3(ExoPlayer)"
            EngineType.IJK_PLAYER -> "IjkPlayerEngine"
        }
        // 使用 RenderRegistry 获取渲染器的显示名称
        val renderDisplayName = try {
            RenderRegistry.create(currentRenderName)?.renderName ?: currentRenderName
        } catch (e: Exception) {
            currentRenderName
        }
        mBinding.tvArchInfo.text = "当前组合: $engineName + $renderDisplayName"
    }

    // ==================== 播放器事件监听 ====================

    /**
     * 播放器事件监听器
     */
    private val playerListener = object : IVideoPlayerListener {

        override fun onPrepared(durationMs: Long) {
            appendLog("onPrepared: 时长: ${formatTime(durationMs)}")

            activity?.runOnUiThread {
                updateStateUI(PlayerState.PREPARED)
                updateButtonStates()

                if (autoPlayOnPrepared) {
                    player.play()
                    appendLog("自动开始播放")
                }
            }
        }

        override fun onComplete() {
            appendLog("onComplete: 播放完成")

            activity?.runOnUiThread {
                updateStateUI(PlayerState.COMPLETED)
                updateButtonStates()
            }
        }

        override fun onError(code: Int, msg: String): Boolean {
            appendLog("onError: 错误码=$code, 消息: $msg")

            activity?.runOnUiThread {
                updateStateUI(PlayerState.ERROR)
                updateButtonStates()
            }
            return true
        }

        override fun onProgress(position: Long, duration: Long) {
            if (!isUserSeeking && duration > 0) {
                activity?.runOnUiThread {
                    val progress = (position.toFloat() / duration * 100).toInt()
                    if (!mBinding.seekBarProgress.isPressed) {
                        mBinding.seekBarProgress.progress = progress
                    }
                    mBinding.tvProgress.text = "${formatTime(position)} / ${formatTime(duration)}"
                }
            }
        }

        override fun onBufferingUpdate(percent: Int) {
            // 可在此更新缓冲进度 UI
        }

        override fun onStateChanged(oldState: PlayerState, newState: PlayerState) {
            appendLog("onStateChanged: $oldState -> $newState")

            activity?.runOnUiThread {
                updateStateUI(newState)
                updateButtonStates()
            }
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            appendLog("onVideoSizeChanged: 尺寸: ${width}x${height}")
            mBinding.tvVideoSize.text = "${width}x${height}"
        }

        override fun onInfo(what: Int, extra: Int): Boolean {
            // 信息日志，通常不需要显示给用户
            return true
        }
    }

    // ==================== UI 更新方法 ====================

    /**
     * 更新状态显示
     */
    private fun updateStateUI(state: PlayerState) {
        mBinding.tvState.text = "状态: $state"

        // 根据状态更新 UI 颜色或图标
        when (state) {
            PlayerState.IDLE, PlayerState.STOPPED -> {
                mBinding.tvState.setTextColor(android.graphics.Color.GRAY)
            }
            PlayerState.PREPARING -> {
                mBinding.tvState.setTextColor(android.graphics.Color.YELLOW)
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                mBinding.tvState.setTextColor(android.graphics.Color.BLUE)
            }
            PlayerState.PLAYING -> {
                mBinding.tvState.setTextColor(android.graphics.Color.GREEN)
            }
            PlayerState.COMPLETED -> {
                mBinding.tvState.setTextColor(android.graphics.Color.CYAN)
            }
            PlayerState.ERROR, PlayerState.RELEASED -> {
                mBinding.tvState.setTextColor(android.graphics.Color.RED)
            }
        }
    }

    /**
     * 更新按钮启用/禁用状态
     */
    private fun updateButtonStates() {
        val state = player.state
        val canPlay = state in listOf(
            PlayerState.IDLE,
            PlayerState.STOPPED,
            PlayerState.PREPARED,
            PlayerState.PAUSED,
            PlayerState.COMPLETED,
            PlayerState.ERROR
        )
        val canPause = state == PlayerState.PLAYING
        val canStop = state !in listOf(PlayerState.IDLE, PlayerState.RELEASED, PlayerState.STOPPED)

        mBinding.btnPlay.isEnabled = canPlay
        mBinding.btnPause.isEnabled = canPause
        mBinding.btnStop.isEnabled = canStop
    }

    /**
     * 重置进度条 UI
     */
    private fun resetProgressUI() {
        mBinding.seekBarProgress.progress = 0
        mBinding.tvProgress.text = "00:00 / 00:00"
    }

    /**
     * 追加日志文本
     */
    private fun appendLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logLine = "[$time] $message\n"
        
        mBinding.tvLog.let { tvLog ->
            val currentText = tvLog.text.toString()
            if (currentText.length > 5000) {
                tvLog.text = currentText.takeLast(3000)
            }
            tvLog.append(logLine)
            
            /// 自动滚动到底部
            val scrollView = mBinding.tvLog.parent as? android.widget.ScrollView
            scrollView?.post {
                scrollView.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
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

    // ==================== 生命周期管理 ====================

    override fun onPause() {
        super.onPause()
        if (::player.isInitialized) {
            if (player.isPlaying) {
                player.pause()
                appendLog("onPause: 暂停播放")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 如果需要恢复播放，可以在这里处理
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::player.isInitialized) {
            player.detach()
            appendLog("onDestroyView: 解绑视图")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
            appendLog("onDestroy: 释放资源")
        }
    }
}
