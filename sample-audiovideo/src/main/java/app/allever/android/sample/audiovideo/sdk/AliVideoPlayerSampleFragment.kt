package app.allever.android.sample.audiovideo.sdk

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.android.IVideoPlayerListener
import app.allever.android.sample.audiovideo.android.LoopMode
import app.allever.android.sample.audiovideo.android.PlayerState
import app.allever.android.sample.audiovideo.databinding.FragmentSdkAliVideoPlayerSampleBinding
import com.aliyun.player.nativeclass.CacheConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AliVideoPlayer (阿里云SDK) 视频播放器示例
 *
 * 功能演示：
 * 1. URL/VidAuth/VidSts 三种数据源播放
 * 2. 播放控制：播放、暂停、停止、进度拖动
 * 3. 变速播放 (0.5x ~ 3.0x)
 * 4. 音量控制 (0% ~ 100%)
 * 5. 循环模式切换（不循环/单曲循环/列表循环）
 * 6. 阿里云特有功能：
 *    - 硬件解码/软件解码切换
 *    - 画质轨道切换（多清晰度）
 *    - 镜像显示（水平翻转）
 *    - 视频旋转（90°步进）
 *    - 截图功能
 * 7. 缓存配置
 * 8. TraceId 追踪启用
 * 9. 本地视频文件选择
 * 10. Assets 文件播放
 */
class AliVideoPlayerSampleFragment :
    BaseFragment<FragmentSdkAliVideoPlayerSampleBinding, BaseViewModel>() {

    private lateinit var player: AliVideoPlayer
    private var isUserTracking = false // 用户是否正在拖动进度条

    // 默认测试视频URL
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    override fun inflate() = FragmentSdkAliVideoPlayerSampleBinding.inflate(layoutInflater)

    override fun init() {
        initPlayer()
        initUIControls()
        setupListeners()
        log("AliVideoPlayer 示例初始化完成")
    }

    /**
     * 初始化阿里云播放器并绑定到容器
     */
    private fun initPlayer() {
        // 在 FrameLayout 中创建 SurfaceView 用于视频显示
        val surfaceView = android.view.SurfaceView(requireContext()).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        mBinding.videoContainer.addView(surfaceView)

        player = AliVideoPlayer().apply {
            attach(surfaceView)  // 绑定到 SurfaceView
            setListener(playerListener)
            retryCount = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 3
            progressIntervalMs = 200  // 进度回调间隔 200ms
            autoPlayOnPrepared = true  // 准备完成后自动播放
        }
        log("✅ AliVideoPlayer 初始化完成")
        log("   容器尺寸: ${mBinding.videoContainer.width}x${mBinding.videoContainer.height}")
    }

    /**
     * 初始化 UI 控件
     */
    private fun initUIControls() {
        // 设置默认测试URL
        mBinding.etUrl.setText(defaultTestUrl)

        // 初始化变速SeekBar（范围 5-30，对应 0.5x-3.0x，步长 0.05x）
        mBinding.seekBarSpeed.progress = 10  // 默认 1.0x
        updateSpeedDisplay(1.0f)

        // 初始化音量SeekBar
        mBinding.seekBarVolume.progress = 100  // 默认 100%
        updateVolumeDisplay(100)

        // 初始化画质轨道Spinner
        val qualityAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("等待加载...")
        )
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinnerQualityTrack.adapter = qualityAdapter
    }

    /**
     * 设置所有监听器
     */
    private fun setupListeners() {
        // ========== 播放控制按钮 ==========
        mBinding.btnPlay.setOnClickListener { playCurrentSource() }
        mBinding.btnPause.setOnClickListener { player.pause() }
        mBinding.btnStop.setOnClickListener { stopPlayback() }

        // 选择本地视频
        mBinding.btnPickLocal.setOnClickListener { pickLocalVideo() }

        // 播放Assets文件
        mBinding.btnPlayAsset.setOnClickListener { playAssetFile() }

        // ========== 进度条监听 ==========
        mBinding.seekBarProgress.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && player.duration > 0) {
                    val position = (progress.toFloat() / 100 * player.duration).toLong()
                    mBinding.tvProgress.text = formatTime(position) + " / " + formatTime(player.duration)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                isUserTracking = true
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                isUserTracking = false
                if (player.duration > 0) {
                    val position = (seekBar?.progress?.toFloat()?.div(100)?.times(player.duration) ?: 0f).toLong()
                    player.seekTo(position)
                    log("📍 用户跳转到: ${formatTime(position)}")
                }
            }
        })

        // ========== 变速控制 ==========
        mBinding.seekBarSpeed.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val speed = (progress + 5) / 10f  // 范围 0.5 ~ 3.5，但限制到 3.0
                    val clampedSpeed = speed.coerceIn(0.5f, 3.0f)
                    updateSpeedDisplay(clampedSpeed)
                    player.speed = clampedSpeed  // 使用属性赋值
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // ========== 音量控制 ==========
        mBinding.seekBarVolume.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateVolumeDisplay(progress)
                    player.volume = progress / 100f  // 使用属性赋值
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // ========== 循环模式 ==========
        mBinding.radioGroupLoop.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLoopNone -> {
                    player.loopMode = LoopMode.NONE
                    log("🔄 循环模式: 不循环")
                }
                R.id.rbLoopSingle -> {
                    player.loopMode = LoopMode.SINGLE
                    log("🔄 循环模式: 单曲循环")
                }
                R.id.rbLoopAll -> {
                    player.loopMode = LoopMode.ALL
                    log("🔄 循环模式: 列表循环")
                }
            }
        }

        // ========== 数据源类型切换 ==========
        mBinding.radioGroupSourceType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSourceUrl -> {
                    mBinding.layoutVidParams.visibility = android.view.View.GONE
                    log("📡 数据源类型: URL")
                }
                R.id.rbSourceVidAuth -> {
                    mBinding.layoutVidParams.visibility = android.view.View.VISIBLE
                    mBinding.etPlayAuth.visibility = android.view.View.VISIBLE
                    mBinding.etAccessKeyId.visibility = android.view.View.GONE
                    mBinding.etAccessKeySecret.visibility = android.view.View.GONE
                    mBinding.etSecurityToken.visibility = android.view.View.GONE
                    log("📡 数据源类型: VidAuth（需填写 Vid 和 PlayAuth）")
                }
                R.id.rbSourceVidSts -> {
                    mBinding.layoutVidParams.visibility = android.view.View.VISIBLE
                    mBinding.etPlayAuth.visibility = android.view.View.GONE
                    mBinding.etAccessKeyId.visibility = android.view.View.VISIBLE
                    mBinding.etAccessKeySecret.visibility = android.view.View.VISIBLE
                    mBinding.etSecurityToken.visibility = android.view.View.VISIBLE
                    log("📡 数据源类型: VidSts（需填写 Vid 和 STS 参数）")
                }
            }
        }

        // ========== 解码方式切换 ==========
        mBinding.radioGroupDecodeType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDecodeAuto -> {
                    player.useHardwareDecoder(true)  // 自动模式，优先硬解
                    log("⚙️ 解码方式: 自动（优先硬件解码）")
                }
                R.id.rbDecodeHard -> {
                    player.useHardwareDecoder(true)
                    log("⚙️ 解码方式: 强制硬件解码")
                }
                R.id.rbDecodeSoft -> {
                    player.useHardwareDecoder(false)
                    log("⚙️ 解码方式: 强制软件解码")
                }
            }
        }

        // ========== 画质轨道切换 ==========
        mBinding.spinnerQualityTrack.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    if (position > 0 && ::player.isInitialized) {
                        val tracks = player.getQualityTracks()
                        if (position <= tracks.size) {
                            val selectedTrack = tracks[position - 1]
                            player.switchQuality(selectedTrack)
                            log("🎬 切换画质: Track ${selectedTrack.index}")
                            Toast.makeText(
                                requireContext(),
                                "已切换到清晰度 ${selectedTrack.index}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        // ========== 镜像和旋转 ==========
        mBinding.btnMirrorHorizontal.setOnClickListener {
            player.setMirror(!isMirrored)
            isMirrored = !isMirrored
            log("🔲 水平镜像: ${if (isMirrored) "开启" else "关闭"}")
            Toast.makeText(
                requireContext(),
                "水平镜像: ${if (isMirrored) "开启" else "关闭"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        var currentRotation = 0
        mBinding.btnRotate90.setOnClickListener {
            currentRotation = (currentRotation + 90) % 360
            when (currentRotation) {
                0 -> player.setRotation(com.aliyun.player.IPlayer.RotateMode.ROTATE_0)
                90 -> player.setRotation(com.aliyun.player.IPlayer.RotateMode.ROTATE_90)
                180 -> player.setRotation(com.aliyun.player.IPlayer.RotateMode.ROTATE_180)
                270 -> player.setRotation(com.aliyun.player.IPlayer.RotateMode.ROTATE_270)
            }
            log("🔄 旋转角度: ${currentRotation}°")
            Toast.makeText(requireContext(), "旋转至 ${currentRotation}°", Toast.LENGTH_SHORT).show()
        }

        // 截图
        mBinding.btnCaptureFrame.setOnClickListener {
            captureAndSaveFrame()
        }
    }

    /**
     * 播放当前选择的数据源
     */
    private fun playCurrentSource() {
        when (mBinding.radioGroupSourceType.checkedRadioButtonId) {
            R.id.rbSourceUrl -> playUrl()
            R.id.rbSourceVidAuth -> playVidAuth()
            R.id.rbSourceVidSts -> playVidSts()
        }
    }

    /**
     * 播放URL数据源
     */
    private fun playUrl() {
        val url = mBinding.etUrl.text.toString().trim().ifEmpty { defaultTestUrl }
        log("🎥 开始播放 URL: $url")

        // 更新重试次数
        player.retryCount = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 3

        // 使用 setSource 方法设置URL源
        player.setSource(url)
        log("✅ 已设置URL数据源，准备中...")
    }

    /**
     * 播放VidAuth数据源（阿里云点播推荐方式）
     */
    private fun playVidAuth() {
        val vid = mBinding.etVid.text.toString().trim()
        val playAuth = mBinding.etPlayAuth.text.toString().trim()

        if (vid.isEmpty()) {
            Toast.makeText(requireContext(), "请输入视频ID (Vid)", Toast.LENGTH_SHORT).show()
            return
        }

        if (playAuth.isEmpty()) {
            Toast.makeText(requireContext(), "请输入播放凭证 (PlayAuth)", Toast.LENGTH_SHORT).show()
            return
        }

        log("🎥 开始播放 VidAuth: vid=$vid")

        // 更新重试次数
        player.retryCount = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 3

        // 使用 setVidAuth 方法设置VidAuth源
        player.setVidAuth(vid, playAuth)
        log("✅ 已设置VidAuth数据源，准备中...")
    }

    /**
     * 播放VidSts数据源（STS临时凭证方式）
     */
    private fun playVidSts() {
        val vid = mBinding.etVid.text.toString().trim()
        val accessKeyId = mBinding.etAccessKeyId.text.toString().trim()
        val accessKeySecret = mBinding.etAccessKeySecret.text.toString().trim()
        val securityToken = mBinding.etSecurityToken.text.toString().trim()
        val region = mBinding.etRegion.text.toString().trim().ifEmpty { "cn-shanghai" }

        if (vid.isEmpty()) {
            Toast.makeText(requireContext(), "请输入视频ID (Vid)", Toast.LENGTH_SHORT).show()
            return
        }

        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty() || securityToken.isEmpty()) {
            Toast.makeText(requireContext(), "请填写完整的 STS 参数", Toast.LENGTH_SHORT).show()
            return
        }

        log("🎥 开始播放 VidSts: vid=$vid, region=$region")

        // 更新重试次数
        player.retryCount = mBinding.etRetryCount.text.toString().toIntOrNull() ?: 3

        // 使用 setVidSts 方法设置VidSts源
        player.setVidSts(
            vid = vid,
            accessKeyId = accessKeyId,
            accessKeySecret = accessKeySecret,
            securityToken = securityToken,
            region = region
        )
        log("✅ 已设置VidSts数据源，准备中...")
    }

    /**
     * 停止播放
     */
    private fun stopPlayback() {
        player.stop()
        resetUI()
        log("⏹️ 停止播放")
    }

    /**
     * 重置 UI 到初始状态
     */
    private fun resetUI() {
        mBinding.tvState.text = "状态: IDLE"
        mBinding.tvProgress.text = "00:00 / 00:00"
        mBinding.seekBarProgress.progress = 0
        mBinding.btnPause.isEnabled = false
        mBinding.btnStop.isEnabled = false
    }

    /**
     * 选择本地视频文件
     */
    private fun pickLocalVideo() {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_PICK_VIDEO)
        log("📂 打开文件选择器...")
    }

    /**
     * 播放 Assets 文件
     */
    private fun playAssetFile() {
        val assetPath = mBinding.etAssetPath.text.toString().trim()
        if (assetPath.isEmpty()) {
            Toast.makeText(requireContext(), "请输入 Assets 文件路径", Toast.LENGTH_SHORT).show()
            return
        }

        log("📁 开始播放 Assets 文件: $assetPath")
        player.setSource(assetPath)
        log("✅ 已设置 Assets 数据源，准备中...")
    }

    /**
     * 截取当前帧并保存
     */
    private fun captureAndSaveFrame() {
        val bitmap = player.captureFrame()
        if (bitmap != null) {
            // 保存到相册或临时目录（示例仅显示Toast）
            log("📸 截图成功！尺寸: ${bitmap.width}x${bitmap.height}")
            Toast.makeText(requireContext(), "截图成功！尺寸: ${bitmap.width}x${bitmap.height}", Toast.LENGTH_SHORT).show()

            // 可选：保存到相册
            // saveBitmapToGallery(bitmap)
        } else {
            log("❌ 截图失败")
            Toast.makeText(requireContext(), "截图失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 配置缓存（可选）
     */
    private fun configureCache(enable: Boolean = true) {
        if (!enable) return

        try {
            val cacheConfig = CacheConfig().apply {
                // 缓存路径
                mDir = requireContext().cacheDir.absolutePath + "/ali_cache"
                // 单个文件最大缓存大小（MB）
                mMaxSizeMB = 500
                // 是否开启缓存
                mEnable = true
            }
            player.enableCache(cacheConfig)
            log("💾 缓存配置已启用: path=${cacheConfig.mDir}, maxSize=${cacheConfig.mMaxSizeMB}MB")
        } catch (_: Exception) {
            log("⚠️ 缓存配置失败（可能不支持）")
        }
    }

    /**
     * 启用 TraceId 追踪（用于性能监控）
     */
    private fun enableTraceTracking(enable: Boolean = true) {
        player.enableTraceId(enable)
        log("🔍 TraceId 追踪: ${if (enable) "已启用" else "已关闭"}")
    }

    /**
     * 更新画质轨道列表
     */
    private fun updateQualityTrackList() {
        val tracks = player.getQualityTracks()
        if (tracks.isEmpty()) {
            val adapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("无多清晰度")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mBinding.spinnerQualityTrack.adapter = adapter
            return
        }

        val trackNames = mutableListOf("默认") + tracks.mapIndexed { index, track ->
            "Track ${track.index}"
        }

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            trackNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinnerQualityTrack.adapter = adapter
        log("🎬 加载到 ${tracks.size} 个画质轨道")
    }

    // ==================== 监听器实现 ====================

    /**
     * 播放器事件监听器
     */
    private val playerListener = object : IVideoPlayerListener {
        override fun onPrepared(duration: Long) {
            activity?.runOnUiThread {
                log("📽️ 准备完成！时长: ${formatTime(duration)}")
                mBinding.tvState.text = "状态: PREPARED"
                mBinding.btnPause.isEnabled = true
                mBinding.btnStop.isEnabled = true

                // 更新画质轨道列表
                updateQualityTrackList()
            }
        }

        override fun onComplete() {
            activity?.runOnUiThread {
                log("🏁 播放完成")
                mBinding.tvState.text = "状态: COMPLETED"
            }
        }

        override fun onError(what: Int, extra: Int): Boolean {
            activity?.runOnUiThread {
                log("❌ 错误 [$what]: $extra")
                mBinding.tvState.text = "状态: ERROR ($what)"
                Toast.makeText(requireContext(), "播放错误: $extra", Toast.LENGTH_LONG).show()
            }
            return true  // 返回 true 表示错误已被消费
        }

        override fun onInfo(what: Int, extra: Int): Boolean {
            activity?.runOnUiThread {
                // 阿里云特有的事件处理
                when (what) {
                    // 可以根据实际 InfoCode 枚举值处理特定事件
                    else -> {
                        // 其他信息事件
                        log("ℹ️ onInfo: what=$what, extra=$extra")
                    }
                }
            }
            return true
        }

        override fun onBufferingStart() {
            activity?.runOnUiThread {
                log("⏳ 缓冲开始")
                mBinding.tvState.text = "状态: BUFFERING"
            }
        }

        override fun onBufferingEnd() {
            activity?.runOnUiThread {
                log("✅ 缓冲结束")
                if (player.isPlaying) {
                    mBinding.tvState.text = "状态: PLAYING"
                }
            }
        }

        override fun onBufferingUpdate(percent: Int) {
            activity?.runOnUiThread {
                // 可选：显示缓冲百分比
                // log("📊 缓冲: $percent%")
            }
        }

        override fun onProgress(position: Long, duration: Long) {
            activity?.runOnUiThread {
                if (!isUserTracking && duration > 0) {
                    val progressPercent = (position.toFloat() / duration * 100).toInt()
                    mBinding.seekBarProgress.progress = progressPercent
                    mBinding.tvProgress.text = "${formatTime(position)} / ${formatTime(duration)}"
                }
            }
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            activity?.runOnUiThread {
                log("📐 视频尺寸变化: ${width}x${height}")
                mBinding.tvVideoSize.text = "${width}x${height}"

                // 根据视频宽高比调整容器大小（可选）
                adjustContainerAspectRatio(width, height)
            }
        }

        override fun onFirstFrameRendered() {
            activity?.runOnUiThread {
                log("🖼️ 首帧渲染完成")
            }
        }

        override fun onStateChanged(oldState: PlayerState, newState: PlayerState) {
            activity?.runOnUiThread {
                log("🔄 状态转换: $oldState → $newState")
                mBinding.tvState.text = "状态: $newState"

                when (newState) {
                    PlayerState.PLAYING -> {
                        mBinding.btnPause.text = "暂停"
                    }
                    PlayerState.PAUSED -> {
                        mBinding.btnPause.text = "继续"
                    }
                    PlayerState.STOPPED, PlayerState.IDLE -> {
                        resetUI()
                    }
                    else -> {}
                }
            }
        }

        override fun onLoopRestart() {
            activity?.runOnUiThread {
                log("🔄 循环播放重新开始")
            }
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 格式化时间显示
     */
    private fun formatTime(timeMs: Long): String {
        if (timeMs < 0) return "00:00"
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    /**
     * 更新速度显示
     */
    private fun updateSpeedDisplay(speed: Float) {
        mBinding.tvSpeed.text = "${String.format("%.1f", speed)}x"
    }

    /**
     * 更新音量显示
     */
    private fun updateVolumeDisplay(volume: Int) {
        mBinding.tvVolume.text = "$volume%"
    }

    /**
     * 根据视频宽高比调整容器尺寸
     */
    private fun adjustContainerAspectRatio(videoWidth: Int, videoHeight: Int) {
        if (videoWidth <= 0 || videoHeight <= 0) return

        val containerWidth = mBinding.videoContainer.width
        if (containerWidth <= 0) return

        val aspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
        val targetHeight = (containerWidth / aspectRatio).toInt()

        // 限制高度在合理范围内（200dp ~ 400dp）
        val minHeight = dpToPx(200)
        val maxHeight = dpToPx(400)
        val finalHeight = targetHeight.coerceIn(minHeight, maxHeight)

        mBinding.videoContainer.layoutParams.height = finalHeight
        mBinding.videoContainer.requestLayout()

        log("📐 调整容器尺寸: ${containerWidth}x$finalHeight (比例: ${String.format("%.2f", aspectRatio)})")
    }

    /**
     * dp 转 px
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * 记录日志
     */
    private fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message\n"
        mBinding.tvLog.append(logMessage)

        // 保持日志不超过一定行数
        val logText = mBinding.tvLog.text.toString()
        if (logText.lines().size > MAX_LOG_LINES) {
            val lines = logText.lines().takeLast(MAX_LOG_LINES / 2)
            mBinding.tvLog.text = lines.joinToString("\n") + "\n"
        }

        // 自动滚动到底部
        val scrollView = mBinding.tvLog.parent as? android.widget.ScrollView
        scrollView?.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::player.isInitialized) {
            player.release()
            log("🗑️ AliVideoPlayer 已释放")
        }
    }

    companion object {
        private const val REQUEST_PICK_VIDEO = 1001
        private const val MAX_LOG_LINES = 200
        private var isMirrored = false
    }
}
