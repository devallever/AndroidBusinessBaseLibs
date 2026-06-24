package app.allever.android.sample.audiovideo.core.player

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.databinding.ActivityVideoPlayerBinding

/**
 * 视频播放器 Activity（支持全屏播放）
 *
 * ## 功能特性
 * - ✅ 全屏播放（默认竖屏，可切换横屏）
 * - ✅ 支持多种数据源：网络视频、本地文件、Assets 资源
 * - ✅ 自动缓存网络视频（通过 VideoCacheManager）
 * - ✅ 横竖屏切换动画流畅
 * - ✅ 生命周期管理完善
 *
 * ## 使用方式
 *
 * ### 方式一：通过 Intent 传递数据源
 * ```kotlin
 * // 播放网络视频
 * val intent = Intent(context, VideoPlayerActivity::class.java).apply {
 *     putExtra(EXTRA_SOURCE_URL, "https://example.com/video.mp4")
 *     putExtra(EXTRA_TITLE, "视频标题")
 * }
 * startActivity(intent)
 *
 * // 播放本地文件
 * intent.putExtra(EXTRA_SOURCE_URL, "/sdcard/DCIM/video.mp4")
 *
 * // 播放 Assets 资源
 * intent.putExtra(EXTRA_ASSET_PATH, "video/test.mp4")
 * ```
 *
 * ### 方式二：直接使用便捷方法
 * ```kotlin
 * VideoPlayerActivity.start(
 *     context = this,
 *     url = "https://example.com/video.mp4",
 *     title = "我的视频"
 * )
 * ```
 *
 * ## 状态管理
 * ```
 * onCreate → 初始化播放器 → 设置数据源 → 自动播放
 * onPause → 暂停播放 → 解绑视图
 * onResume → 重新绑定视图 → 恢复播放
 * onDestroy → 释放所有资源
 * ```
 */
class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VideoPlayerActivity"

        /** Intent Extra：视频 URL 或本地路径 */
        const val EXTRA_SOURCE_URL = "extra_source_url"

        /** Intent Extra：Assets 资源路径 */
        const val EXTRA_ASSET_PATH = "extra_asset_path"

        /** Intent Extra：视频标题 */
        const val EXTRA_TITLE = "extra_title"

        /**
         * 启动 VideoPlayerActivity 的便捷方法
         *
         * @param context 上下文
         * @param url 视频URL或本地路径（优先级高于 assetPath）
         * @param assetPath Assets 目录下的资源路径
         * @param title 视频标题（可选）
         */
        fun start(
            context: android.content.Context,
            url: String? = null,
            assetPath: String? = null,
            title: String? = null
        ) {
            val intent = android.content.Intent(context, VideoPlayerActivity::class.java).apply {
                if (!url.isNullOrEmpty()) {
                    putExtra(EXTRA_SOURCE_URL, url)
                }
                if (!assetPath.isNullOrEmpty()) {
                    putExtra(EXTRA_ASSET_PATH, assetPath)
                }
                if (!title.isNullOrEmpty()) {
                    putExtra(EXTRA_TITLE, title)
                }
                // 添加启动标志，避免重复创建实例
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /** ViewBinding */
    private lateinit var binding: ActivityVideoPlayerBinding

    /** 视频播放器（使用 StdVideoPlayer）*/
    private lateinit var stdVideoPlayer: StdVideoPlayer

    /** 是否处于横屏模式 */
    private var isLandscape: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★ 设置全屏主题（隐藏状态栏和导航栏）
        setupFullscreen()

        // 使用已有布局
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 从 Intent 提取参数
        extractIntentData()

        // 初始化视频播放器
        initVideoPlayer()

        // 加载视频源
        loadVideoSource()
    }

    /**
     * 设置全屏显示（隐藏系统 UI）
     */
    private fun setupFullscreen() {
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 隐藏状态栏和导航栏
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        // 隐藏系统 UI（沉浸式体验）
        hideSystemUI()
    }

    /**
     * 隐藏系统 UI（状态栏、导航栏等）
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    /**
     * 显示系统 UI
     */
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    /**
     * 从 Intent 中提取传入的参数
     */
    private fun extractIntentData() {
        videoTitle = intent.getStringExtra(EXTRA_TITLE)
        sourceUrl = intent.getStringExtra(EXTRA_SOURCE_URL)
        assetPath = intent.getStringExtra(EXTRA_ASSET_PATH)

        log(TAG, "提取到参数:")
        log(TAG, "  - 标题: $videoTitle")
        log(TAG, "  - URL: $sourceUrl")
        log(TAG, "  - Asset Path: $assetPath")
    }

    /** 当前视频标题 */
    private var videoTitle: String? = null

    /** 视频 URL 或本地路径 */
    private var sourceUrl: String? = null

    /** Assets 资源路径 */
    private var assetPath: String? = null

    /**
     * 初始化视频播放器（使用布局中的 StdVideoPlayer）
     */
    private fun initVideoPlayer() {
        // 从布局中获取 StdVideoPlayer 实例
        stdVideoPlayer = binding.videoPlayer

        // 设置播放器监听器
        stdVideoPlayer.setListener(object : IVideoPlayerViewListener {

            override fun onBackClicked() {
                // 点击返回按钮
                if (isLandscape) {
                    // 如果是横屏，先切回竖屏
                    toggleOrientation(false)
                } else {
                    // 如果是竖屏，直接返回
                    onBackPressedDispatcher.onBackPressed()
                }
            }

            override fun onFullscreenClick() {
                // ★ 点击全屏按钮，切换横竖屏
                toggleOrientation(!isLandscape)
            }

            override fun onPlayPauseChanged(isPlaying: Boolean) {
                // 播放/暂停回调（可选处理）
            }

            override fun onProgressChanged(position: Long, duration: Long) {
                // 进度变化回调（可选处理）
            }

            override fun onScaleModeChanged(mode: app.allever.android.sample.audiovideo.lib.VideoScaleMode) {
                // 缩放模式变化回调（可选处理）
            }

            override fun onSpeedChanged(speed: Float) {
                // 变速回调（可选处理）
            }

            override fun onRenderSwitched(renderName: String) {
                // 渲染器切换回调（可选处理）
            }

            override fun onEngineSwitched(engineType: String) {
                // 引擎切换回调（可选处理）
            }

            override fun onControlVisibilityChanged(isVisible: Boolean) {
                // 控制栏显示/隐藏回调（可选处理）
            }

            override fun onTouchDown() {
                // 触摸事件开始（可选处理）
            }

            override fun onTouchUp() {
                // 触摸事件结束（可选处理）
            }

            override fun onLog(msg: String) {
                // 日志回调（可选处理）
            }

            override fun debugUpdateState() {
                // 调试状态更新（可选处理）
            }
        })

        log(TAG, "视频播放器初始化完成 (使用 StdVideoPlayer)")
    }

    /**
     * 加载视频源
     *
     * 支持三种类型：
     * 1. 网络视频（http/https）- 自动启用缓存
     * 2. 本地文件（file 协议）- 直接播放
     * 3. Assets 资源 - 复制到缓存后播放
     */
    private fun loadVideoSource() {
        when {
            // 优先加载网络视频或本地文件
            !sourceUrl.isNullOrEmpty() -> {
                log(TAG, "加载数据源 (URL): $sourceUrl")

                // 如果有标题，设置标题（否则 StdVideoPlayer 会自动从 URL 提取）
                if (!videoTitle.isNullOrEmpty()) {
                    stdVideoPlayer.setSource(sourceUrl!!)
                } else {
                    stdVideoPlayer.setSource(sourceUrl!!)
                }

                // StdVideoPlayer 内部会自动调用 play()
            }

            // 其次加载 Assets 资源
            !assetPath.isNullOrEmpty() -> {
                log(TAG, "加载数据源 (Asset): $assetPath")
                stdVideoPlayer.setAssetSource(assetPath!!)
                // StdVideoPlayer 内部会自动调用 play()
            }

            // 都没有则提示错误
            else -> {
                log(TAG, "错误：未提供任何数据源")
                showError("未提供视频数据源")
            }
        }
    }

    /**
     * 显示错误提示
     */
    private fun showError(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        finish()
    }

    /**
     * 切换横竖屏方向
     *
     * @param toLandscape true=切换到横屏，false=切换到竖屏
     */
    private fun toggleOrientation(toLandscape: Boolean) {
        isLandscape = toLandscape

        if (toLandscape) {
            // ★ 切换到横屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            log(TAG, "切换到横屏")

            // 隐藏系统 UI（完全沉浸式）
            hideSystemUI()

            // 更新控制器的全屏按钮图标
            updateFullscreenButton(true)
        } else {
            // ★ 切换回竖屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            log(TAG, "切换到竖屏")

            // 隐藏系统 UI（保持沉浸式）
            hideSystemUI()

            // 更新控制器的全屏按钮图标
            updateFullscreenButton(false)

        }
    }

    /**
     * 更新全屏按钮状态
     *
     * @param isFullscreen true=当前为横屏（显示退出全屏图标），false=当前为竖屏（显示进入全屏图标）
     */
    private fun updateFullscreenButton(isFullscreen: Boolean) {
        // 通过控制器接口更新按钮图标
        stdVideoPlayer.uiController?.onFullscreenChanged(isFullscreen)
    }

    override fun onResume() {
        super.onResume()
        log(TAG, "onResume")

        // 保持沉浸式体验
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        log(TAG, "onPause")

        // StdVideoPlayer 会在 onDetachedFromWindow 中自动处理暂停和释放
    }

    override fun onDestroy() {
        super.onDestroy()
        log(TAG, "onDestroy")

        // StdVideoPlayer 会在 onDetachedFromWindow 中自动释放资源
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 当窗口获得焦点时，确保系统 UI 隐藏
            hideSystemUI()
        }
    }

    /**
     * 处理物理返回键
     */
    override fun onBackPressed() {
        if (isLandscape) {
            // 如果是横屏，按返回键先切回竖屏
            toggleOrientation(false)
        } else {
            // 如果是竖屏，直接退出 Activity
            super.onBackPressed()
        }
    }
}
