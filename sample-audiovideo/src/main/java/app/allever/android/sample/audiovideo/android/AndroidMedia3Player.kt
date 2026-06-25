package app.allever.android.sample.audiovideo.android

import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.player.core.SurfaceType
import app.allever.android.lib.player.core.engine.media3.ExoPlayerHelper
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.Media3PlayerKernal

/**
 * Media3 (ExoPlayer) 视频播放器实现（继承自 [BaseVideoPlayer]）
 *
 * ## 职责
 * - 封装 ExoPlayer/Media3 的完整生命周期管理
 * - 处理 PlayerView 的绑定与解绑（官方推荐组件）
 * - 实现 [BaseVideoPlayer] 的抽象方法以适配 PlayerView 的特殊行为
 * - 提供 ExoPlayer 特有的高级功能（自适应码率、DASH/HLS 支持等）
 *
 * ## Media3 (ExoPlayer) 特点（与 MediaPlayer/IJK 的区别）
 * **优势：**
 * ✅ **功能强大**：支持 DASH、HLS、SmoothStreaming 等流媒体协议
 * ✅ **性能优秀**：针对 Android 优化，低延迟、高效率
 * ✅ **可定制性极强**：可自定义渲染器、解码器、加载器等每个组件
 * ✅ **官方维护**：Google 官方推荐，持续更新，文档完善
 * ✅ **适配性好**：自动处理各种设备和 Android 版本的兼容性问题
 * ✅ **高级特性**：
 *    - 自适应码率（ABR）：根据网络状况自动切换清晰度
 *    - 缓冲策略优化：智能缓冲，减少卡顿
 *    - 后台播放：Service 集成简单
 *    - 画中画：原生支持 Picture-in-Picture 模式
 *    - 字幕支持：WebVTT、TTML 等多种字幕格式
 *
 * **劣势：**
 * ❌ **依赖体积大**：库体积约 5-10MB（相比 MediaPlayer 的 0MB）
 * ❌ **学习成本高**：API 复杂，概念多（Renderer、TrackSelector、LoadControl 等）
 * ❌ **初始化较慢**：首次创建需要初始化解码器池等资源
 * ❌ **内存占用较高**：相比 MediaPlayer 多占用几 MB 内存
 *
 * ## PlayerView vs SurfaceView/TextureView
 * **PlayerView 是什么？**
 * - ExoPlayer 官方提供的视频视图组件（类似 VideoView）
 * - 内部封装了 SurfaceView + SubtitleView + 控制层
 * - 自动管理 Surface 生命周期，无需手动处理
 * - 内置播放控制 UI（可隐藏或自定义）
 *
 * **为什么推荐使用 PlayerView？**
 * 1. **开箱即用**：无需手动管理 Surface，代码量少
 * 2. **官方维护**：与 ExoPlayer 配合最佳，兼容性有保障
 * 3. **功能丰富**：内置字幕、控制按钮、缓冲动画等
 * 4. **易于扩展**：可通过 PlayerControlViewLayoutManager 自定义控制层
 *
 * **何时使用 SurfaceView/TextureView 替代 PlayerView？**
 * - 需要完全自定义 UI 时（不想用内置的控制层）
 * - 需要与现有 View 体系深度集成时
 * - 对包体积有极致要求时（PlayerView 依赖额外 UI 组件）
 *
 * ## 适用场景
 * - **在线视频应用**（YouTube、Netflix 风格）- DASH/HLS 流媒体
 * - **需要高级功能的播放器**（自适应码率、多音轨切换等）
 * - **企业级/商业项目**（稳定性要求高，需长期维护）
 * - **直播应用**（低延迟、高并发场景）
 * - **需要 DRM 保护的内容**（Widevine、PlayReady 等）
 *
 * ## 不适用场景
 * - 简单的本地视频播放（MediaPlayer 足够，更轻量）
 * - 对 APK 体积有严格限制的应用
 * - 快速原型开发 / MVP 验证（建议先用 VideoView）
 * - 只需基本播放功能的应用（不需要 DASH/HLS 等高级特性）
 *
 * ## 架构说明
 * 本类采用**模板方法模式**，继承 [BaseVideoPlayer] 基类：
 * - **基类负责**：状态管理、数据源设置、播放控制、进度追踪、错误处理等通用逻辑
 * - **本类负责**：
 *   - PlayerView 的绑定/解绑逻辑
 *   - ExoPlayer 特有的缩放模式调整（通过 ExoPlayerHelper）
 *   - 安全的 Surface 切换方案（使用"暂停→切换→恢复"策略）
 *
 * **引擎实现**：使用 [Media3PlayerKernal] 封装 ExoPlayer API
 * - 提供统一的 IPlayerKernal 接口
 * - 内置线程安全、异常处理等机制
 * - 支持 ExoPlayer 特有的配置项（如 TrackSelection、LoadControl 等）
 *
 * ## 使用示例
 * ```kotlin
 * // 基本用法
 * val player = AndroidMedia3Player()
 * player.attach(playerView)  // 绑定 PlayerView
 * player.setSource("https://example.com/video.mpd")  // DASH 流
 * player.listener = object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 *     override fun onComplete() { log("播放完成") }
 *     override fun onError(code: Int, msg: String) { log("错误: $msg") }
 *     override fun onBufferingUpdate(percent: Int) {
 *         // 显示缓冲进度
 *         progressBar.progress = percent
 *     }
 * }
 *
 * // 高级用法：自定义 ExoPlayer 配置
 * val player = AndroidMedia3Player().apply {
 *     loopMode = LoopMode.ALL  // 列表循环
 *     videoScaleMode = VideoScaleMode.CROP_CENTER  // 裁剪模式
 *     speed = 1.5f  // 1.5 倍速
 * }
 *
 * // 切换 Surface（安全方式）
 * player.safeSwitchToPlayerView(newPlayerView)
 *
 * // 页面生命周期管理
 * override fun onPause() {
 *     if (player.isPlaying) player.pause()
 *     player.detach()  // 解绑 PlayerView
 * }
 *
 * override fun onResume() {
 *     player.attach(playerView)  // 重新绑定
 * }
 *
 * override fun onDestroy() {
 *     player.release()  // 释放 ExoPlayer
 * }
 * ```
 *
 * ## 与其他实现的对比
 * | 特性 | AndroidMediaPlayer | AndroidSurfacePlayer | AndroidTexturePlayer | **AndroidMedia3Player** |
 * |------|-------------------|--------------------|---------------------|------------------------|
 * | **引擎** | MediaPlayer | MediaPlayer | MediaPlayer | **ExoPlayer (Media3)** |
 * | **渲染** | VideoView | SurfaceView | TextureView | **PlayerView** |
 * | **代码量** | ~192 行 | ~583 行 | ~586 行 | **~138 行** |
 * | **复杂度** | ⭐ | ⭐⭐⭐ | ⭐⭐⭐ | **⭐⭐** |
 * | **功能** | 基础 | 中级 | 中级 | **高级** |
 * | **DASH/HLS** | ❌ | ❌ | ❌ | **✅** |
 * | **ABR** | ❌ | ❌ | ❌ | **✅** |
 * **DRM** | ❌ | ❌ | ❌ | **✅** |
 * | **依赖大小** | 0KB | 0KB | 0KB | **~5-10MB** |
 * | **适用场景** | 快速原型 | 大多数场景 | 特效需求 | **专业应用** |
 *
 * @see BaseVideoPlayer 基类，包含完整的播放流程实现
 * @see Media3PlayerKernal ExoPlayer 引擎封装
 * @see ExoPlayerHelper ExoPlayer 辅助工具类
 * @see PlayerView ExoPlayer 官方视图组件
 */
class AndroidMedia3Player: BaseVideoPlayer() {

    /**
     * Media3 (ExoPlayer) 引擎实例
     *
     * 通过 [Media3PlayerKernal] 封装 ExoPlayer 的复杂性，
     * 提供统一的 [IPlayerKernal] 接口。
     *
     * 为什么使用 Media3PlayerKernal？
     * 1. **接口统一**：与其他引擎（MediaPlayer、IjkPlayer）保持一致
     * 2. **异常安全**：内置 try-catch 和错误恢复机制
     * 3. **线程安全**：确保回调在主线程执行
     * 4. **配置灵活**：支持自定义 RenderersFactory、TrackSelector 等
     *
     * 初始化时注册 [engineListener] 以接收所有引擎事件。
     */
    override var engine: IPlayerKernal<*> = Media3PlayerKernal().apply {
        registerListener(engineListener)
    }

    // ==================== 内部组件 ====================

    /**
     * PlayerView 实例（ExoPlayer 官方推荐的视图组件）
     *
     * PlayerView 是一个复合 View，包含：
     * - SurfaceView（用于视频渲染）
     * - SubtitleView（用于显示字幕）
     * - PlaybackControlView（用于播放控制，可隐藏或自定义）
     * - OverlayFrameLayout（用于放置自定义覆盖层）
     *
     * 由外部传入（通常在 XML 布局中定义），本类不创建。
     *
     * 与 VideoView 的区别：
     * - VideoView：Android 原生，简单但功能有限
     * - PlayerView：ExoPlayer 官方，功能强大且与 ExoPlayer 深度集成
     */
    private var playerView: PlayerView? = null

    // ==================== Surface 绑定 ====================

    /**
     * 绑定 PlayerView（推荐方式）
     *
     * PlayerView 是 ExoPlayer 官方推荐的视图组件，
     * **自动管理 Surface 生命周期**，无需手动处理 surfaceCreated/surfaceDestroyed。
     *
     * **调用时机：**
     * - Activity.onCreate / Fragment.onViewCreated
     * - 必须 View 已经添加到 Window
     *
     * **调用后的流程：**
     * 1. 先 detach 旧的绑定（如果有）
     * 2. 保存 PlayerView 引用
     * 3. 标记当前类型为 PLAYER_VIEW
     * 4. **立即标记 Surface 就绪**（PlayerView 无需等待）
     * 5. 初始化播放引擎（如果还未创建）
     * 6. 将 ExoPlayer 实例绑定到 PlayerView（关键步骤！）
     * 7. 如果有待执行的 prepare，立即执行
     *
     * **为什么 PlayerView 的 Surface 立即可用？**
     * PlayerView 内部在 attachToWindow 时就创建了 Surface，
     * 不像 SurfaceView 需要等待异步的 surfaceCreated 回调。
     * 这使得 PlayerView 的使用体验比 SurfaceView 更流畅。
     *
     * **注意事项：**
     * - 一个 PlayerView 同一时间只能绑定一个 ExoPlayer
     * - 一个 ExoPlayer 可以被多个 PlayerView 共享（画中画场景）
     * - 解绑时必须将 player 设为 null（避免内存泄漏）
     *
     * @param playerView 外部创建的 PlayerView 实例（不能为 null）
     *
     * @see PlayerView ExoPlayer 官方文档
     */
    fun attach(playerView: PlayerView) {
        // 先解绑旧的绑定（如果有）
        detach()

        this.playerView = playerView
        this.currentSurfaceType = SurfaceType.PLAYER_VIEW
        this.isSurfaceReady = true  // PlayerView 的 Surface 立即可用（这是关键优势）

        log(TAG, "attach PlayerView")

        // 初始化播放引擎（如果还没创建）
        initPlayer()

        // 将 ExoPlayer 实例绑定到 PlayerView（关键步骤！没有这一步画面无法显示）
        // getEnginePlayer() 返回底层的 ExoPlayer 实例
        playerView.player = engine.getEnginePlayer() as? Player?
        log(TAG, "bound to PlayerView")

        // 如果有待执行的 prepare（在 attach 之前就调用了 setSource），现在立即执行
        executePendingPrepare()
    }
    /**
     * 解绑当前 PlayerView（页面 onPause/onDestroyView 时调用）
     *
     * **调用时机：**
     * - Activity.onPause / Fragment.onPause（页面不可见时）
     * - Fragment.onDestroyView（View 销毁时）
     * - 切换 Surface 前（先 detach 旧的，再 attach 新的）
     *
     * **调用后的效果：**
     * - 将 PlayerView.player 设为 null（解绑 ExoPlayer）
     * - 清空 PlayerView 引用
     * - 标记 Surface 类型为 NONE
     * - **不释放引擎和其他资源**（可通过 attach 重新绑定）
     *
     * **为什么必须将 player 设为 null？**
     * PlayerView 内部持有 ExoPlayer 的引用，
     * 如果不设为 null，会导致：
     * 1. 内存泄漏（ExoPlayer 无法被 GC）
     * 2. View 泄漏（PlayerView 无法被回收）
     * 3. 后台继续播放（消耗电量和流量）
     *
     * **注意：**
     * - 不调用 release()（那是彻底销毁）
     * - 调用后可以通过 attach() 重新绑定
     */
    override fun detach() {
        when (currentSurfaceType) {
            SurfaceType.PLAYER_VIEW -> {
                // 关键：将 player 设为 null，避免内存泄漏
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
     * **解决的问题：**
     * 在播放过程中直接调用 detach() + attach() 会导致：
     * ```
     * IllegalStateException: setSurface() is valid only at Executing states;
     * currently at Released state
     * ```
     *
     * **根本原因：**
     * - detach() 会触发 ExoPlayer 异步释放 MediaCodec
     * - attach() 立即执行时，MediaCodec 可能还在 Released 状态
     * - 此时调用 setSurface() 会抛出异常
     *
     * **ExoPlayer 的解决方案（方案 A：暂停 → 切换 → 恢复）：**
     * 与 MediaPlayer 的"stop → 切换 → reprepare"不同，
     * ExoPlayer 使用更轻量的策略：
     * 1. **暂停播放**：让 MediaCodec 进入 Flushed 状态（安全状态）
     * 2. **延迟等待**：确保 PAUSED 状态稳定生效（默认 100ms）
     * 3. **执行切换**：detach + attach（此时 MediaCodec 已在安全状态）
     * 4. **恢复播放**：从保存的位置继续播放
     *
     * **为什么 ExoPlayer 可以用"暂停"而不是"stop"？**
     * - ExoPlayer 的 MediaCodec 状态机比 MediaPlayer 更灵活
     * - PAUSED 状态下 MediaCodec 仍在 Executing 状态，可以安全切换 Surface
     * - 不需要重新 prepare，节省时间
     *
     * **使用示例：**
     * ```kotlin
     * // 切换到新的 PlayerView
     * player.safeSwitchToPlayerView(newPlayerView)
     *
     * // 自定义延迟时间（某些设备需要更长等待）
     * player.safeSwitchToPlayerView(newPlayerView, delayMs = 200L)
     * ```
     */

    /**
     * 安全切换到 PlayerView
     *
     * **使用场景：**
     * - 从全屏模式返回小窗模式
     * - 从一个 PlayerView 切换到另一个 PlayerView（画中画）
     * - 动态更换视频渲染容器
     *
     * **内部流程：**
     * 1. 调用基类的 safeSwitchSurface() 方法
     * 2. 基类会自动处理：
     *    - 防重复调用检查
     *    - 保存当前播放位置和状态
     *    - 暂停播放（让 MediaCodec 进入安全状态）
     *    - 延迟等待（确保状态稳定）
     *    - 执行 attach(playerView)
     *    - 恢复播放位置
     *
     * **参数说明：**
     * @param playerView 目标 PlayerView 实例（必须已添加到 Window）
     * @param delayMs 延迟时间（毫秒），默认 100ms
     *   - 大多数设备 100ms 足够
     *   - 某些低端设备可能需要 200ms ~ 500ms
     *   - 如果仍然崩溃，可以尝试增大此值
     */
    fun safeSwitchToPlayerView(playerView: PlayerView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(playerView) },
            targetName = "PlayerView",
            delayMs = delayMs
        )
    }

    // ==================== 内部：画面自适应 ====================

    /**
     * 根据当前缩放模式调整 PlayerView 的显示方式
     *
     * **调用时机：**
     * - onVideoSizeChanged 回调中（获取到视频尺寸后）
     * - videoScaleMode 属性改变时（用户切换缩放模式）
     *
     * **不同类型的处理方式：**
     * - **PLAYER_VIEW**：通过 [ExoPlayerHelper.applyVideoScaleMode] 调整
     *   PlayerView 内置了 resizeMode 属性，支持：
     *   - RESIZE_MODE_FIT：适应容器（可能有黑边）
     *   - RESIZE_MODE_FIXED_WIDTH：固定宽度，高度自适应
     *   - RESIZE_MODE_FIXED_HEIGHT：固定高度，宽度自适应
     *   - RESIZE_MODE_FILL：填满容器（可能变形）
     *   - RESIZE_MODE_ZOOM：放大填满（可能裁剪）
     *
     * - **其他类型**（SurfaceView/TextureView）：调用父类的实现
     *   通过调整 View 的 LayoutParams 来实现缩放
     *
     * **注意：**
     * PlayerView 的缩放模式与 VideoScaleMode 枚举的映射关系
     * 由 [ExoPlayerHelper] 负责，本类不关心具体实现。
     *
     * @see ExoPlayerHelper.applyVideoScaleMode PlayerView 缩放模式设置
     * @see VideoScaleMode 缩放模式枚举定义
     */
    override fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.PLAYER_VIEW -> ExoPlayerHelper.applyVideoScaleMode(playerView, videoScaleMode)
            else -> { super.adjustSurfaceLayout() }
        }
    }
}
