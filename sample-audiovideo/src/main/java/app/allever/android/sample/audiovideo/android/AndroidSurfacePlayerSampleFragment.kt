package app.allever.android.sample.audiovideo.android

import android.media.MediaPlayer
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentAndroidSurfaceViewPlayerSampleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidSurfacePlayerSampleFragment :
    BaseFragment<FragmentAndroidSurfaceViewPlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: AndroidSurfacePlayer

    private val videoPickerLauncher by lazy {
        MediaPickerCore.registerPickerLauncher(this) { items ->
            items.firstOrNull()?.let { mediaItem ->
                if (mediaItem is MediaItem.Video) {
                    mBinding.etUrl.setText(mediaItem.uri.toString())
                    appendLog("选择本地视频: ${mediaItem.name} (${mediaItem.uri})")
                    autoPlayOnPrepared = true
                    player.setSource(mediaItem.uri)
                }
            }
        }
    }

    private var isUserSeeking = false

    /** 默认测试视频URL */
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    override fun inflate(): FragmentAndroidSurfaceViewPlayerSampleBinding =
        FragmentAndroidSurfaceViewPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initVideoPicker()
        initPlayer()
        initViews()
        updateStateUI(PlayerState.IDLE)
        updateButtonStates()
    }

    private fun initVideoPicker() {
    }

    private fun initPlayer() {
        player = AndroidSurfacePlayer().apply {
            attach(mBinding.surfaceView)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    private fun initViews() {
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

        // 重试次数设置
        mBinding.etRetryCount.setOnEditorActionListener { _, _, _ ->
            val count = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 0
            player.retryCount = count.coerceAtLeast(0)
            appendLog("重试次数设置为: $count")
            true
        }
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
                    MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> "视频帧滞后"
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> "缓冲开始"
                    MediaPlayer.MEDIA_INFO_BUFFERING_END -> "缓冲结束"
                    MediaPlayer.MEDIA_INFO_UNKNOWN -> "未知信息"
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

        mBinding.btnPlay.isEnabled = state in listOf(
            PlayerState.IDLE,
            PlayerState.STOPPED,
            PlayerState.COMPLETED,
            PlayerState.ERROR,
            PlayerState.PREPARED,
            PlayerState.PAUSED,
        )

        mBinding.btnPlay.text = when (state) {
            PlayerState.PAUSED -> "继续"
            else -> "播放"
        }

        mBinding.btnPause.isEnabled = state == PlayerState.PLAYING

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
            player.release()
            appendLog("播放器已释放")
        }
    }

    override fun onResume() {
        super.onResume()
        if (player.state == PlayerState.PLAYING) {
            appendLog("恢复播放器状态: ${player.state}")
        }
    }
}
