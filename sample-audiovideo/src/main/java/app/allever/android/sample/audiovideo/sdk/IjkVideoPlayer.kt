package app.allever.android.sample.audiovideo.sdk

import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.BaseVideoPlayer
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.IjkPlayerKernal
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerState

/**
 * IJKPlayer 视频播放封装（SDK 层）
 *
 * 职责：
 * - 封装 IjkMediaPlayer 完整生命周期（创建 → 准备 → 播放 → 暂停 → 停止 → 释放）
 * - 管理 IjkMediaPlayer 状态与 [PlayerState] 的映射
 * - 支持两种 Surface 绑定模式：SurfaceView（推荐）/ TextureView（高级）
 * - 处理 Surface 异步就绪的 PendingPrepare 机制
 * - 提供进度追踪、变速、音量、循环等能力
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * 设计原则：
 * - **逻辑与 UI 分离**：本类不创建 UI 组件，Surface 由外部传入
 * - **灵活绑定**：支持多种渲染方式，对外 API 统一
 * - **状态驱动**：所有操作基于状态机，确保线程安全
 * - **与 ExoVideoPlayer 对齐**：相同的 API 接口、监听器、状态管理，便于切换引擎
 *
 * 与 ExoVideoPlayer 的区别：
 * - 仅支持 SurfaceView 和 TextureView（IJKPlayer 没有 PlayerView）
 * - 使用 prepareAsync() 异步准备（ExoPlayer 用同步 prepare()）
 * - 支持 TCP 速度监控、缓冲百分比、Native 日志等 IJKPlayer 特有功能
 * - 可通过 setOptions() 配置硬解码、缓冲策略等高级参数
 *
 * 使用示例：
 * ```kotlin
 * // 示例 1：使用 SurfaceView（推荐）
 * val player = IjkVideoPlayer()
 * player.attach(surfaceView)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 * })
 * player.setSource("https://example.com/video.mp4")
 *
 * // 示例 2：使用 TextureView
 * val player = IjkVideoPlayer()
 * player.attach(textureView)
 * player.setSource("/sdcard/video.mp4")
 * player.play()
 *
 * // 页面销毁时
 * player.release()
 * ```
 */
class IjkVideoPlayer: BaseVideoPlayer() {
    /** IjkMediaPlayer 实例 */
//    private var ijkMediaPlayer: IjkMediaPlayer? = null
    override var engine: IPlayerKernal<*> = IjkPlayerKernal().apply {
        registerListener(engineListener)
    }
    /**
     * 解绑当前 Surface 并清理资源
     *
     * 注意：此方法不会释放 IjkMediaPlayer，
     * 仅解绑 Surface 以便后续重新绑定或切换 Surface 类型。
     */
    override fun detach() {
       super.detach()
        try {
            engine.setSurface(null)
        } catch (e: Exception) {
           log(TAG, "detach error: ${e.message}")
        }
    }

    // ==================== 公共查询接口 ====================

    /**
     * 获取 TCP 下载速度（字节/秒）
     *
     * IJKPlayer 特有功能，可用于显示下载速度。
     *
     * @return TCP 下载速度（bps），不可用时返回 0
     */
    val tcpSpeed: Long
        get() = try { engine.getTcpSpeed() } catch (_: Exception) { 0L }

}
