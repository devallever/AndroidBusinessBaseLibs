package app.allever.android.sample.audiovideo.core.render

import android.content.Context
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.engine.IPlayerEngine
import app.allever.android.sample.audiovideo.core.engine.Media3PlayerEngine
import app.allever.android.sample.audiovideo.lib.ExoPlayerHelper
import app.allever.android.sample.audiovideo.lib.VideoScaleMode

/**
 * ExoPlayer PlayerView 渲染器实现（基于 Google Media3 PlayerView）
 *
 * ## 职责
 * - 管理 [PlayerView] 的创建和生命周期
 * - 将 PlayerView 与 [Media3PlayerEngine] 绑定
 * - 处理 Surface 的就绪/销毁回调
 * - 支持布局自适应（根据视频尺寸和缩放模式）
 *
 * ## PlayerView 特点（与 SurfaceView/TextureView/VideoView 的区别）
 * **优势：**
 * ✅ **官方集成**：Google 官方提供的 ExoPlayer 专用 View
 * ✅ **功能丰富**：内置字幕显示、缓冲动画、错误提示等
 * ✅ **自动管理**：自动处理 Surface 创建、销毁、尺寸变化
 * ✅ **性能优化**：针对 ExoPlayer 深度优化，支持硬件加速
 * ✅ **可定制**：支持自定义控制器、覆盖层、装饰视图
 * ✅ **自适应**：自动处理视频宽高比和屏幕旋转
 * ✅ **手势支持**：内置滑动调节音量、亮度、进度等手势
 *
 * **劣势：**
 * ❌ **依赖绑定**：只能配合 ExoPlayer/Media3 使用
 * ❌ **体积较大**：相比纯 SurfaceView 多出 UI 控制组件
 * ❌ **灵活性受限**：部分行为由 PlayerView 内部控制
 * ❌ **学习成本**：需要了解 ExoPlayer 的 API 和概念
 *
 * ## 适用场景
 * - **使用 ExoPlayer 引擎时首选**：最佳集成体验
 * - **需要内置控制器的场景**：减少自定义开发工作
 * - **在线视频应用**：YouTube、Netflix 风格的播放器
 * - **需要字幕支持的应用**：内置字幕渲染能力
 * - **快速原型开发**：开箱即用的完整播放器 UI
 *
 * ## 不适用场景
 * - 使用 MediaPlayer 或 IJKPlayer 引擎（不兼容）
 * - 需要完全自定义 UI 的场景
 * - 对体积有严格限制的应用
 *
 * ## 与其他渲染器的对比
 * | 特性 | SurfaceView | TextureView | VideoView | **PlayerView** |
 * |------|-------------|-------------|-----------|----------------|
 * | 性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
 * | 动画支持 | ❌ | ✅ | ✅ | ✅ |
 * | 内置控制器 | ❌ | ❌ | ❌ | ✅ |
 * | 字幕支持 | ❌ | ❌ | ❌ | ✅ |
 * | 引擎兼容性 | 所有引擎 | 所有引擎 | 仅 MP | **仅 ExoPlayer** |
 * | 推荐度 | ★★★★★ | ★★★★☆ | ★★★☆☆ | ★★★★★ (配合Exo) |
 *
 * ## 使用示例
 * ```kotlin
 * // 必须配合 Media3PlayerEngine 使用！
 * val engine = Media3PlayerEngine()
 * val render = ExoPlayerViewRender()
 *
 * render.attach(container)
 * engine.init()
 *
 * // 将 PlayerView 与 Engine 绑定
 * render.bindToEngine(engine)
 *
 * // 设置数据源并开始播放
 * engine.setSource(uri)
 * engine.prepareAsync()
 * ```
 *
 * @see IVideoRender 渲染器接口定义
 * @see Media3PlayerEngine 配合使用的引擎
 */
class ExoPlayerViewRender : IVideoRender {

    companion object {
        private const val TAG = "ExoPlayerViewRender"
    }

    // ==================== 属性 ====================

    override val renderType: RenderType = RenderType.EXO_PLAYER_VIEW

    /** PlayerView 实例 */
    private var playerView: PlayerView? = null

    /** 当前 Surface 实例 */
    private var currentSurface: Surface? = null

    /** Surface 是否已就绪 */
    private var _isSurfaceReady: Boolean = false

    /** Surface 就绪回调 */
    private var onSurfaceReadyCallback: ((Surface) -> Unit)? = null

    /** Surface 销毁回调 */
    private var onSurfaceDestroyedCallback: (() -> Unit)? = null

    /** 父容器引用（用于布局调整）*/
    private var parentContainer: ViewGroup? = null

    /** 是否使用 SurfaceView（默认 true）*/
    private var useSurfaceView: Boolean = true

    // ==================== 基本信息 ====================

    override val renderView: View?
        get() = playerView

    // ==================== 配置选项 ====================

    /**
     * 设置是否使用 SurfaceView 作为底层渲染 View
     *
     * @param use true 使用 SurfaceView（性能更好），false 使用 TextureView（支持动画）
     */
    fun setUseSurfaceView(use: Boolean) {
        this.useSurfaceView = use
        playerView?.setShutterBackgroundColor(0) // 透明背景
    }

    /**
     * 是否显示 PlayerView 内置的控制器
     *
     * @param show true 显示（默认 false，因为我们有自己的控制面板）
     */
    fun setShowController(show: Boolean) {
        playerView?.useController = show
    }

    /**
     * 是否自动隐藏控制器
     *
     * @param autoHide true 自动隐藏
     */
    fun setControllerAutoHide(autoHide: Boolean) {
        playerView?.controllerAutoShow = autoHide
    }

    // ==================== 绑定与解绑 ====================

    /**
     * 将 PlayerView 绑定到容器并返回
     *
     * @param container 父容器
     * @return PlayerView 实例
     */
    override fun attach(container: ViewGroup, engine: IPlayerEngine): View {
        detach()

        parentContainer = container
        val context = container.context

        playerView = PlayerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 配置 PlayerView
            useController = false // 禁用内置控制器（我们有自己的控制面板）
            setControllerAutoShow(false)
            setShutterBackgroundColor(0) // 透明背景

            // 注意：Media3 新版本中 Surface 类型由内部自动管理
            // 不再需要手动设置 setSurfaceType
        }

        playerView?.player = (engine as Media3PlayerEngine).getExoPlayer()

        container.addView(playerView)

        log(TAG, "attached to container (useSurfaceView=$useSurfaceView)")
        return playerView!!
    }

    /**
     * 解绑 PlayerView（从父容器移除）
     */
    override fun detach() {
        playerView?.let { view ->
            // 先解除绑定 Player
            view.player = null
            parentContainer?.removeView(view)
        }
        playerView = null
        currentSurface = null
        _isSurfaceReady = false
        parentContainer = null
        log(TAG, "detached")
    }

    // ==================== Surface 管理 ====================

    /**
     * Surface 是否已就绪
     */
    override fun isSurfaceReady(): Boolean = playerView?.player != null

    /**
     * 获取当前 Surface 实例
     */
    override fun getSurface(): Surface? = currentSurface

    /**
     * 检查 Surface 是否有效
     */
    override fun isSurfaceValid(): Boolean {
        // PlayerView 管理 Surface 的有效性
        return  playerView?.player != null
    }

    // ==================== 回调设置 ====================

    /**
     * 设置 Surface 就绪回调
     */
    override fun setOnSurfaceReadyListener(listener: ((Surface) -> Unit)?) {
        onSurfaceReadyCallback = listener
    }

    /**
     * 设置 Surface 销毁回调
     */
    override fun setOnSurfaceDestroyedListener(listener: (() -> Unit)?) {
        onSurfaceDestroyedCallback = listener
    }

    // ==================== 布局管理 ====================

    /**
     * 调整 PlayerView 的布局尺寸
     *
     * PlayerView 会自动处理视频宽高比，
     * 此方法主要用于调整容器大小。
     */
    override fun adjustLayout(videoWidth: Int, videoHeight: Int, scaleMode: VideoScaleMode) {
        if (videoWidth <= 0 || videoHeight <= 0) return

        // PlayerView 会自动处理 ResizeMode
        // 这里可以根据需要调整 resizeMode
        ExoPlayerHelper.applyVideoScaleMode(playerView, scaleMode)
    }

    /**
     * 释放所有资源
     */
    override fun release() {
        detach()
        onSurfaceReadyCallback = null
        onSurfaceDestroyedCallback = null
        log(TAG, "released")
        unbindFromEngine()
    }

    /**
     * 解除与 Media3PlayerEngine 的绑定
     */
    fun unbindFromEngine() {
        try {
            playerView?.player = null
            log(TAG, "unbound from Media3PlayerEngine")
        } catch (_: Exception) {}
    }
}
