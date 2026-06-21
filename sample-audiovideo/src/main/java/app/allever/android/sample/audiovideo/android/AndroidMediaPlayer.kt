package app.allever.android.sample.audiovideo.android

import android.view.SurfaceHolder
import android.widget.VideoView
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.MediaPlayerKernal
import app.allever.android.sample.audiovideo.lib.SurfaceType
import app.allever.android.sample.audiovideo.lib.VideoHelper

/**
 * VideoView 视频播放器实现（继承自 [BaseVideoPlayer]）
 *
 * ## 职责
 * - 封装 VideoView + MediaPlayer 的完整生命周期管理
 * - 处理 VideoView 的 SurfaceHolder 生命周期（创建/销毁/变化）
 * - 实现 [BaseVideoPlayer] 的抽象方法以适配 VideoView 的特殊行为
 *
 * ## VideoView 特点（与 SurfaceView/TextureView 的区别）
 * **优势：**
 * ✅ **最简单易用**：一行代码即可播放视频，无需手动管理 Surface
 * ✅ **自动处理 Surface**：内部封装了 SurfaceView 和 MediaPlayer，开箱即用
 * ✅ **兼容性好**：所有 Android 设备都支持，无需担心适配问题
 * ✅ **代码量少**：本类只有 ~192 行（基类已处理大部分逻辑）
 *
 * **劣势：**
 * ❌ **定制性差**：无法自定义渲染逻辑、无法获取底层 Surface 做特效
 * ❌ **性能一般**：比直接使用 SurfaceView 略差（多了一层封装）
 * ❌ **不支持高级功能**：
 *    - 不支持自定义缩放模式（CROP_CENTER、STRETCH 等）
 *    - 不支持矩阵变换（旋转、镜像等）
 *    - 无法与其他 View 混合显示
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
 * - 对性能有极致要求的场景（直播、4K 视频等）
 *
 * ## 使用示例
 * ```kotlin
 * // 最简单的使用方式
 * val player = AndroidMediaPlayer()
 * player.attach(videoView)  // 绑定 VideoView
 * player.setSource("https://example.com/video.mp4")
 * player.listener = object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 *     override fun onComplete() { log("播放完成") }
 *     override fun onError(code: Int, msg: String) { log("错误: $msg") }
 * }
 * // onPrepared 回调后会自动播放
 *
 * // 页面生命周期管理
 * override fun onPause() {
 *     if (player.isPlaying) player.pause()
 * }
 *
 * override fun onDestroy() {
 *     player.release()
 * }
 * ```
 *
 * ## 架构说明
 * 本类采用**模板方法模式**，继承 [BaseVideoPlayer] 基类：
 * - **基类负责**：状态管理、数据源设置、播放控制、进度追踪、错误处理等通用逻辑
 * - **本类负责**：VideoView 特有的绑定/解绑逻辑、布局调整等差异化实现
 *
 * 这种设计使得：
 * - 代码复用率高（~85% 的逻辑在基类）
 * - 易于维护（修改通用逻辑只需改基类）
 * - 易于扩展（新增渲染方式只需写少量子类代码）
 *
 * @see BaseVideoPlayer 基类，包含完整的播放流程实现
 * @see AndroidSurfacePlayer SurfaceView 实现（推荐大多数场景）
 * @see AndroidTexturePlayer TextureView 实现（需要动画/变换时）
 */
class AndroidMediaPlayer: BaseVideoPlayer() {

    /**
     * MediaPlayer 引擎实例（使用 MediaPlayerKernal 封装）
     *
     * 通过依赖注入的方式在初始化时创建，
     * 并注册 [engineListener] 以接收引擎事件回调。
     *
     * 为什么不直接使用 MediaPlayer？
     * - MediaPlayerKernal 封装了 MediaPlayer 的复杂性
     * - 提供统一的接口（IPlayerKernal），方便替换为其他引擎
     * - 内置了线程安全、异常处理等机制
     */
//    private var mediaPlayer: MediaPlayer? = null
    override var engine: IPlayerKernal<*> = MediaPlayerKernal().apply {
        registerListener(engineListener)
    }

    // ==================== 内部组件 ====================

    /** VideoView 实例（外部传入，本类不创建）*/
    private var videoView: VideoView? = null
    /**
     * 绑定 VideoView
     *
     * VideoView 是 Android 原生的视频视图组件，
     * 内部封装了 SurfaceView 和 MediaPlayer。
     * 此方法会获取 VideoView 内部的 SurfaceHolder 并绑定到我们的 MediaPlayer。
     *
     * @param videoView 外部创建的 VideoView 实例
     */
    fun attach(videoView: VideoView) {
        detach()

        this.videoView = videoView
        this.currentSurfaceType = SurfaceType.VIDEO_VIEW
        this.isSurfaceReady = false  // 需要等待 SurfaceHolder 就绪

        log(TAG,"attach VideoView (waiting for surface)")

        initPlayer()

        // 设置 SurfaceHolder 回调以监听 Surface 就绪
        videoView.holder?.addCallback(videoViewSurfaceCallback)

        // 检查 Surface 是否已经可用
        if (videoView.holder?.surface?.isValid == true) {
            onSurfaceReady(videoView.holder!!.surface)
        }
    }

    /**
     * 解绑当前 Surface（页面 onPause/onDestroyView 时调用）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     * 不释放内部 MediaPlayer 和其他资源。
     */
    override fun detach() {
        when (currentSurfaceType) {
            SurfaceType.VIDEO_VIEW -> {
                videoView?.holder?.removeCallback(videoViewSurfaceCallback)
                videoView = null
                isSurfaceReady = false
                log(TAG,"detach VideoView")
                currentSurfaceType = SurfaceType.NONE
            }
            else -> { super.detach() }
        }

    }

    // ==================== 安全的 Surface 切换 API ====================

    /**
     * 安全地切换 Surface（推荐使用此方法）
     *
     * **解决 MediaCodec 状态机竞态条件问题：**
     *
     * 问题背景：
     * 在播放过程中直接调用 detach() + attach() 会导致：
     * ```
     * IllegalStateException: setSurface() is valid only at Executing states;
     * currently at Released state
     * ```
     *
     * 原因：
     * - detach() 会触发 MediaPlayer 异步释放 MediaCodec
     * - attach() 立即执行时，MediaCodec 可能还在 Released 状态
     * - 此时调用 setDisplay() 或 setSurface() 会抛出异常
     *
     * 解决方案（方案 B：stop → 切换 → reprepare）：
     * 1. stop() 完全停止 MediaPlayer（清空所有缓冲区和渲染队列）
     * 2. detach + attach 安全切换 Surface
     * 3. 使用保存的数据源重新 prepare
     * 4. 在 onPrepared 回调中恢复播放位置并继续播放
     */

    /**
     * 安全切换到 VideoView
     *
     * @param videoView 目标 VideoView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaPlayer 状态稳定
     */
    fun safeSwitchToVideoView(videoView: VideoView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(videoView) },
            targetName = "VideoView",
            delayMs = delayMs
        )
    }
    // ==================== 回调定义 ====================

    /** VideoView 的 SurfaceHolder 回调 */
    private val videoViewSurfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            log(TAG,"VideoView surfaceCreated")
            onSurfaceReady(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log(TAG,"VideoView surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log(TAG,"VideoView surfaceDestroyed")
            isSurfaceReady = false
        }
    }

    // ==================== 内部：SurfaceView/TextureView/VideoView 画面自适应 ====================

    /**
     * 根据当前缩放模式调整布局尺寸
     *
     * 调用时机：
     * - onVideoSizeChanged 回调中（获取到视频尺寸后）
     * - videoScaleMode 属性改变时（切换缩放模式）
     */
    override fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.VIDEO_VIEW -> VideoHelper.adjustRenderViewLayout(videoView, videoWidth, videoHeight, videoScaleMode)
            else -> { super.adjustSurfaceLayout() }
        }
    }
}
