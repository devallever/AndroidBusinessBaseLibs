package app.allever.android.sample.audiovideo.core.player

import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerConfig
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.core.render.RenderRegistry
import app.allever.android.sample.audiovideo.databinding.FragmentVideoPlayerViewSampleBinding
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * VideoPlayerView 示例 Fragment
 *
 * ## 演示内容
 * 1. **VideoPlayerView 完整功能**：控制栏、手势、进度条等
 * 2. **动态配置**：运行时修改 PlayerConfig
 * 3. **渲染器切换**：通过 ChipGroup 选择渲染器类型
 * 4. **引擎切换**：通过 ChipGroup 选择引擎类型
 * 5. **多种数据源**：URL、本地文件、Assets
 * 6. **事件监听**：IVideoPlayerViewListener 回调演示
 *
 * ## 使用示例
 * ```kotlin
 * // 基础使用
 * playerView.setSource("https://example.com/video.mp4")
 * playerView.play()
 *
 * // 切换渲染器
 * playerView.switchRender("SurfaceView")
 *
 * // 切换引擎
 * playerView.switchEngine("MediaPlayer")
 *
 * // 设置监听器
 * playerView.listener = object : IVideoPlayerViewListener {
 *     override fun onBackClicked() { activity?.onBackPressed() }
 * }
 * ```
 */
class VideoPlayerViewSampleFragment :
    BaseFragment<FragmentVideoPlayerViewSampleBinding, BaseViewModel>() {

    /** 默认测试视频 URL */
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** 当前播放的 URL（用于重新加载）*/
    private var currentUrl: String? = null

    /** 当前是否正在播放 */
    private var isPlaying: Boolean = false

    /** 视频选择器 */
    private val videoPickerLauncher = MediaPickerCore.registerPickerLauncher(this) { items ->
        items.firstOrNull()?.let { mediaItem ->
            if (mediaItem is MediaItem.Video) {
                currentUrl = mediaItem.uri.toString()
                appendLog("选择本地视频: ${mediaItem.name} (${mediaItem.uri})")
                mBinding.videoPlayerView.setSource(currentUrl!!)
                mBinding.videoPlayerView.play()
            }
        }
    }

    override fun inflate(): FragmentVideoPlayerViewSampleBinding =
        FragmentVideoPlayerViewSampleBinding.inflate(layoutInflater)

    override fun init() {
        initRenderRegistry()
        initPlayerView()
        initViews()
        initRenderEngineChips()
        appendLog("=== VideoPlayerView 示例 ===")
        appendLog("初始化完成，请点击播放按钮开始测试")
    }

    /**
     * 初始化渲染器注册表
     */
    private fun initRenderRegistry() {
        RenderRegistry.registerBuiltInRenders()
        appendLog("已注册 ${RenderRegistry.getRegisteredCount()} 个内置渲染器: ${RenderRegistry.getAvailableRenders()}")
    }

    /**
     * 初始化 VideoPlayerView 并设置监听器
     */
    private fun initPlayerView() {
        // 自定义配置示例
        mBinding.videoPlayerView.apply {
            updateConfig {
                autoHideControlDelay = 4000L  // 4秒后自动隐藏
            }

            // 设置监听器
            listener = playerViewListener

            appendLog("VideoPlayerView 配置完成:")
            appendLog("  - 自动隐藏延迟: 4秒")
        }
    }

    /**
     * 初始化视图和事件监听
     */
    private fun initViews() {
        // 设置默认 URL
        mBinding.etUrl.setText(defaultTestUrl)

        // ==================== 播放控制 ====================

        // 播放网络视频
        mBinding.btnSetUrl.setOnClickListener {
            val url = mBinding.etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                playVideo(url, "网络视频")
            } else {
                toast("请输入视频 URL")
                appendLog("错误: 请输入视频 URL")
            }
        }

        // 播放 Assets 文件
        mBinding.btnSetAsset.setOnClickListener {
            val assetPath = mBinding.etAssetPath.text.toString().trim()
            if (assetPath.isNotEmpty()) {
                playAsset(assetPath)
            } else {
                toast("请输入文件名")
                appendLog("错误: 请输入 Asset 文件名")
            }
        }

        // 选择本地文件
        mBinding.btnSeletFile.setOnClickListener {
            MediaPickerCore.launchVideo(videoPickerLauncher)
        }

        // 应用设置并重新加载
        mBinding.btnApplySettings.setOnClickListener {
            applySettingsAndReload()
        }

        // 清空日志
        mBinding.btnClearLog.setOnClickListener {
            mBinding.tvLog.text = ""
            appendLog("日志已清空")
        }
    }

    /**
     * 初始化渲染器和引擎选择 RadioGroup
     */
    private fun initRenderEngineChips() {
        // 渲染器选择
        mBinding.radioGroupRender.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSurfaceView -> appendLog("[选择] 渲染器: SurfaceView")
                R.id.rbTextureView -> appendLog("[选择] 渲染器: TextureView")
                R.id.rbVideoView -> appendLog("[选择] 渲染器: VideoView")
            }
        }

        // 引擎选择
        mBinding.radioGroupEngine.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMediaPlayer -> appendLog("[选择] 引擎: MediaPlayer")
                R.id.rbMedia3 -> appendLog("[选择] 引擎: Media3 (ExoPlayer)")
                R.id.rbIjkPlayer -> appendLog("[选择] 引擎: IJKPlayer")
            }
        }

        // 默认已选中（在 XML 中设置 android:checked="true"）
    }

    /**
     * 获取选中的渲染器名称
     */
    private fun getSelectedRenderName(): String {
        return when (mBinding.radioGroupRender.checkedRadioButtonId) {
            R.id.rbSurfaceView -> "SurfaceView"
            R.id.rbTextureView -> "TextureView"
            R.id.rbVideoView -> "VideoView"
            else -> "SurfaceView"  // 默认
        }
    }

    /**
     * 获取选中的引擎类型
     */
    private fun getSelectedEngineType(): String {
        return when (mBinding.radioGroupEngine.checkedRadioButtonId) {
            R.id.rbMediaPlayer -> "MediaPlayer"
            R.id.rbMedia3 -> "Media3"
            R.id.rbIjkPlayer -> "IJKPlayer"
            else -> "MediaPlayer"  // 默认
        }
    }

    /**
     * 应用设置并重新加载视频
     */
    private fun applySettingsAndReload() {
        val renderName = getSelectedRenderName()
        val engineType = getSelectedEngineType()

        appendLog("=" .repeat(40))
        appendLog("应用设置:")
        appendLog("  - 渲染器: $renderName")
        appendLog("  - 引擎: $engineType")

        // 如果有正在播放的视频，先保存状态
        val wasPlaying = isPlaying
        val currentPosition = if (currentUrl != null && wasPlaying) {
            try {
                mBinding.videoPlayerView.currentPosition
            } catch (_: Exception) { 0L }
        } else {
            0L
        }

        // 切换渲染器
        mBinding.videoPlayerView.switchRender(renderName)

        // 切换引擎
        mBinding.videoPlayerView.switchEngine(engineType)

        // 如果之前在播放，恢复播放
        if (currentUrl != null && wasPlaying) {
            appendLog("恢复播放: $currentUrl")
            mBinding.videoPlayerView.setSource(currentUrl!!)
            mBinding.videoPlayerView.play()
            if (currentPosition > 0) {
                mBinding.videoPlayerView.seekTo(currentPosition)
            }
        } else {
            appendLog("设置已应用，等待播放操作")
        }
    }

    /**
     * 播放视频
     */
    private fun playVideo(url: String, sourceType: String) {
        currentUrl = url
        appendLog("=" .repeat(40))
        appendLog("开始播放 $sourceType: $url")

        // 提取标题显示
        val title = mBinding.videoPlayerView.extractTitle(url)
        appendLog("提取标题: $title")

        // 设置数据源并播放
        mBinding.videoPlayerView.setSource(url)
        mBinding.videoPlayerView.play()

        isPlaying = true
        appendLog("调用 setSource() 和 play()")
    }

    /**
     * 播放 Assets 文件
     */
    private fun playAsset(assetPath: String) {
        currentUrl = "asset://$assetPath"
        appendLog("=" .repeat(40))
        appendLog("开始播放 Assets 文件: $assetPath")

        // 提取标题显示
        val title = mBinding.videoPlayerView.extractTitle(assetPath)
        appendLog("提取标题: $title")

        // 设置数据源并播放（内部会复制到缓存目录）
        mBinding.videoPlayerView.setAssetSource(assetPath)
        mBinding.videoPlayerView.play()

        isPlaying = true
        appendLog("调用 setAssetSource() 和 play()")
        appendLog("注意: Assets 文件会先复制到缓存目录再播放")
    }

    /**
     * VideoPlayerView 监听器实现
     *
     * 演示如何响应 VideoPlayerView 的各种交互事件。
     */
    private val playerViewListener = object : IVideoPlayerViewListener {

        override fun onBackClicked() {
            appendLog("[事件] onBackClicked: 用户点击返回按钮")
            // 可以在这里处理返回逻辑，例如：
            // activity?.onBackPressed()
            // 或者返回上一级页面
        }

        override fun onPlayPauseChanged(isPlayingState: Boolean) {
            isPlaying = isPlayingState
            appendLog("[事件] onPlayPauseChanged: ${if (isPlayingState) "播放" else "暂停"}")
        }

        override fun onProgressChanged(position: Long, duration: Long) {
            // 进度更新事件（可选处理）
            // 注意：此回调会频繁触发，建议只在需要时启用
            // appendLog("[进度] ${formatTime(position)} / ${formatTime(duration)}")
        }

        override fun onScaleModeChanged(mode: VideoScaleMode) {
            appendLog("[事件] onScaleModeChanged: $mode")
            when (mode) {
                VideoScaleMode.FIT_CENTER -> appendLog("  → 适应屏幕（保持比例）")
                VideoScaleMode.CROP_CENTER -> appendLog("  → 裁剪填充（填满屏幕）")
                VideoScaleMode.STRETCH -> appendLog("  → 拉伸（可能变形）")
            }
        }

        override fun onSpeedChanged(speed: Float) {
            appendLog("[事件] onSpeedChanged: ${String.format(Locale.US, "%.1fx", speed)}")
        }

        override fun onRenderSwitched(renderName: String) {
            appendLog("[事件] onRenderSwitched: $renderName")
            appendLog("  → 当前渲染器已切换到: $renderName")

            // 更新 UI 状态以保持同步
            updateRadioSelection(mBinding.radioGroupRender, renderName, mapOf(
                R.id.rbSurfaceView to "SurfaceView",
                R.id.rbTextureView to "TextureView",
                R.id.rbVideoView to "VideoView"
            ))
        }

        override fun onEngineSwitched(engineType: String) {
            appendLog("[事件] onEngineSwitched: $engineType")
            appendLog("  → 当前引擎已切换到: $engineType")

            // 更新 UI 状态以保持同步
            updateRadioSelection(mBinding.radioGroupEngine, engineType, mapOf(
                R.id.rbMediaPlayer to "MediaPlayer",
                R.id.rbMedia3 to "Media3",
                R.id.rbIjkPlayer to "IJKPlayer"
            ))
        }

        override fun onControlVisibilityChanged(isVisible: Boolean) {
            appendLog("[事件] onControlVisibilityChanged: ${if (isVisible) "显示" else "隐藏"}")
        }

        override fun onTouchDown() {
            // 触摸按下（可用于暂停其他逻辑）
        }

        override fun onTouchUp() {
            // 触摸抬起
        }
    }

    /**
     * 更新 RadioGroup 选择状态
     *
     * @param radioGroup RadioGroup 实例
     * @param value 目标值
     * @param radioValueMap RadioButton ID 到值的映射
     */
    private fun updateRadioSelection(
        radioGroup: RadioGroup,
        value: String,
        radioValueMap: Map<Int, String>
    ) {
        for ((radioId, radioValue) in radioValueMap) {
            if (radioValue == value) {
                if (radioGroup.checkedRadioButtonId != radioId) {
                    radioGroup.check(radioId)
                }
                return
            }
        }
    }

    // ==================== 日志工具方法 ====================

    /**
     * 追加日志文本
     */
    private fun appendLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] $message\n"

        activity?.runOnUiThread {
            mBinding.tvLog.append(logLine)
            val scrollView = mBinding.tvLog.parent as? android.widget.ScrollView
            scrollView?.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    /**
     * 格式化时间
     */
    private fun formatTime(timeMs: Long): String {
        return mBinding.videoPlayerView.formatTime(timeMs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放 VideoPlayerView 资源
        try {
            mBinding.videoPlayerView.release()
            appendLog("VideoPlayerView 已释放")
        } catch (_: Exception) {}
    }
}
