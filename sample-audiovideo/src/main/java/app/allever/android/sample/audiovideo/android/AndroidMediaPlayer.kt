package app.allever.android.sample.audiovideo.android

import android.view.SurfaceHolder
import android.widget.VideoView
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.MediaPlayerKernal
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.SurfaceType
import app.allever.android.sample.audiovideo.lib.VideoHelper

/**
 * Android MediaPlayer 视频播放封装
 *
 * 职责：
 * - 封装 MediaPlayer 完整生命周期（创建 → 准备 → 播放 → 暂停 → 停止 → 释放）
 * - 管理 MediaPlayer 状态与 [PlayerState] 的映射
 * - 支持四种 Surface 绑定模式：VideoView / SurfaceView / TextureView
 * - 处理 Surface 异步就绪的 PendingPrepare 机制
 * - 提供进度追踪、变速、音量、循环等能力
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * 设计原则：
 * - **逻辑与 UI 分离**：本类不创建 UI 组件，Surface 由外部传入
 * - **灵活绑定**：支持多种渲染方式，对外 API 统一
 * - **状态驱动**：所有操作基于状态机，确保线程安全
 *
 * 使用示例：
 * ```kotlin
 * // 示例 1：使用 VideoView
 * val player = AndroidMediaPlayer()
 * player.attach(videoView)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 * })
 * player.setSource("https://example.com/video.mp4")
 *
 * // 示例 2：使用 SurfaceView
 * val player = AndroidMediaPlayer()
 * player.attach(surfaceView)
 * player.setSource("/sdcard/video.mp4")
 * player.play()
 *
 * // 示例 3：使用 TextureView
 * val player = AndroidMediaPlayer()
 * player.attach(textureView)
 * player.setSource("https://example.com/video.mp4")
 * player.play()
 *
 * // 页面销毁时
 * player.release()
 * ```
 */
class AndroidMediaPlayer: BaseVideoPlayer() {
    //TAG

    // ==================== 内部组件 ====================

    /** MediaPlayer 实例 */
//    private var mediaPlayer: MediaPlayer? = null
    override var engine: IPlayerKernal<*> = MediaPlayerKernal().apply {
        registerListener(engineListener)
    }

    // ==================== Surface 绑定（三种模式）====================

    /** VideoView 绑定 */
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
