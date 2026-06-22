package app.allever.android.sample.audiovideo.core.render

import android.content.Context
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.engine.IPlayerEngine
import app.allever.android.sample.audiovideo.lib.VideoHelper
import app.allever.android.sample.audiovideo.lib.VideoScaleMode

/**
 * SurfaceView 渲染器实现
 *
 * ## 职责
 * - 管理 SurfaceView 的创建和生命周期
 * - 处理 Surface 的创建/销毁/变化回调
 * - 提供 Surface 给引擎绑定
 * - 支持布局自适应（根据视频尺寸和缩放模式）
 *
 * ## SurfaceView 特点（与 TextureView/VideoView 的区别）
 * **优势：**
 * ✅ **性能优秀**：独立窗口，硬件合成，功耗低
 * ✅ **控制精细**：可直接操作 Surface，支持自定义渲染逻辑
 * ✅ **兼容性好**：所有 Android 版本都支持
 * ✅ **适合大多数场景**：视频播放、直播、相机预览等
 *
 * **劣势：**
 * ❌ **不能做动画**：不支持旋转、缩放、透明度等变换
 * ❌ **不能嵌套**：会覆盖在其他 View 上层
 * ❌ **Surface 创建异步**：需要处理就绪等待机制
 *
 * ## 适用场景
 * - 大多数视频播放场景（推荐首选）
 * - 直播应用（低延迟要求）
 * - 需要自定义播放器 UI 的应用
 * - 对性能有较高要求的场景
 *
 * ## 使用示例
 * ```kotlin
 * val render = SurfaceViewRender()
 * render.attach(container)
 * render.setOnSurfaceReadyListener { surface ->
 *     engine.setSurface(surface)
 * }
 * ```
 */
class SurfaceViewRender : IVideoRender {

    companion object {
        private const val TAG = "SurfaceViewRender"
    }

    // ==================== 属性 ====================

    override val renderType: RenderType = RenderType.SURFACE_VIEW

    /** SurfaceView 实例 */
    private var surfaceView: SurfaceView? = null

    /** 当前 Surface 实例 */
    private var currentSurface: Surface? = null

    /** Surface 是否已就绪 */
    private var _isSurfaceReady: Boolean = false

    /** Surface 就绪回调 */
    private var onSurfaceReadyCallback: ((Surface) -> Unit)? = null

    /** Surface 销毁回调 */
    private var onSurfaceDestroyedCallback: (() -> Unit)? = null

    /** 父容器引用（用于布局调整） */
    private var parentContainer: ViewGroup? = null

    // ==================== 基本信息 ====================

    override val renderView: View?
        get() = surfaceView

    // ==================== 绑定与解绑 ====================

    /**
     * 将 SurfaceView 绑定到容器并返回
     *
     * @param container 父容器
     * @return SurfaceView 实例
     */
    override fun attach(container: ViewGroup, engine: IPlayerEngine): View {
        detach()

        parentContainer = container
        val context = container.context
        
        surfaceView = SurfaceView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(surfaceView)
        
        // 设置 Surface 回调
        setupSurfaceCallback()
        
        log(TAG, "attached to container")
        return surfaceView!!
    }

    /**
     * 解绑 SurfaceView（从父容器移除）
     */
    override fun detach() {
        surfaceView?.let { view ->
            parentContainer?.removeView(view)
            view.holder.removeCallback(surfaceHolderCallback)
        }
        surfaceView = null
        currentSurface = null
        _isSurfaceReady = false
        parentContainer = null
        log(TAG, "detached")
    }

    // ==================== Surface 管理 ====================

    /**
     * Surface 是否已就绪
     */
    override fun isSurfaceReady(): Boolean = _isSurfaceReady

    /**
     * 获取当前 Surface 实例
     */
    override fun getSurface(): Surface? = currentSurface

    /**
     * 检查 Surface 是否有效
     */
    override fun isSurfaceValid(): Boolean {
        return surfaceView?.holder?.surface?.isValid == true
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
     * 调整 SurfaceView 的布局尺寸
     *
     * 根据视频原始尺寸和缩放模式计算并设置布局参数。
     */
    override fun adjustLayout(videoWidth: Int, videoHeight: Int, scaleMode: VideoScaleMode) {
        if (videoWidth <= 0 || videoHeight <= 0) return
        VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, scaleMode)
    }

    /**
     * 释放所有资源
     */
    override fun release() {
        detach()
        onSurfaceReadyCallback = null
        onSurfaceDestroyedCallback = null
        log(TAG, "released")
    }

    // ==================== 内部方法 ====================

    /**
     * 设置 SurfaceHolder 回调
     */
    private fun setupSurfaceCallback() {
        surfaceView?.holder?.addCallback(surfaceHolderCallback)

        // 检查 Surface 是否已经可用（复用场景）
        if (surfaceView?.holder?.surface?.isValid == true) {
            handleSurfaceCreated(surfaceView!!.holder.surface)
        }
    }

    /** SurfaceHolder 回调实现 */
    private val surfaceHolderCallback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            log(TAG, "surfaceCreated")
            handleSurfaceCreated(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log(TAG, "surfaceChanged: ${width}x${height}")
            // Surface 尺寸变化时可以重新计算布局
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log(TAG, "surfaceDestroyed")
            currentSurface = null
            _isSurfaceReady = false
            onSurfaceDestroyedCallback?.invoke()
        }
    }

    /**
     * 处理 Surface 创建完成
     */
    private fun handleSurfaceCreated(surface: Surface) {
        currentSurface = surface
        _isSurfaceReady = true
        log(TAG, "surface ready")
        onSurfaceReadyCallback?.invoke(surface)
    }
}
