package app.allever.android.lib.player.core.render

import android.view.Surface
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.player.core.VideoScaleMode
import app.allever.android.lib.player.core.engine.IPlayerEngine

/**
 * 视频渲染器接口（纯渲染逻辑，不涉及播放）
 *
 * ## 职责
 * - 管理 Surface/View 的创建和生命周期
 * - 处理 Surface 的就绪/销毁回调
 * - 提供布局自适应能力
 * - 暴露 Surface 供引擎绑定
 *
 * ## 设计原则
 * **单一职责**：本接口只关注视频如何渲染到屏幕上，不关心播放逻辑。
 * 播放控制由 [IPlayerEngine] 负责。
 *
 * ## 实现类
 * - [SurfaceViewRender]：基于 SurfaceView 的渲染实现（性能好，推荐）
 * - [TextureViewRender]：基于 TextureView 的渲染实现（支持动画和变换）
 * - [VideoViewRender]：基于 VideoView 的渲染实现（最简单）
 *
 * ## 使用方式
 * 渲染器通常通过 [app.allever.android.sample.audiovideo.core.player.VideoPlayer] 协调器使用：
 * ```kotlin
 * val player = VideoPlayer(
 *     engine = MediaPlayerEngine(),
 *     render = SurfaceViewRender()
 * )
 * ```
 */
interface IVideoRender {

    val TAG: String
        get() = this::class.java.simpleName

    // ==================== 基本信息 ====================

    /**
     * 渲染器名称（用于识别和日志）
     *
     * 示例: "SurfaceView", "TextureView", "PlayerView"
     */
    val renderName: String

    /**
     * 获取渲染 View 实例
     *
     * 此 View 需要添加到容器中显示。
     */
    val renderView: View?

    // ==================== 绑定与解绑 ====================

    /**
     * 将渲染 View 绑定到容器并返回
     *
     * @param container 父容器（ViewGroup）
     * @return 渲染 View 实例
     */
    fun attach(container: ViewGroup, engine: IPlayerEngine): View

    /**
     * 解绑渲染 View（从父容器移除）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     */
    fun detach()

    // ==================== Surface 管理 ====================

    /**
     * Surface 是否已就绪（可用于渲染）
     *
     * Surface 创建是异步过程，需要等待此方法返回 true 后才能使用。
     */
    fun isSurfaceReady(): Boolean

    /**
     * 获取当前 Surface 实例
     *
     * 如果 Surface 未就绪则返回 null。
     */
    fun getSurface(): Surface?

    /**
     * 检查 Surface 是否有效
     */
    fun isSurfaceValid(): Boolean

    // ==================== 回调设置 ====================

    /**
     * 设置 Surface 就绪回调
     *
     * 当 Surface 创建完成并可用于渲染时触发。
     */
    fun setOnSurfaceReadyListener(listener: ((Surface) -> Unit)?)

    /**
     * 设置 Surface 销毁回调
     *
     * 当 Surface 即将销毁时触发（此时应解绑引擎的 Surface）。
     */
    fun setOnSurfaceDestroyedListener(listener: (() -> Unit)?)

    // ==================== 布局管理 ====================

    /**
     * 调整渲染视图的布局尺寸
     *
     * 根据视频原始尺寸和缩放模式计算并设置 View 的布局参数。
     *
     * @param videoWidth 视频原始宽度（像素）
     * @param videoHeight 视频原始高度（像素）
     * @param scaleMode 缩放模式
     */
    fun adjustLayout(videoWidth: Int, videoHeight: Int, scaleMode: VideoScaleMode)

    /**
     * 释放所有资源
     */
    fun release()

    fun needSetSurface(): Boolean = true
}
