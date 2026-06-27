package app.allever.android.sample.audiovideo.android

import android.widget.SeekBar
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentAndroidMedia3PlayerSampleBinding
import app.allever.android.lib.player.core.IVideoPlayerListener
import app.allever.android.lib.player.core.LoopMode
import app.allever.android.lib.player.core.PendingPrepare
import app.allever.android.lib.player.core.PlayerErrorCode
import app.allever.android.lib.player.core.PlayerState
import app.allever.android.lib.player.core.SurfaceType
import app.allever.android.lib.player.core.VideoHelper
import app.allever.android.lib.player.core.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.player.AndroidMedia3Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidMedia3PlayerSampleFragment :
    BaseFragment<FragmentAndroidMedia3PlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: AndroidMedia3Player
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

    // 默认测试视频URL（可替换为实际可用的测试视频）
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    override fun inflate(): FragmentAndroidMedia3PlayerSampleBinding =
        FragmentAndroidMedia3PlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initVideoPicker()
        initPlayer()
        initViews()
        initListeners()
    }

    private fun initVideoPicker() {
    }

    private fun initPlayer() {
        player = AndroidMedia3Player().apply {
            attach(mBinding.playerView)
            setVideoPlayerListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    private fun initViews() {
        updateStateUI(PlayerState.IDLE)
        updateButtonStates()
    }

    private fun initListeners() {
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

        // 进度条拖动
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

        // 变速控制
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

        // 音量控制
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

        // 循环模式切换
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

        // 缩放模式切换
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

        // Surface 类型切换
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

        // 重试次数设置
        mBinding.etRetryCount.setOnEditorActionListener { _, _, _ ->
            val count = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 0
            player.retryCount = count.coerceAtLeast(0)
            appendLog("重试次数设置为: $count")
            true
        }
    }

    // ==================== Surface 类型切换方法 ====================

    /**
     * 切换到 PlayerView 模式（推荐）
     *
     * 使用安全切换方式：暂停 → 延迟 → 切换 → 恢复
     * 避免 MediaCodec 状态机竞态条件导致的崩溃
     */
    private fun switchToPlayerView() {
        // 显示 PlayerView，隐藏其他视图
        mBinding.playerView.visibility = android.view.View.VISIBLE
        mBinding.surfaceView.visibility = android.view.View.GONE
        mBinding.textureView.visibility = android.view.View.GONE

        // 使用安全切换（自动处理暂停/恢复）
        player.safeSwitchToPlayerView(mBinding.playerView)

        appendLog("正在切换到 PlayerView 模式...")
    }

    /**
     * 切换到 SurfaceView 模式（兼容）
     *
     * 使用安全切换方式：暂停 → 延迟 → 切换 → 恢复
     */
    private fun switchToSurfaceView() {
        // 隐藏 PlayerView 和 TextureView，显示 SurfaceView
        mBinding.playerView.visibility = android.view.View.GONE
        mBinding.surfaceView.visibility = android.view.View.VISIBLE
        mBinding.textureView.visibility = android.view.View.GONE

        // 使用安全切换（自动处理暂停/恢复）
        player.safeSwitchToSurfaceView(mBinding.surfaceView)

        appendLog("正在切换到 SurfaceView 模式...")
    }

    /**
     * 切换到 TextureView 模式（高级）
     *
     * 使用安全切换方式：暂停 → 延迟 → 切换 → 恢复
     */
    private fun switchToTextureView() {
        // 隐藏 PlayerView 和 SurfaceView，显示 TextureView
        mBinding.playerView.visibility = android.view.View.GONE
        mBinding.surfaceView.visibility = android.view.View.GONE
        mBinding.textureView.visibility = android.view.View.VISIBLE

        // 使用安全切换（自动处理暂停/恢复）
        player.safeSwitchToTextureView(mBinding.textureView)

        appendLog("正在切换到 TextureView 模式...")
    }

    // ==================== 播放器监听器 ====================

    private val playerListener = object : IVideoPlayerListener {
        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            activity?.runOnUiThread {
                updateStateUI(to)
                updateButtonStates()
                appendLog("状态变化: $from -> $to")
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

        override fun onError(errorCode: Int, msg: String): Boolean {
            activity?.runOnUiThread {
                appendLog("播放错误: [${PlayerErrorCode.getMessage(errorCode)}] $msg")
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

    private fun resetProgressUI() {
        mBinding.seekBarProgress.progress = 0
        mBinding.tvProgress.text = "00:00 / 00:00"
    }

    // ==================== 工具方法 ====================

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun appendLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logText = "[$timestamp] $message\n"
        mBinding.tvLog.append(logText)

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
            appendLog("播放器已解绑 PlayerView")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isChangingConfigurations) {
            player.release()
            log("Media3Sample", "播放器已释放")
        }
    }

    override fun onResume() {
        super.onResume()
        if (player.state == PlayerState.PLAYING) {
            appendLog("恢复播放器状态: ${player.state}")
        }
    }
}
