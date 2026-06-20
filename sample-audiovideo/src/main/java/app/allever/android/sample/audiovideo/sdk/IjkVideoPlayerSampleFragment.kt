package app.allever.android.sample.audiovideo.sdk

import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.databinding.FragmentSdkIjkVideoPlayerSampleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * IjkVideoPlayer 示例 Fragment
 *
 * 功能演示：
 * - 两种 Surface 绑定模式切换（SurfaceView / TextureView）
 * - 播放/暂停/停止控制
 * - 进度条拖动与实时更新
 * - 变速播放 (0.5x ~ 3.0x)
 * - 音量控制 (0% ~ 100%)
 * - 循环模式（不循环/单曲循环/列表循环）
 * - 画面缩放模式（适应屏幕/填满裁剪/拉伸变形）
 * - 本地视频选择（MediaPicker）
 * - Assets 文件播放
 * - IJKPlayer 特有功能：
 *   - TCP 下载速度监控
 *   - 缓冲百分比显示
 *   - 缓冲进度条显示
 * - 状态机可视化（彩色文字 + 日志）
 * - 视频尺寸显示
 *
 * 使用方式：
 * 1. 默认使用 SurfaceView 播放测试视频
 * 2. 可切换到 TextureView 测试异步就绪特性
 * 3. 可调整各种参数观察效果
 */
class IjkVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkIjkVideoPlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: IjkVideoPlayer

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

    /** 是否在 prepared 后自动播放 */
    private var autoPlayOnPrepared: Boolean = false

    /** TCP 速度监控协程 */
    private var tcpSpeedMonitorJob: Job? = null

    override fun inflate() = FragmentSdkIjkVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initPlayer()
        initListeners()
        startTcpSpeedMonitor()
    }

    /**
     * 初始化 IjkVideoPlayer 实例并绑定默认 SurfaceView
     */
    private fun initPlayer() {
        player = IjkVideoPlayer().apply {
            // 绑定 SurfaceView（推荐方式）
            attach(mBinding.surfaceView)

            // 设置监听器
            setListener(playerListener)

            // 配置参数
            retryCount = 3              // 出错自动重试 3 次
            progressIntervalMs = 200    // 每 200ms 更新一次进度
        }

        appendLog("IjkVideoPlayer 初始化完成")
        appendLog("当前绑定模式: SurfaceView")
    }

    /**
     * 初始化所有 UI 监听器
     */
    private fun initListeners() {
        initSurfaceTypeListeners()
        initPlaybackControlListeners()
        initSpeedControlListeners()
        initVolumeControlListeners()
        initLoopModeListeners()
        initScaleModeListeners()
        initDataInputListeners()
    }

    // ==================== Surface 绑定模式切换 ====================

    /**
     * 初始化 Surface 类型选择监听器
     *
     * 支持两种模式：
     * - SurfaceView：性能优异，异步就绪（需等待 surfaceCreated）
     * - TextureView：支持变换，通常立即可用
     */
    private fun initSurfaceTypeListeners() {
        mBinding.radioGroupSurfaceType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                mBinding.rbSurfaceSurfaceView.id -> switchToSurfaceView()
                mBinding.rbSurfaceTextureView.id -> switchToTextureView()
            }
        }
    }

    /**
     * 切换到 SurfaceView 模式
     *
     * 使用安全切换方式：stop → 延迟 → 切换 → reprepare → 恢复
     * 避免解码器状态机竞态条件导致的崩溃
     */
    private fun switchToSurfaceView() {
        appendLog("切换到 SurfaceView 模式")

        // 显示 SurfaceView，隐藏 TextureView
        mBinding.surfaceView.visibility = android.view.View.VISIBLE
        mBinding.textureView.visibility = android.view.View.GONE

        // 使用安全切换（自动处理 stop/reprepare/恢复）
        player.safeSwitchToSurfaceView(mBinding.surfaceView)

        appendLog("正在切换到 SurfaceView 模式...")
    }

    /**
     * 切换到 TextureView 模式
     *
     * 使用安全切换方式：stop → 延迟 → 切换 → reprepare → 恢复
     */
    private fun switchToTextureView() {
        appendLog("切换到 TextureView 模式")

        // 隐藏 SurfaceView，显示 TextureView
        mBinding.surfaceView.visibility = android.view.View.GONE
        mBinding.textureView.visibility = android.view.View.VISIBLE

        // 使用安全切换（自动处理 stop/reprepare/恢复）
        player.safeSwitchToTextureView(mBinding.textureView)

        appendLog("正在切换到 TextureView 模式...")
    }

    // ==================== 播放控制 ====================

    /**
     * 初始化播放控制按钮监听器
     */
    private fun initPlaybackControlListeners() {
        // 播放按钮
        mBinding.btnPlay.setOnClickListener {
            val url = mBinding.etUrl.text.toString().trim()

            if (url.isNotEmpty()) {
                // 有自定义 URL，设置数据源并准备
                autoPlayOnPrepared = true
                player.setSource(url)
                appendLog("设置数据源: $url")
            } else {
                // 使用默认 URL 或根据状态决定操作
                if (player.state == PlayerState.PAUSED || player.state == PlayerState.COMPLETED) {
                    // 暂停或完成状态 → 直接继续播放
                    player.play()
                    appendLog("继续播放")
                } else if (player.state == PlayerState.PREPARED) {
                    // 准备就绪 → 开始播放
                    player.play()
                    appendLog("开始播放")
                } else if (player.state == PlayerState.IDLE) {
                    // IDLE 状态 → 设置默认测试 URL
                    autoPlayOnPrepared = true
                    player.setSource(DEFAULT_TEST_URL)
                    appendLog("设置默认测试视频")
                } else {
                    appendLog("无法播放，当前状态: ${player.state}")
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
            resetProgressUI()
            appendLog("停止播放")
        }

        // 选择本地视频
        mBinding.btnPickLocal.setOnClickListener {
            MediaPickerCore.launchVideo(videoPickerLauncher)
        }

        // 进度条拖动监听
        mBinding.seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && player.duration > 0) {
                    // 用户拖动时实时更新时间显示
                    val position = (progress.toFloat() / 100 * player.duration).toLong()
                    mBinding.tvProgress.text = "${formatTime(position)} / ${formatTime(player.duration)}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 开始拖动，可以在这里暂停进度更新
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 松手后执行 seekTo
                if (player.duration > 0) {
                    val position = (seekBar!!.progress.toFloat() / 100 * player.duration).toLong()
                    player.seekTo(position)
                    appendLog("Seek 到: ${formatTime(position)}")
                }
            }
        })
    }

    /**
     * 选择本地视频文件
     */
    private fun pickLocalVideo() {

    }

    // ==================== 变速控制 ====================

    /**
     * 初始化变速控制监听器
     *
     * 支持 0.5x ~ 3.0x 倍速播放
     * SeekBar 范围：0-50，对应倍率：(progress + 5) / 10f
     * 例如：
     * - progress=0 → 0.5x
     * - progress=10 → 1.5x
     * - progress=25 → 3.0x
     */
    private fun initSpeedControlListeners() {
        mBinding.seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 计算实际倍率：(progress + 5) / 10f
                    val speed = (progress + 5).toFloat() / 10f
                    player.speed = speed
                    mBinding.tvSpeed.text = String.format("%.1fx", speed)
                    appendLog("变速: ${String.format("%.1fx", speed)}")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // ==================== 音量控制 ====================

    /**
     * 初始化音量控制监听器
     *
     * 支持 0% ~ 100% 音量调节
     */
    private fun initVolumeControlListeners() {
        mBinding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress.toFloat() / 100f
                    player.volume = volume
                    mBinding.tvVolume.text = "$progress%"
                    appendLog("音量: $progress%")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // ==================== 循环模式控制 ====================

    /**
     * 初始化循环模式选择监听器
     */
    private fun initLoopModeListeners() {
        mBinding.radioGroupLoop.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                mBinding.rbLoopNone.id -> LoopMode.NONE
                mBinding.rbLoopSingle.id -> LoopMode.SINGLE
                mBinding.rbLoopAll.id -> LoopMode.ALL
                else -> LoopMode.NONE
            }
            player.loopMode = mode
            appendLog("循环模式: $mode")
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

    // ==================== 数据输入控制 ====================

    /**
     * 初始化数据源输入相关监听器
     */
    private fun initDataInputListeners() {
        // Assets 文件播放按钮
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

        // 重试次数设置
        mBinding.etRetryCount.setOnEditorActionListener { _, _, _ ->
            val count = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 0
            player.retryCount = count
            appendLog("重试次数设置为: $count")
            false
        }
    }

    // ==================== 播放器事件监听器 ====================

    /**
     * IjkVideoPlayer 事件监听器实现
     *
     * 处理所有播放器回调，更新 UI 和日志。
     */
    private val playerListener = object : IVideoPlayerListener {

        override fun onStateChanged(from: PlayerState, to: PlayerState) {
            activity?.runOnUiThread {
                updateStateUI(to)
                updateButtonStates()
            }
        }

        override fun onPrepared(durationMs: Long) {
            activity?.runOnUiThread {
                appendLog("准备完成，时长: ${formatTime(durationMs)}")

                // 如果设置了自动播放，立即开始
                if (autoPlayOnPrepared) {
                    autoPlayOnPrepared = false
                    player.play()
                    appendLog("自动开始播放")
                }
            }
        }

        override fun onProgress(currentMs: Long, durationMs: Long) {
            activity?.runOnUiThread {
                if (durationMs > 0 && !mBinding.seekBarProgress.isPressed) {
                    // 非用户拖动时才更新进度条
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
                appendLog("错误: [${PlayerErrorCode.getMessage(errorCode)}] $msg")
                updateButtonStates()
            }
            return false
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            activity?.runOnUiThread {
                mBinding.tvVideoSize.text = "${width}x${height}"
                appendLog("视频尺寸: ${width}x${height}")
            }
        }

        override fun onBufferingUpdate(percent: Int) {
            activity?.runOnUiThread {
                mBinding.tvBufferPercent.text = "$percent%"
            }
        }

        override fun onFirstFrameRendered() {
            activity?.runOnUiThread {
                appendLog("首帧渲染完成（视频开始显示）")
            }
        }

        override fun onBufferingStart() {
            activity?.runOnUiThread {
                mBinding.progressBarBuffering.visibility = android.view.View.VISIBLE
                appendLog("开始缓冲...")
            }
        }

        override fun onBufferingEnd() {
            activity?.runOnUiThread {
                mBinding.progressBarBuffering.visibility = android.view.View.GONE
                appendLog("缓冲结束")
            }
        }

        override fun onNetworkBandwidth(bps: Long) {
            // 此回调由 tcpSpeedMonitorJob 处理
        }

        override fun onLoopRestart() {
            activity?.runOnUiThread {
                appendLog("单曲循环重新开始")
            }
        }
    }

    // ==================== TCP 速度监控 ====================

    /**
     * 启动 TCP 下载速度监控协程
     *
     * 定时获取 IJKPlayer 的 TCP 速度并更新 UI。
     * 这是 IJKPlayer 特有的功能，可用于显示网络状况。
     */
    private fun startTcpSpeedMonitor() {
        stopTcpSpeedMonitor()

        tcpSpeedMonitorJob = lifecycleScope.launch(Dispatchers.Main) {
            while (isActive) {
                try {
                    val speed = player.tcpSpeed
                    if (speed > 0) {
                        val speedKBs = speed / 1024
                        val displayText = when {
                            speedKBs >= 1024 -> String.format("%.1f MB/s", speedKBs / 1024f)
                            else -> "$speedKBs KB/s"
                        }
                        mBinding.tvTcpSpeed.text = displayText
                    }
                } catch (_: Exception) {}

                delay(1000)  // 每秒更新一次
            }
        }
    }

    /**
     * 停止 TCP 速度监控
     */
    private fun stopTcpSpeedMonitor() {
        tcpSpeedMonitorJob?.cancel()
        tcpSpeedMonitorJob = null
    }

    // ==================== UI 更新方法 ====================

    /**
     * 更新状态显示 UI
     *
     * 根据不同状态显示不同颜色的文字：
     * - IDLE/PREPARING/ERROR: 灰色/橙色/红色
     * - PREPARED/PLAYING: 蓝色/绿色
     * - PAUSED/COMPLETED: 深蓝色/紫色
     */
    private fun updateStateUI(state: PlayerState) {
        val stateText = "状态: $state"
        val colorRes = when (state) {
            PlayerState.IDLE -> android.graphics.Color.GRAY
            PlayerState.PREPARING -> android.graphics.Color.parseColor("#FF9800")  // 橙色
            PlayerState.PREPARED -> android.graphics.Color.BLUE
            PlayerState.PLAYING -> android.graphics.Color.GREEN
            PlayerState.PAUSED -> android.graphics.Color.parseColor("#2196F3")  // 深蓝色
            PlayerState.STOPPED -> android.graphics.Color.GRAY
            PlayerState.COMPLETED -> android.graphics.Color.parseColor("#9C27B0")  // 紫色
            PlayerState.ERROR -> android.graphics.Color.RED
            PlayerState.RELEASED -> android.graphics.Color.GRAY
        }

        mBinding.tvState.text = stateText
        mBinding.tvState.setTextColor(colorRes)

        // PREPARING 状态时重置进度条 UI
        if (state == PlayerState.PREPARING) {
            resetProgressUI()
        }
    }

    /**
     * 更新按钮可用状态
     *
     * 根据当前播放器状态启用或禁用按钮：
     * - 播放按钮：PREPARED/PAUSED/COMPLETED 时可用
     * - 暂停按钮：PLAYING 时可用
     * - 停止按钮：非 IDLE/RELEASED 时可用
     */
    private fun updateButtonStates() {
        mBinding.btnPlay.isEnabled = when (player.state) {
            PlayerState.PREPARED,
            PlayerState.PAUSED,
            PlayerState.COMPLETED,
            PlayerState.IDLE -> true
            else -> false
        }.also {
            if (it && player.state == PlayerState.PAUSED) {
                mBinding.btnPlay.text = "继续"
            } else {
                mBinding.btnPlay.text = "播放"
            }
        }

        mBinding.btnPause.isEnabled = (player.state == PlayerState.PLAYING)
        mBinding.btnStop.isEnabled = (player.state != PlayerState.IDLE &&
                player.state != PlayerState.RELEASED)
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
     * 格式化时间为 mm:ss 或 hh:mm:ss 格式
     */
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * 追加日志文本到日志区域
     *
     * 自动添加时间戳并滚动到底部。
     */
    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())

        val logText = "[$timestamp] $message\n"

        mBinding.tvLog.append(logText)

        // 自动滚动到底部
        // 自动滚动到底部
        val scrollView = mBinding.tvLog.parent as? android.widget.ScrollView
        scrollView?.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    // ==================== 生命周期管理 ====================

    override fun onDestroyView() {
        super.onDestroyView()

        // 停止 TCP 速度监控
        stopTcpSpeedMonitor()

        // 解绑 Surface 但保留 IjkMediaPlayer（用于配置变更恢复）
        if (!requireActivity().isChangingConfigurations) {
            player.detach()
            appendLog("Fragment 销毁，解绑 Surface")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 完全释放所有资源（仅在真正销毁时释放）
        if (!requireActivity().isChangingConfigurations) {
            player.release()
            appendLog("Fragment 彻底销毁，释放所有资源")
        }
    }

    companion object {
        /** 默认测试视频 URL（可替换为实际可用的地址）*/
        private const val DEFAULT_TEST_URL =
            "https://www.w3schools.com/html/mov_bbb.mp4"
    }
}
