package app.allever.android.sample.audiovideo.sdk

import android.view.SurfaceView
import android.view.TextureView
import android.widget.SeekBar
import androidx.media3.ui.PlayerView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.android.IVideoPlayerListener
import app.allever.android.sample.audiovideo.android.LoopMode
import app.allever.android.sample.audiovideo.android.PlayerState
import app.allever.android.sample.audiovideo.android.VideoScaleMode
import app.allever.android.sample.audiovideo.databinding.FragmentSdkExoVideoPlayerSampleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ExoPlayer (SDK) 视频播放器示例
 *
 * 功能演示：
 * - 三种 Surface 绑定模式切换（PlayerView / SurfaceView / TextureView）
 * - 播放/暂停/停止控制
 * - 进度条拖动与实时更新
 * - 变速播放（0.5x ~ 3.0x）
 * - 音量控制（0% ~ 100%）
 * - 循环模式（不循环/单曲循环/列表循环）
 * - 本地视频选择
 * - Assets 文件播放
 * - 自动重试机制
 * - 状态机变化监听
 */
class ExoVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkExoVideoPlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: ExoVideoPlayer

    /** 视频选择器启动器 */
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

    /** 默认测试视频URL */
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    override fun inflate() = FragmentSdkExoVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initVideoPicker()
        initPlayer()
        initViews()
        initListeners()
    }

    // ==================== 初始化方法 ====================

    private fun initVideoPicker() {
        // 可在此处配置 MediaPickerConfig
    }

    /**
     * 初始化播放器并绑定到 PlayerView（默认）
     */
    private fun initPlayer() {
        player = ExoVideoPlayer().apply {
            attach(mBinding.playerView)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    private fun initViews() {
        updateStateUI(PlayerState.IDLE)
        updateButtonStates()
    }

    private fun initListeners() {
        initPlayControlListeners()
        initProgressListeners()
        initSpeedControlListeners()
        initVolumeControlListeners()
        initLoopControlListeners()
        initScaleModeListeners()
        initSurfaceTypeListeners()
        initRetryCountListener()
    }

    // ==================== 播放控制监听器 ====================

    private fun initPlayControlListeners() {
        // 播放/继续按钮
        mBinding.btnPlay.setOnClickListener {
            when (player.state) {
                PlayerState.PAUSED -> {
                    player.play()
                    appendLog("继续播放")
                }
                else -> {
                    val url = mBinding.etUrl.text.toString().trim()
                    if (url.isNotEmpty()) {
                        autoPlayOnPrepared = true
                        player.setSource(url)
                    } else {
                        autoPlayOnPrepared = true
                        player.setSource(defaultTestUrl)
                    }
                    appendLog("设置数据源: ${if (mBinding.etUrl.text.isNotEmpty()) mBinding.etUrl.text else defaultTestUrl}")
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
    }

    // ==================== 进度条监听器 ====================

    private fun initProgressListeners() {
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
                // 延迟解除拖动标志，避免 seekTo 异步完成前被 onProgress 用旧位置覆盖
                mBinding.seekBarProgress.post { isUserSeeking = false }
            }
        })
    }

    // ==================== 变速控制监听器 ====================

    private fun initSpeedControlListeners() {
        mBinding.seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = 0.5f + (progress.toFloat() / 50 * 2.5f)
                mBinding.tvSpeed.text = String.format(Locale.US, "%.1fx", speed)
                if (fromUser) {
                    player.speed = speed
                    appendLog("变速: ${String.format(Locale.US, "%.1fx", speed)}")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // ==================== 音量控制监听器 ====================

    private fun initVolumeControlListeners() {
        mBinding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress.toFloat() / 100
                mBinding.tvVolume.text = "${progress}%"
                if (fromUser) {
                    player.volume = volume
                    appendLog("音量: ${progress}%")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // ==================== 循环模式监听器 ====================

    private fun initLoopControlListeners() {
        mBinding.radioGroupLoop.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                mBinding.rbLoopNone.id -> {
                    player.loopMode = LoopMode.NONE
                    appendLog("循环模式: 不循环")
                }
                mBinding.rbLoopSingle.id -> {
                    player.loopMode = LoopMode.SINGLE
                    appendLog("循环模式: 单曲循环")
                }
                mBinding.rbLoopAll.id -> {
                    player.loopMode = LoopMode.ALL
                    appendLog("循环模式: 列表循环")
                }
            }
        }
    }

    // ==================== 缩放模式控制 ====================

    /**
     * 初始化画面缩放模式选择监听器
     *
     * 三种模式：
     * - FIT_CENTER：保持比例，完整显示（可能有黑边）
     * - CROP_CENTER：保持比例，填满容器（可能裁剪）
     * - STRETCH：拉伸填满容器（可能变形）
     */
    private fun initScaleModeListeners() {
        mBinding.radioGroupScaleMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                mBinding.rbScaleFitCenter.id -> VideoScaleMode.FIT_CENTER
                mBinding.rbScaleCropCenter.id -> VideoScaleMode.CROP_CENTER
                mBinding.rbScaleStretch.id -> VideoScaleMode.STRETCH
                else -> VideoScaleMode.FIT_CENTER
            }
            player.videoScaleMode = mode
            appendLog("画面缩放模式: $mode")
        }
    }

    // ==================== Surface 类型切换监听器 ====================

    private fun initSurfaceTypeListeners() {
        mBinding.radioGroupSurfaceType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                mBinding.rbSurfacePlayerView.id -> {
                    switchToPlayerView()
                    appendLog("切换到 PlayerView 模式")
                }
                mBinding.rbSurfaceSurfaceView.id -> {
                    switchToSurfaceView()
                    appendLog("切换到 SurfaceView 模式")
                }
                mBinding.rbSurfaceTextureView.id -> {
                    switchToTextureView()
                    appendLog("切换到 TextureView 模式")
                }
            }
        }
    }

    /**
     * 切换到 PlayerView 模式（推荐）
     */
    private fun switchToPlayerView() {
        // 显示 PlayerView，隐藏其他视图
        mBinding.playerView.visibility = android.view.View.VISIBLE
        
        // 解绑当前 Surface，重新绑定到 PlayerView
        player.detach()
        player.attach(mBinding.playerView)
        
        appendLog("已绑定到 PlayerView")
    }

    /**
     * 切换到 SurfaceView 模式（兼容）
     */
    private fun switchToSurfaceView() {
        // 隐藏 PlayerView
        mBinding.playerView.visibility = android.view.View.GONE
        
        // 创建或获取 SurfaceView（此处简化处理，实际项目中应从布局中获取）
        val surfaceView = SurfaceView(requireContext()).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // 将 SurfaceView 添加到视频容器中
        val videoContainer = mBinding.playerView.parent as? android.widget.FrameLayout
        videoContainer?.addView(surfaceView, 0)
        
        // 解绑当前 Surface，重新绑定到 SurfaceView
        player.detach()
        player.attach(surfaceView)
        
        appendLog("已绑定到 SurfaceView（注意：SurfaceView 异步创建中）")
    }

    /**
     * 切换到 TextureView 模式（高级）
     */
    private fun switchToTextureView() {
        // 隐藏 PlayerView
        mBinding.playerView.visibility = android.view.View.GONE
        
        // 创建或获取 TextureView
        val textureView = TextureView(requireContext()).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // 将 TextureView 添加到视频容器中
        val videoContainer = mBinding.playerView.parent as? android.widget.FrameLayout
        videoContainer?.addView(textureView, 0)
        
        // 解绑当前 Surface，重新绑定到 TextureView
        player.detach()
        player.attach(textureView)
        
        appendLog("已绑定到 TextureView")
    }

    // ==================== 重试次数设置监听器 ====================

    private fun initRetryCountListener() {
        mBinding.etRetryCount.setOnEditorActionListener { _, _, _ ->
            val count = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 0
            player.retryCount = count.coerceAtLeast(0)
            appendLog("重试次数设置为: $count")
            true
        }
    }

    // ==================== 播放器监听器 ====================

    /**
     * ExoVideoPlayer 事件监听器实现
     *
     * 监听所有播放事件并更新 UI：
     * - 状态变化 → 更新状态文字和按钮状态
     * - 准备就绪 → 自动开始播放（如果启用）
     * - 进度更新 → 更新进度条和时间显示
     * - 播放完成 → 更新日志和按钮状态
     * - 错误发生 → 记录错误日志
     * - 视频尺寸变化 → 更新尺寸显示
     */
    private val playerListener = object : IVideoPlayerListener {

        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            activity?.runOnUiThread {
                updateStateUI(to)
                updateButtonStates()
                appendLog("状态变化: $from -> $to")

                // 当开始准备新数据源时，重置进度条 UI 为初始状态
                if (to == PlayerState.PREPARING) {
                    resetProgressUI()
                }
            }
        }

        override fun onPrepared(durationMs: Long) {
            activity?.runOnUiThread {
                appendLog("准备就绪, 时长: ${formatTime(durationMs)}")

                // 自动开始播放
                if (autoPlayOnPrepared) {
                    player.play()
                    appendLog("自动开始播放")
                }
            }
        }

        override fun onProgress(currentMs: Long, durationMs: Long) {
            activity?.runOnUiThread {
                if (!isUserSeeking && durationMs > 0) {
                    val progress = (currentMs.toFloat() / durationMs * 100).toInt()
                    mBinding.seekBarProgress.progress = progress
                    mBinding.tvProgress.text = "${formatTime(currentMs)} / ${formatTime(durationMs)}"
                }
            }
        }

        override fun onComplete() {
            activity?.runOnUiThread {
                appendLog("播放完成")
                updateButtonStates()
            }
        }

        override fun onError(what: Int, extra: Int): Boolean {
            activity?.runOnUiThread {
                appendLog("播放错误: what=$what, extra=$extra")
            }
            return false
        }

        override fun onBufferingUpdate(percent: Int) {
            activity?.runOnUiThread {
                appendLog("缓冲进度: $percent%")
            }
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            activity?.runOnUiThread {
                mBinding.tvVideoSize.text = "${width}x${height}"
                appendLog("视频尺寸: ${width}x${height}")
            }
        }

        override fun onInfo(what: Int, extra: Int): Boolean {
            activity?.runOnUiThread {
                val infoText = when (what) {
                    android.media.MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> "视频帧滞后"
                    android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START -> "缓冲开始"
                    android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END -> "缓冲结束"
                    android.media.MediaPlayer.MEDIA_INFO_UNKNOWN -> "未知信息"
                    else -> "info($what)"
                }
                appendLog("播放器信息: $infoText (extra=$extra)")
            }
            return false
        }
    }

    // ==================== UI 更新方法 ====================

    /**
     * 更新状态显示 UI
     */
    private fun updateStateUI(state: PlayerState) {
        mBinding.tvState.text = "状态: $state"

        val colorRes = when (state) {
            PlayerState.IDLE, PlayerState.RELEASED -> android.R.color.darker_gray
            PlayerState.PREPARING -> android.R.color.holo_orange_light
            PlayerState.PREPARED -> android.R.color.holo_blue_light
            PlayerState.PLAYING -> android.R.color.holo_green_light
            PlayerState.PAUSED -> android.R.color.holo_blue_dark
            PlayerState.STOPPED, PlayerState.COMPLETED -> android.R.color.darker_gray
            PlayerState.ERROR -> android.R.color.holo_red_light
        }
        mBinding.tvState.setTextColor(resources.getColor(colorRes, null))
    }

    /**
     * 根据当前状态更新按钮的可用性
     */
    private fun updateButtonStates() {
        val state = player.state

        // 播放按钮：可设置新数据源，或从暂停/完成状态恢复播放
        mBinding.btnPlay.isEnabled = state in listOf(
            PlayerState.IDLE,
            PlayerState.STOPPED,
            PlayerState.COMPLETED,
            PlayerState.ERROR,
            PlayerState.PREPARED,
            PlayerState.PAUSED,
        )

        // 根据状态改变播放按钮文字
        mBinding.btnPlay.text = when (state) {
            PlayerState.PAUSED -> "继续"
            else -> "播放"
        }

        // 暂停按钮：仅在 PLAYING 状态可用
        mBinding.btnPause.isEnabled = state == PlayerState.PLAYING

        // 停止按钮：在 PLAYING/PAUSED/PREPARED/COMPLETED 状态可用
        mBinding.btnStop.isEnabled = state in listOf(
            PlayerState.PREPARED,
            PlayerState.PLAYING,
            PlayerState.PAUSED,
            PlayerState.COMPLETED,
        )
    }

    /**
     * 重置进度条 UI 为初始状态
     */
    private fun resetProgressUI() {
        mBinding.seekBarProgress.progress = 0
        mBinding.tvProgress.text = "00:00 / 00:00"
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化时间为 MM:ss 格式
     */
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    /**
     * 追加日志信息到日志区域
     */
    private fun appendLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logText = "[$timestamp] $message\n"
        mBinding.tvLog.append(logText)

        // 自动滚动到底部
        val scrollView = mBinding.tvLog.parent as? android.widget.ScrollView
        scrollView?.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    // ==================== 生命周期管理 ====================

    override fun onDestroyView() {
        super.onDestroyView()
        if (!requireActivity().isChangingConfigurations) {
            player.detach()
            appendLog("播放器已解绑 Surface")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isChangingConfigurations) {
            player.release()
            log("ExoVideoSample", "播放器已释放")
        }
    }

    override fun onResume() {
        super.onResume()
        if (player.state == PlayerState.PLAYING) {
            appendLog("恢复播放器状态: ${player.state}")
        }
    }
}
