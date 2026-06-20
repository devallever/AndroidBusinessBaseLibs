package app.allever.android.sample.audiovideo.android

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.VideoView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

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
class AndroidMediaPlayer {

    // ==================== 内部组件 ====================

    /** MediaPlayer 实例 */
    private var mediaPlayer: MediaPlayer? = null

    /** 监听器回调 */
    private var listener: IVideoPlayerListener? = null

    // ==================== Surface 绑定（三种模式）====================

    /** VideoView 绑定 */
    private var videoView: VideoView? = null

    /** SurfaceView 绑定 */
    private var surfaceView: SurfaceView? = null

    /** TextureView 绑定 */
    private var textureView: TextureView? = null

    /** 当前绑定的 Surface 类型 */
    private enum class SurfaceType {
        NONE,           // 未绑定
        VIDEO_VIEW,     // VideoView
        SURFACE_VIEW,   // SurfaceView
        TEXTURE_VIEW    // TextureView
    }

    private var currentSurfaceType: SurfaceType = SurfaceType.NONE

    /** Surface 是否已就绪（可用于渲染）*/
    @Volatile
    private var isSurfaceReady: Boolean = false

    /** 是否正在执行 seek 操作（防止 seek 过程中误停进度追踪）*/
    @Volatile
    private var isSeeking: Boolean = false

    // ==================== 状态管理 ====================

    private var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("AndroidMP", "state: $old -> $value")
                field = value
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态 */
    val state get() = _state

    /** 是否正在播放 */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING && try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }

    /** 当前位置（毫秒）*/
    val currentPosition: Long
        get() = try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try {
            mediaPlayer?.duration?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }

    // ==================== 配置属性 ====================

    /** 循环模式，默认不循环 */
    var loopMode: LoopMode = LoopMode.NONE
        set(value) {
            field = value
            applyLoopMode()
        }

    /** 进度回调间隔（毫秒），默认 200ms */
    var progressIntervalMs: Int = 200

    /** 自动重试次数（出错时自动重试 prepare），默认 0 不重试 */
    var retryCount: Int = 0

    /** 变速倍率（0.5 ~ 3.0），默认 1.0 */
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 3.0f)
            applySpeed()
        }

    /** 音量（0.0 ~ 1.0），默认 1.0 */
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            try {
                mediaPlayer?.setVolume(field, field)
            } catch (_: Exception) {}
        }

    /**
     * 视频缩放模式（默认 FIT_CENTER）
     *
     * 对于 VideoView：通过调整布局尺寸实现
     * 对于 SurfaceView/TextureView：通过调整布局尺寸实现
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            if (videoWidth > 0 && videoHeight > 0) {
                adjustSurfaceLayout()
            }
        }

    // ==================== 内部状态 ====================

    /** 进度追踪协程 */
    private var progressJob: Job? = null

    /** PREPARING 状态监控协程 */
    private var preparingMonitorJob: Job? = null

    /** 当前数据源 URI */
    private var currentUri: Uri? = null

    /** 当前数据源 HTTP 头 */
    private var currentHeaders: Map<String, String>? = null

    /** 当前数据源 Asset 路径（如果是 Assets 文件） */
    private var currentAssetPath: String? = null

    /** 剩余重试次数 */
    private var retryLeft: Int = 0

    /** 视频原始宽度（像素）*/
    private var videoWidth: Int = 0

    /** 视频原始高度（像素）*/
    private var videoHeight: Int = 0

    /**
     * 待执行的 prepare 参数（当 Surface 未就绪时缓存 setSource 调用）
     *
     * 使用场景：
     * 1. 用户调用 attach(surfaceView) → Surface 还在异步创建中
     * 2. 用户立即调用 setSource(url)
     * 3. 此时 Surface 未就绪 → 存入 pendingPrepare
     * 4. surfaceCreated / onSurfaceReady 回调触发 → 自动执行缓存的 prepare
     */
    private data class PendingPrepare(
        val uri: Uri,
        val headers: Map<String, String>?,
        val assetPath: String?
    )

    private var pendingPrepare: PendingPrepare? = null

    /** 切换 Surface 后待恢复的播放位置（-1 表示无需恢复） */
    private var pendingSeekPosition: Long = -1L

    /** 是否正在执行安全切换操作（防止重复调用） */
    @Volatile
    private var isSafeSwitching: Boolean = false

    // ==================== Surface 绑定 API ====================

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

        log("AndroidMP", "attach VideoView (waiting for surface)")

        initMediaPlayer()

        // 设置 SurfaceHolder 回调以监听 Surface 就绪
        videoView.holder?.addCallback(videoViewSurfaceCallback)

        // 检查 Surface 是否已经可用
        if (videoView.holder?.surface?.isValid == true) {
            onSurfaceReady(videoView.holder!!.surface)
        }
    }

    /**
     * 绑定 SurfaceView（兼容方式）
     *
     * 会设置 SurfaceHolder.Callback 监听 Surface 创建/销毁/变化。
     * Surface 可能需要时间才能就绪，此时会使用 PendingPrepare 机制缓存操作。
     *
     * @param surfaceView 外部创建的 SurfaceView 实例
     */
    fun attach(surfaceView: SurfaceView) {
        detach()

        this.surfaceView = surfaceView
        this.currentSurfaceType = SurfaceType.SURFACE_VIEW
        this.isSurfaceReady = false  // Surface 需要异步创建

        log("AndroidMP", "attach SurfaceView (waiting for surface)")

        initMediaPlayer()
        setupSurfaceViewCallback()

        // 检查 Surface 是否已经可用（某些情况下立即可用）
        if (surfaceView.holder.surface.isValid) {
            onSurfaceReady(surfaceView.holder.surface)
        }
    }

    /**
     * 绑定 TextureView（高级方式）
     *
     * 会设置 SurfaceTextureListener 监听 Surface 可用/尺寸变化/销毁。
     * TextureView 的 Surface 通常比 SurfaceView 更快可用。
     *
     * @param textureView 外部创建的 TextureView 实例
     */
    fun attach(textureView: TextureView) {
        detach()

        this.textureView = textureView
        this.currentSurfaceType = SurfaceType.TEXTURE_VIEW
        this.isSurfaceReady = false  // Surface 需要异步准备

        log("AndroidMP", "attach TextureView (waiting for surface)")

        initMediaPlayer()
        setupTextureViewCallback()

        // 检查 Surface 是否已经可用
        if (textureView.isAvailable) {
            onSurfaceReady(Surface(textureView.surfaceTexture))
        }
    }

    /**
     * 解绑当前 Surface（页面 onPause/onDestroyView 时调用）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     * 不释放内部 MediaPlayer 和其他资源。
     */
    fun detach() {
        when (currentSurfaceType) {
            SurfaceType.VIDEO_VIEW -> {
                videoView?.holder?.removeCallback(videoViewSurfaceCallback)
                videoView = null
                isSurfaceReady = false
                log("AndroidMP", "detach VideoView")
            }
            SurfaceType.SURFACE_VIEW -> {
                surfaceView?.holder?.removeCallback(surfaceHolderCallback)
                surfaceView = null
                isSurfaceReady = false
                log("AndroidMP", "detach SurfaceView")
            }
            SurfaceType.TEXTURE_VIEW -> {
                textureView?.surfaceTextureListener = null
                textureView = null
                isSurfaceReady = false
                log("AndroidMP", "detach TextureView")
            }
            SurfaceType.NONE -> {}
        }

        currentSurfaceType = SurfaceType.NONE
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

    /**
     * 安全切换到 SurfaceView
     *
     * @param surfaceView 目标 SurfaceView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaPlayer 状态稳定
     */
    fun safeSwitchToSurfaceView(surfaceView: SurfaceView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(surfaceView) },
            targetName = "SurfaceView",
            delayMs = delayMs
        )
    }

    /**
     * 安全切换到 TextureView
     *
     * @param textureView 目标 TextureView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaPlayer 状态稳定
     */
    fun safeSwitchToTextureView(textureView: TextureView, delayMs: Long = 100L) {
        safeSwitchSurface(
            targetAction = { attach(textureView) },
            targetName = "TextureView",
            delayMs = delayMs
        )
    }

    /**
     * 安全切换 Surface 的核心实现
     *
     * **方案 B：stop → 切换 → reprepare**
     *
     * 流程时间线：
     * T0: 用户点击切换
     *    ├─ 记录状态 (wasPlaying, savedPosition)
     *    └─ stop() → 清空所有缓冲区
     * T0+100ms:
     *    ├─ detach() + attach() → 安全切换 Surface
     *    └─ doSetSource(currentUri, currentHeaders) → 重新准备
     * T0+500ms~1s (异步):
     *    └─ onPrepared 触发
     *       ├─ seekTo(savedPosition) → 恢复位置
     *       └─ play() → 继续播放
     *
     * @param targetAction 实际的 attach 操作
     * @param targetName 目标名称（用于日志）
     * @param delayMs 延迟时间（毫秒），默认 100ms
     */
    private fun safeSwitchSurface(
        targetAction: () -> Unit,
        targetName: String,
        delayMs: Long = 100L
    ) {
        // 防重复调用
        if (isSafeSwitching) {
            log("AndroidMP", "safeSwitchSurface [方案B]: ⚠️ 忽略重复调用（正在切换到 $targetName）")
            return
        }
        
        isSafeSwitching = true
        
        try {
            // 1. 记录当前状态（扩展到 PREPARING 状态）
            val wasPlaying = (_state == PlayerState.PLAYING || _state == PlayerState.PAUSED ||
                    _state == PlayerState.PREPARING)
            val savedPosition = currentPosition

            log("AndroidMP", "safeSwitchSurface [方案B]: 开始切换到 $targetName" +
                    " (wasPlaying=$wasPlaying, position=${formatTime(savedPosition)}, state=$_state)")

            // 2. 完全停止 MediaPlayer（清空所有缓冲区和渲染队列）
            if (_state != PlayerState.IDLE && _state != PlayerState.STOPPED && _state != PlayerState.RELEASED) {
                stop()
                log("AndroidMP", "safeSwitchSurface [方案B]: 已 stop()，清空所有缓冲区")
            }

            // 3. 如果需要恢复播放，保存位置信息
            if (wasPlaying && savedPosition >= 0 && currentUri != null) {
                pendingSeekPosition = savedPosition
                log("AndroidMP", "safeSwitchSurface [方案B]: 待恢复位置 ${formatTime(savedPosition)}")
            }

            // 4. 使用 postDelayed 延迟执行切换操作
            App.mainHandler.postDelayed({
                try {
                    log("AndroidMP", "safeSwitchSurface [方案B]: 执行切换到 $targetName")

                    // 执行实际的切换操作（detach + attach）
                    targetAction()

                    // ✨ 确保 Surface 完全就绪后再 prepare
                    val surfaceReady = when (currentSurfaceType) {
                        SurfaceType.VIDEO_VIEW -> {
                            videoView?.holder?.surface?.isValid == true
                        }
                        SurfaceType.SURFACE_VIEW -> {
                            surfaceView?.holder?.surface?.isValid == true
                        }
                        SurfaceType.TEXTURE_VIEW -> {
                            textureView?.let { tv ->
                                tv.isAvailable && tv.surfaceTexture != null
                            } == true
                        }
                        else -> true
                    }

                    if (!surfaceReady && currentSurfaceType != SurfaceType.NONE) {
                        log("AndroidMP", "safeSwitchSurface [方案B]: ⚠️ Surface 未就绪，延迟 50ms 等待...")
                        
                        App.mainHandler.postDelayed({
                            try {
                                performPrepareAfterSwitch()
                            } catch (e: Exception) {
                                handlePrepareFailure(e)
                            }
                        }, 50L)
                    } else {
                        log("AndroidMP", "safeSwitchSurface [方案B]: Surface 已就绪，立即 prepare")
                        try {
                            performPrepareAfterSwitch()
                        } catch (e: Exception) {
                            handlePrepareFailure(e)
                        }
                    }
                } catch (e: Exception) {
                    log("AndroidMP", "safeSwitchSurface [方案B]: 切换失败 - ${e.message}")
                    pendingSeekPosition = -1L
                    _state = PlayerState.ERROR
                    listener?.onError(PlayerErrorCode.SURFACE_SWITCH_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.SURFACE_SWITCH_FAILED, e.message))
                } finally {
                    isSafeSwitching = false
                }
            }, delayMs)

        } catch (e: Exception) {
            log("AndroidMP", "safeSwitchSurface [方案B]: 准备阶段失败 - ${e.message}")
            isSafeSwitching = false
            pendingSeekPosition = -1L
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
        }
    }

    /**
     * 执行切换后的 prepare 操作（统一入口）
     */
    private fun performPrepareAfterSwitch() {
        if (currentUri == null && currentAssetPath == null) {
            log("AndroidMP", "safeSwitchSurface [方案B]: 切换完成（无数据源）")
            return
        }

        log("AndroidMP", "safeSwitchSurface [方案B]: 重新准备数据源" +
                " (autoResume=${pendingSeekPosition >= 0})")

        if (currentAssetPath != null && currentAssetPath!!.isNotEmpty()) {
            // Assets 数据源：必须手动处理文件复制 + prepare
            log("AndroidMP", "safeSwitchSurface [方案B]: 使用 setAssetSource (assets)")

            try {
                val context = App.context.applicationContext
                val cacheFile = File(context.cacheDir, "asset_video_${currentAssetPath!!.hashCode()}")

                // 如果缓存文件不存在，从 assets 重新复制
                if (!cacheFile.exists()) {
                    context.assets.open(currentAssetPath!!).use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    log("AndroidMP", "safeSwitchSurface [方案B]: 已重新复制 asset 文件")
                }

                // 直接使用 doPrepareInternal，跳过 isSurfaceReady 检查
                val cacheUri = Uri.fromFile(cacheFile)
                currentUri = cacheUri
                doPrepareInternal(cacheUri, currentHeaders)

            } catch (e: Exception) {
                log("AndroidMP", "safeSwitchSurface [方案B]: Asset 处理失败 - ${e.message}")
                handlePrepareFailure(e)
            }

        } else if (currentUri != null) {
            // 普通 URI（HTTP/本地文件/Content Provider）：直接 prepare
            log("AndroidMP", "safeSwitchSurface [方案B]: 使用 doPrepareInternal (uri=$currentUri)")
            doPrepareInternal(currentUri!!, currentHeaders)
        }

        log("AndroidMP", "safeSwitchSurface [方案B]: prepare 完成，等待 onPrepared 或 PREPARING Monitor")
    }

    /**
     * 处理 prepare 失败的情况
     */
    private fun handlePrepareFailure(e: Exception) {
        log("AndroidMP", "safeSwitchSurface [方案B]: prepare 失败 - ${e.message}")
        pendingSeekPosition = -1L
        _state = PlayerState.ERROR
        listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
    }

    // ==================== 数据源设置 ====================

    /**
     * 设置数据源并开始准备（不自动播放）
     *
     * 支持的数据源类型：
     * - HTTP/HTTPS URL：在线视频
     * - file:// 路径：本地文件
     * - content:// URI：Content Provider
     * - file:///android_asset/filename.mp4：Assets 目录（自动复制到缓存）
     *
     * 准备完成后回调 [IVideoPlayerListener.onPrepared]，此时需调用 [play] 开始播放。
     *
     * @param url 数据源地址
     */
    fun setSource(url: String) {
        val uri = Uri.parse(url)

        // 处理 Assets 文件（需特殊处理）
        if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
            val assetPath = uri.path?.substringAfter("/android_asset/") ?: ""
            setAssetSource(assetPath)
            return
        }

        doSetSource(uri, null, null)
    }

    /**
     * 设置视频数据源并准备（不自动播放）
     *
     * 支持的数据源类型：
     * - HTTP/HTTPS URI：在线视频（支持自定义请求头，如 Cookie、Referer 等）
     * - file:// URI：本地文件
     * - content:// URI：Content Provider
     *
     * 准备完成后回调 [IVideoPlayerListener.onPrepared]，此时需调用 [play] 开始播放。
     *
     * @param uri 视频 URI（支持 http/https/file/content 协议）
     * @param headers HTTP 请求头（仅对 http(s) 协议生效，可为 null）
     */
    fun setSource(uri: Uri, headers: Map<String, String>? = null) {
        // 处理 Assets 文件（需特殊处理）
        if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
            val assetPath = uri.path?.substringAfter("/android_asset/") ?: ""
            setAssetSource(assetPath)
            return
        }

        doSetSource(uri, headers, null)
    }

    /**
     * 设置 assets 目录下的视频文件并准备（不自动播放）
     *
     * 由于 MediaPlayer 无法直接读取 Assets 中的文件，
     * 此方法会将文件复制到内部缓存目录后再加载。
     *
     * @param path Assets 中的相对路径（如 "video/test.mp4"）
     */
    fun setAssetSource(path: String) {
        try {
            val context = App.context.applicationContext
            val cacheFile = File(context.cacheDir, "asset_video_${path.hashCode()}")

            // 如果缓存文件不存在或 Assets 文件更新了，重新复制
            if (!cacheFile.exists()) {
                context.assets.open(path).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                log("AndroidMP", "copied asset to cache: ${cacheFile.absolutePath}")
            }

            doSetSource(Uri.fromFile(cacheFile), null, path)
        } catch (e: Exception) {
            log("AndroidMP", "setAssetSource error: ${e.message}")
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.ASSET_COPY_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.ASSET_COPY_FAILED, e.message))
        }
    }

    /**
     * 执行实际的 setSource 操作
     */
    private fun doSetSource(uri: Uri, headers: Map<String, String>?, assetPath: String?) {
        if (_state == PlayerState.RELEASED) return

        // 停止当前的进度追踪（切换数据源前必须清理）
        stopProgressTracking()

        currentUri = uri
        currentHeaders = headers
        currentAssetPath = assetPath
        retryLeft = retryCount

        // 如果 Surface 未就绪，缓存待执行的 prepare
        if (!isSurfaceReady && currentSurfaceType != SurfaceType.NONE) {
            log("AndroidMP", "Surface not ready, caching prepare request")
            pendingPrepare = PendingPrepare(uri, headers, assetPath)
            _state = PlayerState.PREPARING
            return
        }

        doPrepareInternal(uri, headers)
    }

    /**
     * 执行缓存的 prepare 操作
     */
    private fun executePendingPrepare() {
        pendingPrepare?.let { pending ->
            log("AndroidMP", "executing pending prepare: ${pending.uri}")
            pendingPrepare = null
            doPrepareInternal(pending.uri, pending.headers)
        }
    }

    // ==================== 播放控制 ====================

    /**
     * 开始播放 或 从暂停恢复播放
     *
     * - PREPARED/COMPLETED → 开始播放
     * - PAUSED → 恢复播放
     * - 其他状态 → 忽略
     */
    fun play() {
        when (_state) {
            PlayerState.PREPARED, PlayerState.COMPLETED -> {
                mediaPlayer?.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                startPreparingStateMonitor()
                log("AndroidMP", "play() -> PLAYING (from $_state)")
            }
            PlayerState.PAUSED -> {
                mediaPlayer?.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log("AndroidMP", "play() -> PLAYING (from PAUSED)")
            }
            else -> {
                log("AndroidMP", "play() ignored (current state: $_state)")
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        if (_state == PlayerState.PLAYING) {
            mediaPlayer?.pause()
            _state = PlayerState.PAUSED
            stopProgressTracking()
            log("AndroidMP", "pause() -> PAUSED")
        }
    }

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    fun stop() {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return

        stopProgressTracking()
        stopPreparingMonitor()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (_: Exception) {}

        _state = PlayerState.STOPPED
        log("AndroidMP", "stop() -> STOPPED")
    }

    /**
     * 跳转到指定位置
     *
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            isSeeking = true
            mediaPlayer?.seekTo(positionMs.toInt())
            // 延迟重置标志并确保进度追踪正常运行
            App.mainHandler.postDelayed({
                isSeeking = false
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    log("AndroidMP", "restart progress tracking after seek")
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
            log("AndroidMP", "seekTo error: ${e.message}")
            isSeeking = false
        }
    }

    // ==================== 生命周期 ====================

    /**
     * 释放所有资源，调用后不可再使用此实例
     */
    fun release() {
        detach()
        releaseMediaPlayer()
        currentUri = null
        currentHeaders = null
        currentAssetPath = null
        pendingPrepare = null
        pendingSeekPosition = -1L
        _state = PlayerState.RELEASED
        log("AndroidMP", "release() -> RELEASED")
    }

    // ==================== 监听器设置 ====================

    /**
     * 设置播放事件监听器
     */
    fun setListener(listener: IVideoPlayerListener?) {
        this.listener = listener
    }

    // ==================== 私有方法：初始化 ====================

    /**
     * 初始化 MediaPlayer 实例
     */
    private fun initMediaPlayer() {
        if (mediaPlayer != null) return

        mediaPlayer = MediaPlayer().apply {
            // 设置各种监听器
            setOnPreparedListener(mOnPreparedListener)
            setOnCompletionListener(mOnCompletionListener)
            setOnErrorListener(mOnErrorListener)
            setOnBufferingUpdateListener(mOnBufferingUpdateListener)
            setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
            setOnInfoListener(mOnInfoListener)
            
            // 应用初始配置
            applyLoopMode()
            applySpeed()
            if (volume != 1.0f) {
                setVolume(volume, volume)
            }
        }

        log("AndroidMP", "MediaPlayer initialized")
    }

    /**
     * 设置 SurfaceView 的 SurfaceHolder 回调
     */
    private fun setupSurfaceViewCallback() {
        surfaceView?.holder?.addCallback(surfaceHolderCallback)
    }

    /**
     * 设置 TextureView 的 SurfaceTextureListener 回调
     */
    private fun setupTextureViewCallback() {
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                log("AndroidMP", "onSurfaceTextureAvailable: ${width}x${height}")
                onSurfaceReady(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                log("AndroidMP", "onSurfaceTextureSizeChanged: ${width}x${height}")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log("AndroidMP", "onSurfaceTextureDestroyed")
                isSurfaceReady = false
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    // ==================== 回调定义 ====================

    /** VideoView 的 SurfaceHolder 回调 */
    private val videoViewSurfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            log("AndroidMP", "VideoView surfaceCreated")
            onSurfaceReady(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log("AndroidMP", "VideoView surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log("AndroidMP", "VideoView surfaceDestroyed")
            isSurfaceReady = false
        }
    }

    /** SurfaceView 的 SurfaceHolder 回调 */
    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            log("AndroidMP", "surfaceCreated")
            onSurfaceReady(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log("AndroidMP", "surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log("AndroidMP", "surfaceDestroyed")
            isSurfaceReady = false
        }
    }

    /** OnPreparedListener：准备完成回调 */
    private val mOnPreparedListener = MediaPlayer.OnPreparedListener {
        App.mainHandler.post {
            val dur = try { mediaPlayer?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }

            if (_state == PlayerState.PREPARING) {
                _state = PlayerState.PREPARED
            }

            listener?.onPrepared(dur)
            log("AndroidMP", "onPrepared (duration=${dur}ms)")

            // 检查是否需要自动恢复播放（Surface 切换后）
            val shouldAutoResume = pendingSeekPosition >= 0
            val savedPos = pendingSeekPosition
            pendingSeekPosition = -1L  // 重置标记

            log("AndroidMP", "onPrepared: autoResume=$shouldAutoResume" +
                    (if (shouldAutoResume) ", savedPosition=${formatTime(savedPos)}" else ""))

            // 如果是 Surface 切换后的 reprepare，自动恢复播放
            if (shouldAutoResume && savedPos!! >= 0) {
                log("AndroidMP", "safeSwitchSurface [方案B]: 自动恢复播放 (position=${formatTime(savedPos)})")
                
                // ✨ 关键：MediaPlayer 必须先 start 再 seekTo
                play()
                seekTo(savedPos)
                
                log("AndroidMP", "safeSwitchSurface [方案B]: 已恢复播放 (${formatTime(savedPos)})")
            }
        }
    }

    /** OnCompletionListener：播放完成回调 */
    private val mOnCompletionListener = MediaPlayer.OnCompletionListener {
        App.mainHandler.post {
            if (_state == PlayerState.PLAYING) {
                _state = PlayerState.COMPLETED
                stopProgressTracking()
                stopPreparingMonitor()
                listener?.onComplete()
                log("AndroidMP", "onComplete")
            }
        }
    }

    /** OnErrorListener：错误回调 */
    private val mOnErrorListener = MediaPlayer.OnErrorListener { _, what, extra ->
        App.mainHandler.post {
            log("AndroidMP", "onError: what=$what, extra=$extra")

            // 将 MediaPlayer 错误代码映射到 PlayerErrorCode
            val errorCode = mapMediaPlayerError(what, extra)
            val errorMsg = PlayerErrorCode.formatError(errorCode, "MediaPlayer error: what=$what, extra=$extra")

            if (_state == PlayerState.PREPARING) {
                // 准备阶段出错，尝试重试
                handlePrepareError(Exception(errorMsg))
            } else {
                // 播放阶段出错
                _state = PlayerState.ERROR
                listener?.onError(errorCode, errorMsg)
            }
        }
        true  // 返回 true 表示已处理错误
    }

    /**
     * 将 MediaPlayer 的错误代码映射到 PlayerErrorCode
     *
     * @param what MediaPlayer 错误类型
     * @param extra 额外错误信息
     * @return 对应的 PlayerErrorCode
     */
    private fun mapMediaPlayerError(what: Int, extra: Int): Int {
        return when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> PlayerErrorCode.MEDIA_PLAYER_INTERNAL_ERROR
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> PlayerErrorCode.SERVER_ERROR
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_IO -> when (extra) {
                else -> PlayerErrorCode.FILE_READ_ERROR
            }
            MediaPlayer.MEDIA_ERROR_MALFORMED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlayerErrorCode.NETWORK_TIMEOUT
            else -> PlayerErrorCode.UNKNOWN
        }
    }

    /** OnBufferingUpdateListener：缓冲进度回调 */
    private val mOnBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { _, percent ->
        App.mainHandler.post {
            if (percent > 0) {
                listener?.onBufferingUpdate(percent)
            }
        }
    }

    /** OnVideoSizeChangedListener：视频尺寸回调 */
    private val mOnVideoSizeChangedListener = MediaPlayer.OnVideoSizeChangedListener { _, width, height ->
        App.mainHandler.post {
            if (width > 0 && height > 0) {
                videoWidth = width
                videoHeight = height
                
                listener?.onVideoSizeChanged(width, height)
                log("AndroidMP", "onVideoSizeChanged: ${width}x${height}")

                // 对非 VideoView 进行画面自适应
                if (currentSurfaceType == SurfaceType.SURFACE_VIEW ||
                    currentSurfaceType == SurfaceType.TEXTURE_VIEW ||
                    currentSurfaceType == SurfaceType.VIDEO_VIEW) {
                    adjustSurfaceLayout()
                }
            }
        }
    }

    /** OnInfoListener：信息回调（如缓冲开始/结束） */
    private val mOnInfoListener = MediaPlayer.OnInfoListener { mp, what, extra ->
        App.mainHandler.post {
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    log("AndroidMP", "MEDIA_INFO_BUFFERING_START")
                    // 可以在这里显示缓冲指示器
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    log("AndroidMP", "MEDIA_INFO_BUFFERING_END")
                    // 可以在这里隐藏缓冲指示器
                }
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    log("AndroidMP", "MEDIA_INFO_VIDEO_RENDERING_START")
                    // 视频首帧渲染
                }
            }
        }
        false
    }

    /**
     * Surface 就绪处理（统一入口）
     */
    private fun onSurfaceReady(surface: Surface) {
        isSurfaceReady = true
        log("AndroidMP", "Surface ready")

        // 将 Surface 设置给 MediaPlayer
        try {
            mediaPlayer?.setSurface(surface)
        } catch (e: Exception) {
            log("AndroidMP", "setSurface error: ${e.message}")
        }

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
        }
    }

    // ==================== 私有方法：准备流程 ====================

    /**
     * 执行实际的 prepare 操作
     */
    private fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?) {
        val srcUri = uri ?: return

        // 确保 MediaPlayer 存在
        if (mediaPlayer == null) {
            initMediaPlayer()
        }

        try {
            // 重置 MediaPlayer（重要：确保处于 Idle 状态）
            mediaPlayer?.reset()

            // 设置数据源
            when (srcUri.scheme) {
                "http", "https" -> {
                    // HTTP/HTTPS 数据源
                    if (headers != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // API 21+ 支持设置请求头
                        mediaPlayer?.setDataSource(App.context.applicationContext, srcUri, headers)
                    } else {
                        mediaPlayer?.setDataSource(srcUri.toString())
                    }
                }
                "content" -> {
                    // Content Provider
                    mediaPlayer?.setDataSource(App.context.applicationContext, srcUri)
                }
                "file" -> {
                    // 本地文件
                    mediaPlayer?.setDataSource(srcUri.toString())
                }
                else -> {
                    // 其他情况尝试直接设置
                    mediaPlayer?.setDataSource(srcUri.toString())
                }
            }

            // 绑定当前 Surface（必须在 reset 之后、prepareAsync 之前）
            bindCurrentSurface()

            // 异步准备
            mediaPlayer?.prepareAsync()

            // 切换到 PREPARING 状态
            _state = PlayerState.PREPARING
            log("AndroidMP", "state -> PREPARING (prepare: $srcUri)")

            // 启动 PREPARING 状态监控（作为 onPrepared 的备用方案）
            startPreparingStateMonitor()

        } catch (e: Exception) {
            log("AndroidMP", "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    /**
     * 绑定当前 Surface 到 MediaPlayer
     */
    private fun bindCurrentSurface() {
        var surfaceBound = false
        
        when (currentSurfaceType) {
            SurfaceType.VIDEO_VIEW -> {
                videoView?.holder?.let { holder ->
                    if (holder.surface.isValid) {
                        try {
                            mediaPlayer?.setDisplay(holder)
                            surfaceBound = true
                            log("AndroidMP", "bindSurface: VideoView success")
                        } catch (e: Exception) {
                            log("AndroidMP", "bindSurface: VideoView error - ${e.message}")
                        }
                    } else {
                        log("AndroidMP", "bindSurface: VideoView invalid")
                    }
                }
            }
            SurfaceType.SURFACE_VIEW -> {
                surfaceView?.holder?.let { holder ->
                    if (holder.surface.isValid) {
                        try {
                            mediaPlayer?.setDisplay(holder)
                            surfaceBound = true
                            log("AndroidMP", "bindSurface: SurfaceView success")
                        } catch (e: Exception) {
                            log("AndroidMP", "bindSurface: SurfaceView error - ${e.message}")
                        }
                    } else {
                        log("AndroidMP", "bindSurface: SurfaceView invalid")
                    }
                }
            }
            SurfaceType.TEXTURE_VIEW -> {
                textureView?.let { tv ->
                    val surfaceTexture = tv.surfaceTexture
                    if (tv.isAvailable && surfaceTexture != null) {
                        try {
                            val surface = Surface(surfaceTexture)
                            if (surface.isValid) {
                                mediaPlayer?.setSurface(surface)
                                surfaceBound = true
                                log("AndroidMP", "bindSurface: TextureView success")
                            } else {
                                log("AndroidMP", "bindSurface: TextureView Surface invalid")
                                surface.release()
                            }
                        } catch (e: Exception) {
                            log("AndroidMP", "bindSurface: TextureView error - ${e.message}")
                        }
                    } else {
                        log("AndroidMP", "bindSurface: TextureView not ready " +
                                "(isAvailable=${tv.isAvailable}, surfaceTexture=$surfaceTexture)")
                    }
                }
            }
            else -> {}
        }

        if (!surfaceBound && currentSurfaceType != SurfaceType.NONE) {
            log("AndroidMP", "bindSurface: ⚠️ Surface not bound, may have no video output")
        }
    }

    /**
     * 处理准备错误（可能触发重试）
     */
    private fun handlePrepareError(e: Exception) {
        if (retryLeft > 0) {
            retryLeft--
            log("AndroidMP", "retrying... ($retryLeft left)")
            App.mainHandler.postDelayed({
                doPrepareInternal(currentUri, currentHeaders)
            }, 1000)
        } else {
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.RETRY_EXHAUSTED, PlayerErrorCode.formatError(PlayerErrorCode.RETRY_EXHAUSTED, e.message))
        }
    }

    // ==================== 私有方法：配置应用 ====================

    /**
     * 应用循环模式到 MediaPlayer
     */
    private fun applyLoopMode() {
        val looping = when (loopMode) {
            LoopMode.SINGLE -> true
            LoopMode.ALL -> true
            else -> false
        }
        try {
            mediaPlayer?.isLooping = looping
        } catch (_: Exception) {}
    }

    /**
     * 应用变速到 MediaPlayer
     */
    private fun applySpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = android.media.PlaybackParams().setSpeed(speed)
            } catch (e: Exception) {
                log("AndroidMP", "setSpeed error: ${e.message}")
            }
        } else {
            log("AndroidMP", "setSpeed not supported below API 23")
        }
    }

    // ==================== 私有方法：资源释放 ====================

    /**
     * 释放 MediaPlayer 实例
     */
    private fun releaseMediaPlayer() {
        stopProgressTracking()
        stopPreparingMonitor()
        try {
            mediaPlayer?.apply {
                setOnPreparedListener(null)
                setOnCompletionListener(null)
                setOnErrorListener(null)
                setOnBufferingUpdateListener(null)
                setOnVideoSizeChangedListener(null)
                setOnInfoListener(null)
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        log("AndroidMP", "MediaPlayer released")
    }

    // ==================== 内部：PREPARING State Monitor ====================

    /**
     * 启动 PREPARING 状态监控协程
     *
     * 作为 onPrepared 回调的备用方案：
     * 在某些情况下（特别是快速 stop+reprepare），
     * MediaPlayer 可能不触发 OnPreparedListener，
     * 此时会通过轮询检测播放是否已经开始。
     */
    private fun startPreparingStateMonitor() {
        preparingMonitorJob?.cancel()
        preparingMonitorJob = CoroutineScope(Dispatchers.Main).launch {
            val maxCheckTime = 30000L  // 最大检查时间 30 秒
            val startTime = System.currentTimeMillis()

            while (isActive && System.currentTimeMillis() - startTime < maxCheckTime) {
                if (_state != PlayerState.PREPARING) return@launch

                delay(100)

                try {
                    val actualIsPlaying = mediaPlayer?.isPlaying == true
                    val dur = mediaPlayer?.duration ?: 0

                    if (actualIsPlaying && dur > 0) {
                        // 有 duration 且正在播放 → 已准备就绪
                        log("AndroidMP", "⚡ PREPARING Monitor: 检测到正在播放！修正状态")
                        
                        val pos = mediaPlayer?.currentPosition ?: 0
                        log("AndroidMP", "PREPARING Monitor: duration=$dur, position=$pos")

                        // ✨ 检查是否需要自动恢复播放（Surface 切换后）
                        val shouldAutoResume = pendingSeekPosition >= 0
                        val savedPos = pendingSeekPosition
                        pendingSeekPosition = -1L  // 重置标记

                        log("AndroidMP", "PREPARING Monitor: autoResume=$shouldAutoResume" +
                                (if (shouldAutoResume) ", savedPosition=${formatTime(savedPos)}" else ""))

                        // 通知准备完成（如果还没通知过）
                        listener?.onPrepared(dur.toLong())

                        // 如果是 Surface 切换后的 reprepare，自动恢复播放位置
                        if (shouldAutoResume && savedPos >= 0) {
                            log("AndroidMP", "PREPARING Monitor [方案B]: 自动恢复播放 " +
                                    "(position=${formatTime(savedPos)})")
                            
                            // Monitor 检测到 isPlaying=true，只需 seekTo
                            seekTo(savedPos.toLong())
                            log("AndroidMP", "PREPARING Monitor [方案B]: 已恢复播放 " +
                                    "(${formatTime(savedPos)})")
                        }

                        // 更新为正确状态
                        _state = PlayerState.PLAYING
                        listener?.onStateChanged(PlayerState.PREPARING, PlayerState.PLAYING)

                        // 启动进度追踪
                        startProgressTracking()

                        return@launch  // 任务完成，退出监控
                    }
                } catch (_: Exception) {}
            }

            // 超时
            if (_state == PlayerState.PREPARING) {
                log("AndroidMP", "PREPARING Monitor: 超时，仍处于 PREPARING 状态")
            }
        }
    }

    /**
     * 停止 PREPARING 状态监控
     */
    private fun stopPreparingMonitor() {
        if (preparingMonitorJob != null) {
            log("AndroidMP", "stopping PREPARING state monitor")
            preparingMonitorJob?.cancel()
            preparingMonitorJob = null
        }
    }

    // ==================== 内部：进度追踪 ====================

    /**
     * 启动进度追踪协程
     *
     * 定时获取 MediaPlayer 的当前位置和总时长，通过监听器回调。
     */
    private fun startProgressTracking() {
        // 如果已经在运行且状态正确，不需要重启
        if (progressJob != null && progressJob!!.isActive && _state == PlayerState.PLAYING) {
            log("AndroidMP", "progress tracking already running")
            return
        }

        stopProgressTracking()
        log("AndroidMP", "starting progress tracking (state: $_state)")

        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { 
                    mediaPlayer?.currentPosition?.toLong() ?: 0L 
                } catch (_: Exception) { 
                    0L 
                }
                val dur = try { 
                    mediaPlayer?.duration?.toLong() ?: 0L 
                } catch (_: Exception) { 
                    0L 
                }
                listener?.onProgress(pos, dur)
                delay(progressIntervalMs.toLong())
            }
            log("AndroidMP", "progress tracking stopped (loop exited, state: $_state)")
        }
    }

    /**
     * 停止进度追踪协程
     */
    private fun stopProgressTracking() {
        if (progressJob != null) {
            log("AndroidMP", "stopping progress tracking")
            progressJob?.cancel()
            progressJob = null
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
    private fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.VIDEO_VIEW -> adjustVideoViewLayout()
            SurfaceType.SURFACE_VIEW -> adjustSurfaceViewLayout()
            SurfaceType.TEXTURE_VIEW -> adjustTextureViewLayout()
            else -> {}
        }
    }

    /**
     * 调整 VideoView 布局尺寸以适应视频宽高比
     */
    private fun adjustVideoViewLayout() {
        val vv = videoView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = vv.parent as? android.view.ViewGroup ?: return

        App.mainHandler.post {
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log("AndroidMP", "adjustVideoViewLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            val params = vv.layoutParams
            params.width = targetWidth
            params.height = targetHeight
            
            if (params is android.widget.FrameLayout.LayoutParams) {
                params.gravity = android.view.Gravity.CENTER
            }
            
            vv.layoutParams = params
        }
    }

    /**
     * 调整 SurfaceView 布局尺寸以适应视频宽高比
     */
    private fun adjustSurfaceViewLayout() {
        val sv = surfaceView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = sv.parent as? android.view.ViewGroup ?: return

        App.mainHandler.post {
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log("AndroidMP", "adjustSurfaceViewLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            val params = sv.layoutParams
            params.width = targetWidth
            params.height = targetHeight

            if (params is android.widget.FrameLayout.LayoutParams) {
                params.gravity = android.view.Gravity.CENTER
            }

            sv.layoutParams = params
        }
    }

    /**
     * 调整 TextureView 布局尺寸以适应视频宽高比
     */
    private fun adjustTextureViewLayout() {
        val tv = textureView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = tv.parent as? android.view.ViewGroup ?: return

        App.mainHandler.post {
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log("AndroidMP", "adjustTextureViewLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            val params = tv.layoutParams
            params.width = targetWidth
            params.height = targetHeight

            if (params is android.widget.FrameLayout.LayoutParams) {
                params.gravity = android.view.Gravity.CENTER
            }

            tv.layoutParams = params
        }
    }

    /**
     * 根据缩放模式计算目标尺寸
     *
     * @param videoW 视频宽度
     * @param videoH 视频高度
     * @param containerW 容器宽度
     * @param containerH 容器高度
     * @param mode 缩放模式
     * @return Pair<目标宽度, 目标高度>
     */
    private fun calculateTargetSize(
        videoW: Int, videoH: Int,
        containerW: Int, containerH: Int,
        mode: VideoScaleMode
    ): Pair<Int, Int> {
        if (videoW <= 0 || videoH <= 0 || containerW <= 0 || containerH <= 0) {
            return Pair(containerW, containerH)
        }

        val videoRatio = videoW.toFloat() / videoH.toFloat()
        val containerRatio = containerW.toFloat() / containerH.toFloat()

        return when (mode) {
            VideoScaleMode.FIT_CENTER -> {
                // 保持比例，完整显示（可能有黑边）
                if (videoRatio > containerRatio) {
                    // 视频更宽，以宽度为准
                    Pair(containerW, (containerW / videoRatio).toInt())
                } else {
                    // 视频更高，以高度为准
                    Pair((containerH * videoRatio).toInt(), containerH)
                }
            }
            VideoScaleMode.CROP_CENTER -> {
                // 保持比例，填满容器（可能裁剪边缘）
                if (videoRatio > containerRatio) {
                    // 视频更宽，以高度为准（裁剪左右）
                    Pair((containerH * videoRatio).toInt(), containerH)
                } else {
                    // 视频更高，以宽度为准（裁剪上下）
                    Pair(containerW, (containerW / videoRatio).toInt())
                }
            }
            VideoScaleMode.STRETCH -> {
                // 拉伸填满容器（可能变形）
                Pair(containerW, containerH)
            }
        }
    }
}
