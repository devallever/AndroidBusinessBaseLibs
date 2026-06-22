package app.allever.android.sample.audiovideo.core.render

import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.engine.IPlayerEngine
import app.allever.android.sample.audiovideo.lib.VideoHelper
import app.allever.android.sample.audiovideo.lib.VideoScaleMode

/**
 * VideoView 渲染器实现
 *
 * ## 职责
 * - 管理 VideoView 的创建和生命周期
 * - 处理 VideoView 内部 Surface 的创建/销毁回调
 * - 提供 Surface 给引擎绑定（注意：VideoView 内部封装了 SurfaceView）
 * - 支持基本的布局自适应
 *
 * ## VideoView 特点（与 SurfaceView/TextureView 的区别）
 * **优势：**
 * ✅ **最简单易用**：一行代码即可播放视频，无需手动管理 Surface
 * ✅ **自动处理 Surface**：内部封装了 SurfaceView 和 MediaPlayer，开箱即用
 * ✅ **兼容性好**：所有 Android 设备都支持
 * ✅ **代码量少**：适合快速原型开发或简单场景
 *
 * **劣势：**
 * ❌ **定制性差**：无法自定义渲染逻辑、无法获取底层 Surface 做特效
 * ❌ **性能一般**：比直接使用 SurfaceView 略差（多了一层封装）
 * ❌ **不支持高级功能**：
 *    - 不支持自定义缩放模式（CROP_CENTER、STRETCH 等）
 *    - 不支持矩阵变换（旋转、镜像等）
 * ❌ **控制粒度粗**：无法精细控制缓冲策略、解码器参数等
 *
 * ## 适用场景
 * - 快速原型开发 / MVP 验证
 * - 简单的视频播放需求（如启动页广告、帮助视频）
 * - 不需要自定义 UI 或特效的场景
 * - 对性能要求不高的应用
 *
 * ## 不适用场景
 * - 需要自定义播放器 UI（进度条、控制按钮等）
 * - 需要做视频特效（圆角、模糊背景、画中画等）
 * - 需要支持多种缩放模式切换
 * - 需要与 ExoPlayer/IjkPlayer 等高级引擎集成
 *
 * ## 使用示例
 * ```kotlin
 * val render = VideoViewRender()
 * render.attach(container)
 * render.setOnSurfaceReadyListener { surface ->
 *     engine.setSurface(surface)
 * }
 * ```
 */
class VideoViewRender : IVideoRender {

    companion object {
        private const val TAG = "VideoViewRender"
    }

    // ==================== 属性 ====================

    override val renderType: RenderType = RenderType.VIDEO_VIEW

    /** VideoView 实例 */
    private var videoView: VideoView? = null

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
        get() = videoView

    // ==================== 绑定与解绑 ====================

    /**
     * 将 VideoView 绑定到容器并返回
     *
     * @param container 父容器
     * @return VideoView 实例
     */
    override fun attach(container: ViewGroup, engine: IPlayerEngine): View {
        detach()

        parentContainer = container
        val context = container.context
        
        videoView = VideoView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(videoView)
        
        // 设置 SurfaceHolder 回调
        setupSurfaceHolderCallback()
        
        log(TAG, "attached to container")
        return videoView!!
    }

    /**
     * 解绑 VideoView（从父容器移除）
     */
    override fun detach() {
        videoView?.let { view ->
            parentContainer?.removeView(view)
            view.holder.removeCallback(surfaceHolderCallback)
        }
        videoView = null
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
        return videoView?.holder?.surface?.isValid == true
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
     * 调整 VideoView 的布局尺寸
     *
     * 注意：VideoView 对缩放模式的支持有限，
     * 主要依赖其内部的 SurfaceView 实现。
     */
    override fun adjustLayout(videoWidth: Int, videoHeight: Int, scaleMode: VideoScaleMode) {
        if (videoWidth <= 0 || videoHeight <= 0) return
        // VideoView 的布局调整能力有限
        // 这里只做基本的大小调整
        VideoHelper.adjustRenderViewLayout(videoView, videoWidth, videoHeight, scaleMode)
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
    private fun setupSurfaceHolderCallback() {
        videoView?.holder?.addCallback(surfaceHolderCallback)

        // 检查 Surface 是否已经可用（复用场景）
        if (videoView?.holder?.surface?.isValid == true) {
            handleSurfaceCreated(videoView!!.holder.surface)
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
            // 尺寸变化时可以重新计算布局
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
