package app.allever.android.sample.audiovideo.core.render

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.engine.IPlayerEngine
import app.allever.android.sample.audiovideo.lib.VideoHelper
import app.allever.android.sample.audiovideo.lib.VideoScaleMode

/**
 * TextureView 渲染器实现
 *
 * ## 职责
 * - 管理 TextureView 的创建和生命周期
 * - 处理 SurfaceTexture 的可用/尺寸变化/销毁回调
 * - 提供 Surface 给引擎绑定
 * - 支持布局自适应（根据视频尺寸和缩放模式）
 *
 * ## TextureView 特点（与 SurfaceView/VideoView 的区别）
 * **优势：**
 * ✅ **支持动画**：可进行旋转、缩放、透明度等变换
 * ✅ **支持嵌套**：可作为普通 View 嵌套在其他 View 中
 * ✅ **Surface 就绪快**：通常比 SurfaceView 更快可用
 * ✅ **变换灵活**：可通过 Matrix 进行各种图像变换
 *
 * **劣势：**
 * ❌ **性能较差**：在普通 View 层渲染，无法利用硬件合成
 * ❌ **功耗较高**：需要 CPU/GPU 参与合成
 * ❌ **内存占用大**：相比 SurfaceView 多占用一些内存
 * ❌ **帧率受限**：某些设备上最高 60fps
 *
 * ## 适用场景
 * - 需要视频动画效果（圆角、模糊、旋转等）
 * - 需要与其他 View 混合显示（如列表中的视频）
 * - 需要自定义 UI 效果（画中画、悬浮窗等）
 * - 短视频应用（需要丰富的 UI 交互）
 *
 * ## 不适用场景
 * - 对性能要求极高的场景（4K 视频、直播等）
 * - 长时间播放的场景（功耗敏感）
 * - 不需要特殊 UI 效果的普通播放（推荐 SurfaceView）
 *
 * ## 使用示例
 * ```kotlin
 * val render = TextureViewRender()
 * render.attach(container)
 * render.setOnSurfaceReadyListener { surface ->
 *     engine.setSurface(surface)
 * }
 * ```
 */
class TextureViewRender : IVideoRender {

    companion object {
        private const val TAG = "TextureViewRender"
    }

    // ==================== 属性 ====================

    override val renderType: RenderType = RenderType.TEXTURE_VIEW

    /** TextureView 实例 */
    private var textureView: TextureView? = null

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
        get() = textureView

    // ==================== 绑定与解绑 ====================

    /**
     * 将 TextureView 绑定到容器并返回
     *
     * @param container 父容器
     * @return TextureView 实例
     */
    override fun attach(container: ViewGroup, engine: IPlayerEngine): View {
        detach()

        parentContainer = container
        val context = container.context
        
        textureView = TextureView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(textureView)
        
        // 设置 SurfaceTexture 回调
        setupSurfaceTextureListener()
        
        log(TAG, "attached to container")
        return textureView!!
    }

    /**
     * 解绑 TextureView（从父容器移除）
     */
    override fun detach() {
        textureView?.let { view ->
            parentContainer?.removeView(view)
            view.surfaceTextureListener = null
        }
        textureView = null
        currentSurface?.release()
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
        return textureView?.let { tv ->
            tv.isAvailable && tv.surfaceTexture != null
        } == true
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
     * 调整 TextureView 的布局尺寸
     *
     * 根据视频原始尺寸和缩放模式计算并设置布局参数。
     */
    override fun adjustLayout(videoWidth: Int, videoHeight: Int, scaleMode: VideoScaleMode) {
        if (videoWidth <= 0 || videoHeight <= 0) return
        VideoHelper.adjustRenderViewLayout(textureView, videoWidth, videoHeight, scaleMode)
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
     * 设置 SurfaceTexture 监听器
     */
    private fun setupSurfaceTextureListener() {
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                log(TAG, "onSurfaceTextureAvailable: ${width}x${height}")
                handleSurfaceAvailable(surfaceTexture)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                log(TAG, "onSurfaceTextureSizeChanged: ${width}x${height}")
                // 尺寸变化时可以重新计算布局
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log(TAG, "onSurfaceTextureDestroyed")
                currentSurface?.release()
                currentSurface = null
                _isSurfaceReady = false
                onSurfaceDestroyedCallback?.invoke()
                return true // 返回 true 表示已处理
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // 每帧更新时调用，通常不需要处理
            }
        }

        // 检查 SurfaceTexture 是否已经可用（复用场景）
        if (textureView?.isAvailable == true && textureView?.surfaceTexture != null) {
            handleSurfaceAvailable(textureView!!.surfaceTexture!!)
        }
    }

    /**
     * 处理 SurfaceTexture 可用
     */
    private fun handleSurfaceAvailable(surfaceTexture: SurfaceTexture) {
        currentSurface = Surface(surfaceTexture)
        _isSurfaceReady = true
        log(TAG, "surface ready")
        onSurfaceReadyCallback?.invoke(currentSurface!!)
    }
}
