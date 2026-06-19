package app.allever.android.sample.audiovideo.android

import android.net.Uri
import android.view.SurfaceView
import android.view.TextureView
import android.widget.VideoView
import android.widget.SeekBar
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentAndroidMediaPlayerSampleBinding
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AndroidMediaPlayer 示例 Fragment
 *
 * 功能演示：
 * 1. VideoView / SurfaceView / TextureView 三种 Surface 绑定模式
 * 2. 安全切换 Surface（stop → 切换 → reprepare）
 * 3. 播放控制（播放/暂停/停止/跳转）
 * 4. 变速播放、音量调节
 * 5. 循环模式（不循环/单曲循环/列表循环）
 * 6. 画面缩放模式（适应屏幕/填满裁剪/拉伸变形）
 * 7. 本地视频选择、Assets 文件播放
 * 8. 实时日志输出
 */
class AndroidMediaPlayerSampleFragment :
    BaseFragment<FragmentAndroidMediaPlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: AndroidMediaPlayer

    // 视频选择器回调
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

    // 默认测试视频URL
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    override fun inflate(): FragmentAndroidMediaPlayerSampleBinding =
        FragmentAndroidMediaPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initVideoPicker()
        initPlayer()
        initViews()
        initListeners()
    }

    /**
     * 初始化视频选择器
     */
    private fun initVideoPicker() {
        // 配置视频选择器（可选）
        // MediaPickerCore 配置已在应用初始化时完成
    }

    /**
     * 初始化播放器
     *
     * 使用 VideoView 作为默认 Surface 类型
     */
    private fun initPlayer() {
        player = AndroidMediaPlayer().apply {
            attach(mBinding.videoView)
            setListener(playerListener)
            retryCount = 3
            progressIntervalMs = 200
        }
    }

    /**
     * 初始化视图状态
     */
    private fun initViews() {
        updateStateUI(PlayerState.IDLE)
        updateButtonStates()
    }

    /**
     * 初始化所有监听器
     *
     * 包括：
     * - 播放控制按钮（播放/暂停/停止）
     * - 本地视频选择
     * - Assets 文件播放
     * - 进度条拖动
     * - 变速控制
     * - 音量控制
     * - 循环模式切换
     * - 缩放模式切换
     * - Surface 类型切换
     * - 重试次数设置
     */
    private fun initListeners() {
        // ==================== 播放控制按钮 ====================

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

        // ==================== 进度条拖动 ====================

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

        // ==================== 变速控制 ====================

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

        // ==================== 音量控制 ====================

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

        // ==================== 循环模式切换 ====================

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

        // ==================== 缩放模式切换 ====================

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

        // ==================== Surface 类型切换 ====================

        mBinding.radioGroupSurfaceType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                mBinding.rbSurfaceVideoView.id -> {
                    switchToVideoView()
                    appendLog("切换到 VideoView 模式")
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

        // ==================== 重试次数设置 ====================

        mBinding.etRetryCount.setOnEditorActionListener { _, _, _ ->
            val count = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 0
            player.retryCount = count.coerceAtLeast(0)
            appendLog("重试次数设置为: $count")
            true
        }
    }

    // ==================== Surface 类型切换方法 ====================

    /**
     * 切换到 VideoView 模式（推荐，差异化特性）
     *
     * 使用安全切换方式：停止 → 延迟 → 切换 → 重新准备 → 恢复
     * 避免 MediaPlayer 状态机冲突导致的崩溃
     */
    private fun switchToVideoView() {
        // 显示 VideoView，隐藏其他视图
        mBinding.videoView.visibility = android.view.View.VISIBLE
        mBinding.surfaceView.visibility = android.view.View.GONE
        mBinding.textureView.visibility = android.view.View.GONE

        // 使用安全切换（自动处理停止/恢复）
        player.safeSwitchToVideoView(mBinding.videoView)

        appendLog("正在切换到 VideoView 模式...")
    }

    /**
     * 切换到 SurfaceView 模式（兼容性好，性能优）
     *
     * 使用安全切换方式：停止 → 延迟 → 切换 → 重新准备 → 恢复
     */
    private fun switchToSurfaceView() {
        // 隐藏 VideoView 和 TextureView，显示 SurfaceView
        mBinding.videoView.visibility = android.view.View.GONE
        mBinding.surfaceView.visibility = android.view.View.VISIBLE
        mBinding.textureView.visibility = android.view.View.GONE

        // 使用安全切换（自动处理停止/恢复）
        player.safeSwitchToSurfaceView(mBinding.surfaceView)

        appendLog("正在切换到 SurfaceView 模式...")
    }

    /**
     * 切换到 TextureView 模式（支持动画和透明度）
     *
     * 使用安全切换方式：停止 → 延迟 → 切换 → 重新准备 → 恢复
     */
    private fun switchToTextureView() {
        // 隐藏 VideoView 和 SurfaceView，显示 TextureView
        mBinding.videoView.visibility = android.view.View.GONE
        mBinding.surfaceView.visibility = android.view.View.GONE
        mBinding.textureView.visibility = android.view.View.VISIBLE

        // 使用安全切换（自动处理停止/恢复）
        player.safeSwitchToTextureView(mBinding.textureView)

        appendLog("正在切换到 TextureView 模式...")
    }

    // ==================== 播放器监听器 ====================

    /**
     * 播放器事件监听器
     *
     * 处理所有播放器状态变化和事件回调
     */
    private val playerListener = object : IVideoPlayerListener {

        /**
         * 状态变化回调
         *
         * @param from 原始状态
         * @param to 目标状态
         */
        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            activity?.runOnUiThread {
                updateStateUI(to)
                updateButtonStates()
                appendLog("状态变化: $from -> $to")
            }
        }

        /**
         * 准备完成回调
         *
         * @param durationMs 视频时长（毫秒）
         */
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

        /**
         * 播放进度回调
         *
         * @param currentMs 当前位置（毫秒）
         * @param durationMs 总时长（毫秒）
         */
        override fun onProgress(currentMs: Long, durationMs: Long) {
            activity?.runOnUiThread {
                if (!isUserSeeking && durationMs > 0) {
                    val progress = (currentMs.toFloat() / durationMs * 100).toInt()
                    mBinding.seekBarProgress.progress = progress
                    mBinding.tvProgress.text = "${formatTime(currentMs)} / ${formatTime(durationMs)}"
                }
            }
        }

        /**
         * 播放完成回调
         */
        override fun onComplete() {
            activity?.runOnUiThread {
                appendLog("播放完成")
                updateButtonStates()
            }
        }

        /**
         * 错误回调
         *
         * @param what 错误类型
         * @param extra 额外错误信息
         * @return 是否已处理错误
         */
        override fun onError(what: Int, extra: Int): Boolean {
            activity?.runOnUiThread {
                appendLog("播放错误: what=$what, extra=$extra")
            }
            return false
        }

        /**
         * 缓冲进度回调
         *
         * @param percent 缓冲百分比（0-100）
         */
        override fun onBufferingUpdate(percent: Int) {
            activity?.runOnUiThread {
                appendLog("缓冲进度: $percent%")
            }
        }

        /**
         * 视频尺寸变化回调
         *
         * @param width 视频宽度（像素）
         * @param height 视频高度（像素）
         */
        override fun onVideoSizeChanged(width: Int, height: Int) {
            activity?.runOnUiThread {
                mBinding.tvVideoSize.text = "${width}x${height}"
                appendLog("视频尺寸: ${width}x${height}")
            }
        }

        /**
         * 信息通知回调
         *
         * @param what 信息类型
         * @param extra 额外信息
         * @return 是否已处理信息
         */
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
     *
     * @param state 当前播放器状态
     */
    private fun updateStateUI(state: PlayerState) {
        mBinding.tvState.text = "状态: $state"

        // 根据状态设置不同颜色
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
     * 更新按钮可用状态
     *
     * 根据当前播放器状态启用/禁用各控制按钮
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
     * 重置进度条 UI 到初始状态
     */
    private fun resetProgressUI() {
        mBinding.seekBarProgress.progress = 0
        mBinding.tvProgress.text = "00:00 / 00:00"
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化时间显示
     *
     * @param ms 时间（毫秒）
     * @return 格式化后的时间字符串（MM:SS）
     */
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    /**
     * 追加日志到日志显示区域
     *
     * @param message 日志消息
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

    /**
     * View 销毁时解绑播放器
     *
     * 注意：仅在非配置变更时解绑，避免屏幕旋转导致重复创建
     */
    override fun onDestroyView() {
        super.onDestroyView()
        if (!requireActivity().isChangingConfigurations) {
            player.detach()
            appendLog("播放器已解绑 Surface")
        }
    }

    /**
     * Activity 销毁时释放播放器资源
     *
     * 注意：仅在非配置变更时释放，避免屏幕旋转导致重复创建
     */
    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isChangingConfigurations) {
            player.release()
            appendLog("播放器已释放")
        }
    }

    /**
     * Activity 暂停时自动暂停播放
     */
    override fun onPause() {
        super.onPause()
        if (player.state == PlayerState.PLAYING) {
            player.pause()
            appendLog("Activity 暂停，自动暂停播放")
        }
    }

    /**
     * Activity 恢复时不自动恢复播放（用户手动点击继续）
     */
    override fun onResume() {
        super.onResume()
        // 不自动恢复播放，让用户手动点击"继续"按钮
    }
}
