package app.allever.android.sample.audiovideo.android

import android.annotation.SuppressLint
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.Media3PlayerKernal
import app.allever.android.sample.audiovideo.lib.ExoPlayerHelper
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.SurfaceType
/**
 * Android Media3 (ExoPlayer) 视频播放封装
 *
 * 职责：
 * - 封装 ExoPlayer 完整生命周期（创建 → 准备 → 播放 → 暂停 → 停止 → 释放）
 * - 管理 ExoPlayer 状态与 [PlayerState] 的映射
 * - 支持三种 Surface 绑定模式：PlayerView（推荐）/ SurfaceView / TextureView
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
 * // 示例 1：使用 PlayerView（推荐）
 * val player = AndroidMedia3Player()
 * player.attach(playerView)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 * })
 * player.setSource("https://example.com/video.mp4")
 *
 * // 示例 2：使用 SurfaceView
 * val player = AndroidMedia3Player()
 * player.attach(surfaceView)
 * player.setSource("/sdcard/video.mp4")
 * player.play()
 *
 * // 示例 3：使用 TextureView
 * val player = AndroidMedia3Player()
 * player.attach(textureView)
 * player.setSource("https://example.com/video.mp4")
 * player.play()
 *
 * // 页面销毁时
 * player.release()
 * ```
 */
class AndroidMedia3Player: BaseVideoPlayer() {
    
    override var engine: IPlayerKernal<*> = Media3PlayerKernal().apply {
        registerListener(engineListener)
    }
    // ==================== Surface 绑定（三种模式）====================

    /** PlayerView 绑定（推荐方式）*/
    private var playerView: PlayerView? = null

    /**
     * 绑定 PlayerView（推荐方式）
     *
     * PlayerView 是 ExoPlayer 官方推荐的视图组件，
     * 自动管理 Surface 生命周期，无需手动处理。
     *
     * @param playerView 外部创建的 PlayerView 实例
     */
    fun attach(playerView: PlayerView) {
        detach()

        this.playerView = playerView
        this.currentSurfaceType = SurfaceType.PLAYER_VIEW
        this.isSurfaceReady = true  // PlayerView 的 Surface 立即可用

        log(TAG, "attach PlayerView")

        initPlayer()

        //将 ExoPlayer 绑定到 PlayerView
        playerView.player =  engine.getEnginePlayer() as? Player?
        log(TAG, "bound to PlayerView")

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
        }
    }
    /**
     * 解绑当前 Surface（页面 onPause/onDestroyView 时调用）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     * 不释放内部 ExoPlayer 和其他资源。
     */
    override fun detach() {
        when (currentSurfaceType) {
            SurfaceType.PLAYER_VIEW -> {
                playerView?.player = null
                playerView = null
                log(TAG, "detach PlayerView")
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
     * - detach() 会触发 ExoPlayer 异步释放 MediaCodec
     * - attach() 立即执行时，MediaCodec 可能还在 Released 状态
     * - 此时调用 setSurface() 会抛出异常
     *
     * 解决方案（方案 A：暂停 → 切换 → 恢复）：
     * 1. 暂停播放 → 让 MediaCodec 进入安全状态（Flushed）
     * 2. 延迟等待 → 确保 PAUSED 状态稳定生效
     * 3. 执行切换 → detach + attach（此时安全）
     * 4. 恢复播放 → 从保存的位置继续播放
     *
     * 使用示例：
     * ```kotlin
     * // 切换到 SurfaceView
     * player.safeSwitchToSurfaceView(surfaceView)
     *
     * // 切换到 TextureView
     * player.safeSwitchToTextureView(textureView)
     *
     * // 切换到 PlayerView
     * player.safeSwitchToPlayerView(playerView)
     * ```
     */

    /**
     * 安全切换到 PlayerView
     *
     * @param playerView 目标 PlayerView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaCodec 状态稳定
     */
    fun safeSwitchToPlayerView(playerView: PlayerView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(playerView) },
            targetName = "PlayerView",
            delayMs = delayMs
        )
    }

    // ==================== 内部：SurfaceView/TextureView 画面自适应 ====================

    /**
     * 根据当前缩放模式调整 SurfaceView 或 TextureView 的布局尺寸
     *
     * 调用时机：
     * - onVideoSizeChanged 回调中（获取到视频尺寸后）
     * - videoScaleMode 属性改变时（切换缩放模式）
     *
     * 仅对 SurfaceView 和 TextureView 生效，PlayerView 有自己的缩放控制。
     */
    override fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.PLAYER_VIEW -> ExoPlayerHelper.applyVideoScaleMode(playerView, videoScaleMode)
            else -> { super.adjustSurfaceLayout() }
        }
    }
}
