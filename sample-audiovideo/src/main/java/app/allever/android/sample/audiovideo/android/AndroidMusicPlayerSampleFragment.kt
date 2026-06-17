package app.allever.android.sample.audiovideo.android

import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentAndroidMusicPlayerSampleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidMusicPlayerSampleFragment :
    BaseFragment<FragmentAndroidMusicPlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: AndroidMusicPlayer
    private lateinit var audioPickerLauncher: ActivityResultLauncher<MediaPickerConfig>
    private var isUserSeeking = false

    // 默认测试音频URL（可替换为实际可用的测试音频）
    private val defaultTestUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    override fun inflate(): FragmentAndroidMusicPlayerSampleBinding =
        FragmentAndroidMusicPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initAudioPicker()
        initPlayer()
        initViews()
        initListeners()
    }

    private fun initAudioPicker() {
        audioPickerLauncher = MediaPickerCore.registerPickerLauncher(this) { items ->
            items.firstOrNull()?.let { mediaItem ->
                if (mediaItem is MediaItem.Audio) {
                    mBinding.etUrl.setText(mediaItem.uri.toString())
                    appendLog("选择本地音频: ${mediaItem.name} (${mediaItem.title})")
                    player.play(mediaItem.uri)
                }
            }
        }
    }

    private fun initPlayer() {
        player = AndroidMusicPlayer().apply {
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    private fun initViews() {
        // 初始化UI状态
        updateStateUI(PlayerState.IDLE)
        updateButtonStates()
    }

    private fun initListeners() {
        // 播放控制按钮
        mBinding.btnPlay.setOnClickListener {
            val url = mBinding.etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                player.play(url)
            } else {
                player.play(defaultTestUrl)
            }
            appendLog("开始播放: ${if (mBinding.etUrl.text.isNotEmpty()) mBinding.etUrl.text else defaultTestUrl}")
        }

        mBinding.btnPause.setOnClickListener {
            player.pause()
            appendLog("暂停播放")
        }

        mBinding.btnResume.setOnClickListener {
            player.resume()
            appendLog("继续播放")
        }

        mBinding.btnStop.setOnClickListener {
            player.stop()
            appendLog("停止播放")
            resetProgressUI()
        }

        // 选择本地音频
        mBinding.btnPickLocal.setOnClickListener {
            MediaPickerCore.launchAudio(audioPickerLauncher)
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
                isUserSeeking = false
                if (player.duration > 0) {
                    val position = (seekBar?.progress?.toFloat() ?: 0f / 100 * player.duration).toLong()
                    player.seekTo(position)
                    appendLog("跳转到: ${formatTime(position)}")
                }
            }
        })

        // 变速控制
        mBinding.seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // progress: 0-50, 映射到 0.5-3.0
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

    private val playerListener = object : IPlayerListener {
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
            return false // 返回false让播放器自动处理重试
        }

        override fun onBufferingUpdate(percent: Int) {
            activity?.runOnUiThread {
                // 可以在这里更新缓冲进度（如果需要的话）
                // mBinding.tvBuffering.text = "缓冲: $percent%"
            }
        }
    }

    // ==================== UI 更新方法 ====================

    private fun updateStateUI(state: PlayerState) {
        mBinding.tvState.text = "状态: $state"

        // 根据状态改变颜色
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

        // 播放按钮：在 IDLE/STOPPED/COMPLETED/ERROR 状态下可用
        mBinding.btnPlay.isEnabled = state in listOf(
            PlayerState.IDLE,
            PlayerState.STOPPED,
            PlayerState.COMPLETED,
            PlayerState.ERROR
        )

        // 暂停按钮：仅在 PLAYING 状态可用
        mBinding.btnPause.isEnabled = state == PlayerState.PLAYING

        // 继续按钮：仅在 PAUSED 状态可用
        mBinding.btnResume.isEnabled = state == PlayerState.PAUSED

        // 停止按钮：在 PREPARED/PLAYING/PAUSED/COMPLETED 状态可用
        mBinding.btnStop.isEnabled = state in listOf(
            PlayerState.PREPARED,
            PlayerState.PLAYING,
            PlayerState.PAUSED,
            PlayerState.COMPLETED
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

        // 自动滚动到底端
        val scrollView = mBinding.tvLog.parent as? android.widget.ScrollView
        scrollView?.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    // ==================== 生命周期管理 ====================

    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment 销毁时释放播放器资源
        if (!requireActivity().isChangingConfigurations) {
            player.release()
            appendLog("播放器已释放")
        }
    }

    override fun onResume() {
        super.onResume()
        // 从后台恢复时检查播放器状态
        if (player.state == PlayerState.PLAYING) {
            appendLog("恢复播放器状态: ${player.state}")
        }
    }
}
