package app.allever.android.sample.audiovideo.sdk

import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.android.IVideoPlayerListener
import app.allever.android.sample.audiovideo.android.LoopMode
import app.allever.android.sample.audiovideo.android.PlayerState
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.nativeclass.CacheConfig
import com.aliyun.player.nativeclass.MediaInfo
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.UrlSource
import com.aliyun.player.source.VidAuth
import com.aliyun.player.source.VidSts
import com.aliyun.player.videoview.AliDisplayView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 阿里云播放器视频封装（SDK 层）
 *
 * 职责：
 * - 封装 AliyunVodPlayer 完整生命周期（创建 → 准备 → 播放 → 暂停 → 停止 → 释放）
 * - 管理 AliyunVodPlayer 状态与 [PlayerState] 的映射
 * - 支持三种 Surface 绑定模式：AliDisplayView（推荐）/ SurfaceView / TextureView
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
 * // 示例 1：使用 URL 播放（简单场景）
 * val player = AliVideoPlayer()
 * player.attach(surfaceView)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 * })
 * player.setSource("https://example.com/video.mp4")
 *
 * // 示例 2：使用 VidAuth 播放（点播推荐）
 * val player = AliVideoPlayer()
 * player.attach(aliyunPlayerView)
 * player.setVidAuth(vid = "xxx", playAuth = "yyy")
 * // autoPlayOnPrepared 默认 true，会自动播放
 *
 * // 页面销毁时
 * player.release()
 * ```
 */
class AliVideoPlayer {

    // ==================== 内部组件 ====================

    /** 阿里云播放器实例 */
    private var aliPlayer: AliPlayer? = null

    /** 监听器回调 */
    private var listener: IVideoPlayerListener? = null

    // ==================== Surface 绑定（三种模式）====================

    /** AliDisplayView 绑定（推荐方式，自带控制层）*/
    private var playerView: AliDisplayView? = null

    /** SurfaceView 绑定（兼容方式）*/
    private var surfaceView: SurfaceView? = null

    /** TextureView 绑定（高级方式）*/
    private var textureView: TextureView? = null

    /** 当前绑定的 Surface 类型 */
    private enum class SurfaceType { NONE, PLAYER_VIEW, SURFACE_VIEW, TEXTURE_VIEW }
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
                log("AliVideoPlayer", "state: $old -> $value")
                field = value
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态 */
    val state get() = _state

    /** 是否正在播放 */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING

    /** 当前位置（毫秒）*/
    val currentPosition: Long
        get() = try { aliPlayer?.currentPosition ?: 0L } catch (_: Exception) { 0L }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try { aliPlayer?.duration ?: 0L } catch (_: Exception) { 0L }

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

    /** 变速倍率（0.5 ~ 5.0），默认 1.0 */
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 5.0f)
            applySpeed()
        }

    /** 音量（0.0 ~ 2.0），默认 1.0（>1 可能产生噪音，不推荐）*/
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 2f)
            aliPlayer?.volume = field
        }

    /**
     * SurfaceView/TextureView 缩放模式（默认 FIT_CENTER）
     *
     * 仅对 SurfaceView 和 TextureView 生效，
     * AliDisplayView 有自己的缩放控制。
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            // 如果已有视频尺寸，立即重新调整布局
            if (videoWidth > 0 && videoHeight > 0) {
                adjustSurfaceLayout()
            }
        }

    /**
     * 准备完成后是否自动开始播放，默认 true
     *
     * 设为 false 时，需在 [IVideoPlayerListener.onPrepared] 回调中手动调用 [play]
     */
    var autoPlayOnPrepared: Boolean = true

    // ==================== 内部状态 ====================

    /** 进度追踪协程 */
    private var progressJob: Job? = null

    /** 当前数据源类型 */
    private enum class SourceType { NONE, URL, VID_AUTH, VID_STS }
    private var currentSourceType: SourceType = SourceType.NONE

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
        val sourceType: SourceType,
        val url: String?,
        val vid: String?,
        val playAuth: String?,
        val accessKeyId: String?,
        val accessKeySecret: String?,
        val securityToken: String?,
        val region: String?
    )

    private var pendingPrepare: PendingPrepare? = null

    // ==================== Surface 绑定 API ====================

    /**
     * 绑定 AliDisplayView（推荐方式）
     *
     * AliDisplayView 是阿里云官方提供的视图组件，
     * 自带控制层（播放/暂停/进度条/全屏等），Surface 生命周期自动管理。
     *
     * @param playerView 外部创建的 AliDisplayView 实例
     */
    fun attach(playerView: AliDisplayView) {
        detach()

        this.playerView = playerView
        this.currentSurfaceType = SurfaceType.PLAYER_VIEW
        this.isSurfaceReady = true  // PlayerView 的 Surface 立即可用

        log("AliVideoPlayer", "attach AliDisplayView")

        initAliPlayer()
        bindToPlayerView()

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
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

        log("AliVideoPlayer", "attach SurfaceView (waiting for surface)")

        initAliPlayer()
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

        log("AliVideoPlayer", "attach TextureView (waiting for surface)")

        initAliPlayer()
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
     * 不释放内部 AliyunVodPlayer 和其他资源。
     */
    /**
     * 解绑当前 Surface（页面 onPause/onDestroyView 时调用）
     *
     * 根据阿里云官方文档：
     * - SurfaceView/TextureView：调用 setSurface(null) 解绑
     * - AliDisplayView：调用 setDisplayView(null) 解绑
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     * 不释放内部 AliyunVodPlayer 和其他资源。
     */
    fun detach() {
        when (currentSurfaceType) {
            SurfaceType.PLAYER_VIEW -> {
                // 阿里云文档：AliDisplayView 解绑方式
                aliPlayer?.setDisplayView(null)
                playerView = null
                log("AliVideoPlayer", "detach AliDisplayView (setDisplayView null)")
            }
            SurfaceType.SURFACE_VIEW -> {
                // 阿里云文档：SurfaceView 解绑方式
                aliPlayer?.setSurface(null)
                surfaceView?.holder?.removeCallback(surfaceHolderCallback)
                surfaceView = null
                isSurfaceReady = false
                log("AliVideoPlayer", "detach SurfaceView (setSurface null)")
            }
            SurfaceType.TEXTURE_VIEW -> {
                // 阿里云文档：TextureView 解绑方式（同 SurfaceView）
                aliPlayer?.setSurface(null)
                textureView?.surfaceTextureListener = null
                textureView = null
                isSurfaceReady = false
                log("AliVideoPlayer", "detach TextureView (setSurface null)")
            }
            SurfaceType.NONE -> {}
        }

        currentSurfaceType = SurfaceType.NONE
    }

    // ==================== 数据源设置 ====================

    /**
     * 设置数据源并开始准备（不自动播放）
     *
     * 支持的数据源类型：
     * - HTTP/HTTPS URL：在线视频
     * - file:// 路径：本地文件
     *
     * 准备完成后回调 [IVideoPlayerListener.onPrepared]，此时需调用 [play] 开始播放（或设置 autoPlayOnPrepared=true）。
     *
     * @param url 数据源地址
     */
    fun setSource(url: String) {
        doSetSource(SourceType.URL, url = url)
    }

    /**
     * 设置点播 VidAuth 数据源（阿里云推荐方式）
     *
     * 使用视频 ID 和播放凭证进行播放，适用于阿里云点播服务。
     * 播放凭证通过服务端 SDK 调用 GetVideoPlayAuth 接口获取。
     *
     * @param vid 视频ID（可在控制台或 SearchMedia 接口获取）
     * @param playAuth 播放凭证（调用 GetVideoPlayAuth 接口获取）
     */
    fun setVidAuth(vid: String, playAuth: String) {
        doSetSource(SourceType.VID_AUTH, vid = vid, playAuth = playAuth)
    }

    /**
     * 设置点播 VidSts 数据源（STS 临时凭证方式）
     *
     * 使用 STS 临时安全令牌进行播放，适用于需要临时授权的场景。
     *
     * @param vid 视频ID
     * @param accessKeyId AccessKey ID
     * @param accessKeySecret AccessKey Secret
     * @param securityToken 安全令牌
     * @param region 地域标识（如 cn-shanghai），参见点播地域标识文档
     */
    fun setVidSts(
        vid: String,
        accessKeyId: String,
        accessKeySecret: String,
        securityToken: String,
        region: String = "cn-shanghai"
    ) {
        doSetSource(
            sourceType = SourceType.VID_STS,
            vid = vid,
            accessKeyId = accessKeyId,
            accessKeySecret = accessKeySecret,
            securityToken = securityToken,
            region = region
        )
    }

    /**
     * 执行实际的 setSource 操作
     */
    private fun doSetSource(
        sourceType: SourceType,
        url: String? = null,
        vid: String? = null,
        playAuth: String? = null,
        accessKeyId: String? = null,
        accessKeySecret: String? = null,
        securityToken: String? = null,
        region: String? = null
    ) {
        if (_state == PlayerState.RELEASED) return

        // 停止当前的进度追踪（切换数据源前必须清理）
        stopProgressTracking()

        currentSourceType = sourceType
        retryLeft = retryCount

        // 如果 Surface 未就绪，缓存待执行的 prepare
        if (!isSurfaceReady && currentSurfaceType != SurfaceType.NONE) {
            log("AliVideoPlayer", "Surface not ready, caching prepare request")
            pendingPrepare = PendingPrepare(
                sourceType = sourceType,
                url = url,
                vid = vid,
                playAuth = playAuth,
                accessKeyId = accessKeyId,
                accessKeySecret = accessKeySecret,
                securityToken = securityToken,
                region = region
            )
            _state = PlayerState.PREPARING
            return
        }

        doPrepareInternal(
            sourceType = sourceType,
            url = url,
            vid = vid,
            playAuth = playAuth,
            accessKeyId = accessKeyId,
            accessKeySecret = accessKeySecret,
            securityToken = securityToken,
            region = region
        )
    }

    /**
     * 执行缓存的 prepare 操作
     */
    private fun executePendingPrepare() {
        pendingPrepare?.let { pending ->
            log("AliVideoPlayer", "executing pending prepare: type=${pending.sourceType}")
            pendingPrepare = null
            doPrepareInternal(
                sourceType = pending.sourceType,
                url = pending.url,
                vid = pending.vid,
                playAuth = pending.playAuth,
                accessKeyId = pending.accessKeyId,
                accessKeySecret = pending.accessKeySecret,
                securityToken = pending.securityToken,
                region = pending.region
            )
        }
    }

    // ==================== 播放控制 ====================

    /**
     * 开始播放 或 从暂停恢复播放
     *
     * - PREPARED/COMPLETED → 开始播放
     * - PAUSED → 恢复播放（阿里云播放器使用 start 恢复）
     * - 其他状态 → 忽略
     */
    fun play() {
        when (_state) {
            PlayerState.PREPARED, PlayerState.COMPLETED -> {
                aliPlayer?.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log("AliVideoPlayer", "play() -> PLAYING")
            }
            PlayerState.PAUSED -> {
                aliPlayer?.start()  // 阿里云播放器：恢复播放也用 start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log("AliVideoPlayer", "play() -> PLAYING (from PAUSED)")
            }
            else -> {
                log("AliVideoPlayer", "play() ignored (current state: $_state)")
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        if (_state == PlayerState.PLAYING) {
            aliPlayer?.pause()
            _state = PlayerState.PAUSED
            stopProgressTracking()
            log("AliVideoPlayer", "pause() -> PAUSED")
        }
    }

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    fun stop() {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return

        stopProgressTracking()

        aliPlayer?.stop()
        _state = PlayerState.STOPPED
        log("AliVideoPlayer", "stop() -> STOPPED")
    }

    /**
     * 跳转到指定位置
     *
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            isSeeking = true  // 标记正在 seek，防止误停进度追踪
            aliPlayer?.seekTo(positionMs)
            // 延迟重置标志并确保进度追踪正常运行（seek 是异步操作）
            App.mainHandler.postDelayed({
                isSeeking = false
                // 确保 seek 完成后进度追踪仍在运行
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    log("AliVideoPlayer", "restart progress tracking after seek")
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
            log("AliVideoPlayer", "seekTo error: ${e.message}")
            isSeeking = false
        }
    }

    /**
     * 设置下次播放的起播时间（需在 prepare 前调用）
     *
     * 根据阿里云官方文档：
     * - 设置下次播放器 prepare 时的起播时间（毫秒）
     * - 仅对紧接着的一次 prepare 生效
     * - 调用 prepare 后，该值会被内部自动清零
     *
     * @param timeMs 起播位置（毫秒）
     * @param seekMode seek 模式（精准或非精准）
     */
    fun setStartTime(timeMs: Long, seekMode: IPlayer.SeekMode = IPlayer.SeekMode.Accurate) {
        aliPlayer?.setStartTime(timeMs, seekMode)
        log("AliVideoPlayer", "setStartTime: ${timeMs}ms, mode=$seekMode")
    }

    /**
     * 重置到初始状态（保留资源，可重新 setSource）
     */
    fun reset() {
        stop()
        aliPlayer?.reset()
        _state = PlayerState.IDLE
        log("AliVideoPlayer", "reset() -> IDLE")
    }

    // ==================== 生命周期 ====================

    /**
     * 释放所有资源（同步方式）
     *
     * 根据阿里云官方文档：
     * - 同步销毁：内部会自动调用 stop 接口
     * - 需等待播放器资源完全释放后才返回
     * - 如果对界面响应速度有要求，建议使用 [releaseAsync]
     *
     * 调用后不可再使用此实例
     */
    fun release() {
        detach()
        releaseAliPlayer()
        pendingPrepare = null
        _state = PlayerState.RELEASED
        log("AliVideoPlayer", "release() -> RELEASED (sync)")
    }

    /**
     * 异步释放所有资源
     *
     * 根据阿里云官方文档：
     * - 异步销毁：内部会自动调用 stop 接口
     * - 不等待播放器资源完全释放就返回，响应速度更快
     * - 注意事项：
     *   1. 避免在异步销毁过程中对播放器对象执行任何其他操作
     *   2. 无需在调用异步销毁之前手动停止播放器
     *
     * 调用后不可再使用此实例
     */
    fun releaseAsync() {
        detach()
        stopProgressTracking()
        aliPlayer?.releaseAsync()  // 异步销毁
        aliPlayer = null
        pendingPrepare = null
        _state = PlayerState.RELEASED
        log("AliVideoPlayer", "releaseAsync() -> RELEASED (async)")
    }

    // ==================== 监听器设置 ====================

    /**
     * 设置播放事件监听器
     */
    fun setListener(listener: IVideoPlayerListener?) {
        this.listener = listener
    }

    // ==================== 阿里云特有功能 ====================

    /**
     * 设置是否使用硬件解码（默认开启）
     *
     * 硬件解码性能更好，但兼容性可能不如软解。
     * 如遇花屏/绿屏问题，可尝试关闭硬解使用软件解码。
     *
     * @param enabled true 使用硬解（默认），false 使用软解
     */
    fun useHardwareDecoder(enabled: Boolean) {
        aliPlayer?.enableHardwareDecoder(enabled)
        log("AliVideoPlayer", "hardware decoder: $enabled")
    }

    /**
     * 启用本地缓存功能（边播边存）
     *
     * 缓存后的视频下次播放可直接从本地加载，节省流量。
     *
     * @param cacheConfig 缓存配置（包含缓存目录、最大大小等）
     */
    fun enableCache(cacheConfig: CacheConfig?) {
        aliPlayer?.setCacheConfig(cacheConfig)
        log("AliVideoPlayer", "cache config set: ${cacheConfig != null}")
    }

    /**
     * 切换画质（多码率切换）
     *
     * 仅在视频支持多码率时有效，可通过 [getQualityTracks] 获取可用画质列表。
     *
     * @param trackInfo 要切换到的画质轨道信息
     */
    fun switchQuality(trackInfo: TrackInfo) {
        aliPlayer?.selectTrack(trackInfo.index)
        log("AliVideoPlayer", "switch quality to track: ${trackInfo.index}")
    }

    /**
     * 获取当前视频的所有画质轨道信息
     *
     * 返回值包含不同码率的清晰度列表，用于 UI 展示和切换。
     *
     * @return 画质轨道列表，未准备完成时返回空列表
     */
    fun getQualityTracks(): List<TrackInfo> {
        return try {
            // 使用反射获取 trackInfos 并过滤视频轨道
            val mediaInfo = aliPlayer?.mediaInfo ?: return emptyList()
            val trackInfos = try {
                val field = mediaInfo.javaClass.getDeclaredField("trackInfos")
                field.isAccessible = true
                field.get(mediaInfo) as? List<*> ?: emptyList<TrackInfo>()
            } catch (_: Exception) { emptyList<TrackInfo>() }

            trackInfos.filterIsInstance<TrackInfo>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 获取当前媒体信息
     *
     * 包含视频时长、轨道信息等详细数据。
     *
     * @return 媒体信息对象，未准备好时返回 null
     */
    fun getMediaInfo(): MediaInfo? {
        return try { aliPlayer?.mediaInfo } catch (_: Exception) { null }
    }

    /**
     * 设置镜像显示（水平翻转）
     *
     * @param mirror true 开启水平镜像，false 关闭（默认）
     */
    fun setMirror(mirror: Boolean) {
        val mode = if (mirror) IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL else IPlayer.MirrorMode.MIRROR_MODE_NONE
        aliPlayer?.setMirrorMode(mode)
    }

    /**
     * 设置垂直镜像
     *
     * @param verticalMirror true 开启垂直镜像，false 关闭
     */
    fun setVerticalMirror(verticalMirror: Boolean) {
        val mode = if (verticalMirror) IPlayer.MirrorMode.MIRROR_MODE_VERTICAL else IPlayer.MirrorMode.MIRROR_MODE_NONE
        aliPlayer?.setMirrorMode(mode)
    }

    /**
     * 设置旋转角度
     *
     * 根据阿里云官方文档，支持的角度：
     * - ROTATE_0: 0 度（默认）
     * - ROTATE_90: 90 度
     * - ROTATE_180: 180 度
     * - ROTATE_270: 270 度
     *
     * @param rotation 旋转角度枚举值
     */
    fun setRotation(rotation: IPlayer.RotateMode) {
        aliPlayer?.rotateMode = rotation
    }

    /**
     * 截取当前帧为 Bitmap
     *
     * @return 当前帧的 Bitmap，失败返回 null
     */
    fun captureFrame(): android.graphics.Bitmap? {
        return try {
            // 使用反射调用 captureFrame 方法
            val method = aliPlayer?.javaClass?.getMethod("captureFrame", Int::class.javaPrimitiveType)
            method?.invoke(aliPlayer, 0) as? android.graphics.Bitmap
        } catch (_: Exception) { null }
    }

    /**
     * 启用 TraceId 追踪（用于性能监控和问题排查）
     *
     * @param enable true 启用，false 关闭
     */
    fun enableTraceId(enable: Boolean) {
        try {
            // 使用反射调用 enableTraceId 方法
            val method = aliPlayer?.javaClass?.getMethod("enableTraceId", Boolean::class.javaPrimitiveType)
            method?.invoke(aliPlayer, enable)
        } catch (_: Exception) {
            log("AliVideoPlayer", "enableTraceId not supported")
        }
    }

    /**
     * 获取当前缓冲百分比（0-100）
     *
     * @return 缓冲百分比
     */
    fun getBufferedPercent(): Int {
        return try {
            // 使用反射获取 bufferedPercentage 属性
            val field = aliPlayer?.javaClass?.getDeclaredField("bufferedPercentage")
            field?.isAccessible = true
            (field?.get(aliPlayer) as? Int) ?: 0
        } catch (_: Exception) { 0 }
    }


    /**
     * 获取当前渲染的帧率（FPS）
     *
     * @return 渲染帧率，失败返回 -1f
     */
    fun getRenderFPS(): Float {
        return try {
            (aliPlayer?.getOption(IPlayer.Option.RenderFPS) as? Float) ?: -1f
        } catch (_: Exception) { -1f }
    }

    /**
     * 获取当前播放的视频码率（bps）
     *
     * @return 视频码率，失败返回 -1f
     */
    fun getVideoBitrate(): Float {
        return try {
            (aliPlayer?.getOption(IPlayer.Option.VideoBitrate) as? Float) ?: -1f
        } catch (_: Exception) { -1f }
    }

    /**
     * 获取当前播放的音频码率（bps）
     *
     * @return 音频码率，失败返回 -1f
     */
    fun getAudioBitrate(): Float {
        return try {
            (aliPlayer?.getOption(IPlayer.Option.AudioBitrate) as? Float) ?: -1f
        } catch (_: Exception) { -1f }
    }

    /**
     * 获取当前的网络下行码率（bps）
     *
     * @return 网络下行码率，失败返回 -1f
     */
    fun getDownloadBitrate(): Float {
        return try {
            (aliPlayer?.getOption(IPlayer.Option.DownloadBitrate) as? Float) ?: -1f
        } catch (_: Exception) { -1f }
    }

    /**
     * 获取实际播放时长（不包含暂停、卡顿时间）
     *
     * @return 实际播放时长（毫秒）
     */
    fun getPlayedDuration(): Long {
        return try { aliPlayer?.playedDuration ?: 0L } catch (_: Exception) { 0L }
    }

    /**
     * 设置静音
     *
     * @param mute true 静音，false 取消静音
     */
    fun setMute(mute: Boolean) {
        aliPlayer?.isMute = mute
        log("AliVideoPlayer", "setMute: $mute")
    }

    /**
     * 获取是否静音状态
     *
     * @return 是否静音
     */
    fun isMuted(): Boolean {
        return try { aliPlayer?.isMute == true } catch (_: Exception) { false }
    }

    /**
     * 设置显示缩放模式（阿里云原生模式）
     *
     * 根据阿里云官方文档：
     * - SCALE_ASPECT_FIT：宽高比适应（默认，可能有黑边）
     * - SCALE_ASPECT_FILL：宽高比填充（可能裁剪边缘）
     * - SCALE_TO_FILL：拉伸填充（可能变形）
     *
     * 注意：此方法仅对 AliyunPlayerView 生效，
     * SurfaceView/TextureView 使用 [videoScaleMode] 属性控制。
     *
     * @param scaleMode 阿里云缩放模式枚举
     */
    fun setScaleMode(scaleMode: IPlayer.ScaleMode) {
        aliPlayer?.setScaleMode(scaleMode)
        log("AliVideoPlayer", "setScaleMode: $scaleMode")
    }

    /**
     * 开启/关闭循环播放
     *
     * @param loop true 开启循环，false 关闭循环
     */
    fun setLoop(loop: Boolean) {
        aliPlayer?.isLoop = loop
        log("AliVideoPlayer", "setLoop: $loop")
    }

    // ==================== 私有方法：初始化 ====================

    /**
     * 初始化 AliyunVodPlayer 实例
     */
    private fun initAliPlayer() {
        if (aliPlayer != null) return

        val context = App.context
        aliPlayer = AliPlayerFactory.createAliPlayer(context).apply {
            // 设置监听器（按阿里云官方文档推荐）
            setOnPreparedListener(mOnPreparedListener)
            setOnCompletionListener(mOnCompletionListener)
            setOnErrorListener(mOnErrorListener)
            setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
//            setOnBufferingUpdateListener(mOnBufferingUpdateListener)
            setOnInfoListener(mOnInfoListener)  // 重要：包含进度、缓冲、首帧渲染等
            setOnLoadingStatusListener(mOnLoadingStatusListener)  // 重要：加载状态
            setOnStateChangedListener(mOnStateChangedListener)  // 重要：播放器状态变化

            // 应用初始配置
            applyLoopMode()
            applySpeed()
            volume@ this@AliVideoPlayer.volume.let { vol ->
                if (vol != 1.0f) this.volume = vol
            }
        }

        log("AliVideoPlayer", "AliyunVodPlayer initialized")
    }

    /**
     * 将 AliyunVodPlayer 绑定到 AliDisplayView
     */
    private fun bindToPlayerView() {
        aliPlayer?.setDisplayView(playerView)
        log("AliVideoPlayer", "bound to AliDisplayView")
    }

    /**
     * 执行实际的 prepare 操作
     *
     * 根据阿里云官方文档：
     * - 使用 setDataSource() 设置数据源（不是 prepareAsync）
     * - 然后调用 prepare() 开始准备
     */
    private fun doPrepareInternal(
        sourceType: SourceType,
        url: String? = null,
        vid: String? = null,
        playAuth: String? = null,
        accessKeyId: String? = null,
        accessKeySecret: String? = null,
        securityToken: String? = null,
        region: String? = null
    ) {
        _state = PlayerState.PREPARING

        when (sourceType) {
            SourceType.URL -> {
                // 阿里云文档：UrlSource 方式
                val urlSource = UrlSource().apply {
                    uri = url  // 播放地址（必选）
                }
                aliPlayer?.setDataSource(urlSource)
                log("AliVideoPlayer", "setDataSource with UrlSource: $url")
            }
            SourceType.VID_AUTH -> {
                // 阿里云文档：VidAuth 方式（推荐）
                val vidAuth = VidAuth().apply {
                    this.vid = vid  // 视频ID（必选）
                    this.playAuth = playAuth  // 播放凭证（必选）
                    // region 在 5.5.5.0+ 版本已弃用，无需设置
                }
                aliPlayer?.setDataSource(vidAuth)
                log("AliVideoPlayer", "setDataSource with VidAuth: vid=$vid")
            }
            SourceType.VID_STS -> {
                // 阿里云文档：VidSts 方式
                val vidSts = VidSts().apply {
                    this.vid = vid  // 视频ID（必选）
                    this.accessKeyId = accessKeyId  // STS AccessKey ID（必选）
                    this.accessKeySecret = accessKeySecret  // STS AccessKey Secret（必选）
                    this.securityToken = securityToken  // STS 安全令牌（必选）
                    this.region = region ?: "cn-shanghai"  // 接入地域（必选，默认 cn-shanghai）
                }
                aliPlayer?.setDataSource(vidSts)
                log("AliVideoPlayer", "setDataSource with VidSts: vid=$vid, region=$region")
            }
            SourceType.NONE -> {}
        }

        // 设置完 DataSource 后，调用 prepare() 开始准备
        aliPlayer?.prepare()
    }

    // ==================== Surface 回调处理 ====================

    /**
     * Surface 就绪回调（统一入口）
     */
    private fun onSurfaceReady(surface: Surface) {
        isSurfaceReady = true
        aliPlayer?.setSurface(surface)
        log("AliVideoPlayer", "surface ready, binding to player")

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
        }
    }

    /**
     * 设置 SurfaceView 的 SurfaceHolder 回调
     */
    private fun setupSurfaceViewCallback() {
        surfaceView?.holder?.addCallback(surfaceHolderCallback)
    }

    /**
     * SurfaceView 的 SurfaceHolder 回调
     */
    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            log("AliVideoPlayer", "surfaceCreated")
            onSurfaceReady(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log("AliVideoPlayer", "surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log("AliVideoPlayer", "surfaceDestroyed")
            isSurfaceReady = false
        }
    }

    /**
     * 设置 TextureView 的 SurfaceTextureListener 回调
     */
    private fun setupTextureViewCallback() {
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                log("AliVideoPlayer", "onSurfaceTextureAvailable: ${width}x${height}")
                onSurfaceReady(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                log("AliVideoPlayer", "onSurfaceTextureSizeChanged: ${width}x${height}")
                // 尝试重新调整布局
                if (videoWidth > 0 && videoHeight > 0) {
                    adjustSurfaceLayout()
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log("AliVideoPlayer", "onSurfaceTextureDestroyed")
                isSurfaceReady = false
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    // ==================== 阿里云监听器实现 ====================

    /**
     * 准备完成监听器
     */
    private val mOnPreparedListener = IPlayer.OnPreparedListener {
        App.mainHandler.post {
            log("AliVideoPlayer", "onPrepared (current state: $_state)")

            if (_state == PlayerState.PREPARING || _state == PlayerState.IDLE) {
                val dur = duration
                _state = PlayerState.PREPARED
                listener?.onPrepared(dur)

                // 自动播放
                if (autoPlayOnPrepared) {
                    play()
                }
            }
        }
    }

    /**
     * 播放完成监听器
     */
    private val mOnCompletionListener = IPlayer.OnCompletionListener {
        App.mainHandler.post {
            log("AliVideoPlayer", "onCompletion")

            stopProgressTracking()

            when (loopMode) {
                LoopMode.SINGLE -> {
                    // 单曲循环：从头开始播放
                    listener?.onLoopRestart()
                    seekTo(0)
                    play()
                }
                LoopMode.ALL -> {
                    // 列表循环：通知上层切换下一个
                    _state = PlayerState.COMPLETED
                    listener?.onComplete()
                }
                LoopMode.NONE -> {
                    _state = PlayerState.COMPLETED
                    listener?.onComplete()
                }
            }
        }
    }

    /**
     * 错误监听器
     */
    private val mOnErrorListener = IPlayer.OnErrorListener { error ->
        App.mainHandler.post {
            log("AliVideoPlayer", "onError: code=${error.code}, msg=${error.msg}")

            if (_state == PlayerState.PREPARING) {
                // 准备阶段出错，尝试重试
                handlePrepareError(error)
            } else {
                // 播放阶段出错
                _state = PlayerState.ERROR
                val consumed = listener?.onError(error.code.value, error.code.value) ?: false
                consumed
            }
        }
        true
    }

    /**
     * 视频尺寸变化监听器
     */
    private val mOnVideoSizeChangedListener = IPlayer.OnVideoSizeChangedListener { width, height ->
        App.mainHandler.post {
            if (width > 0 && height > 0) {
                // 保存视频原始尺寸
                this@AliVideoPlayer.videoWidth = width
                this@AliVideoPlayer.videoHeight = height

                listener?.onVideoSizeChanged(width, height)
                log("AliVideoPlayer", "onVideoSizeChanged: ${width}x${height}")

                // 对 SurfaceView 和 TextureView 进行画面自适应
                if (currentSurfaceType == SurfaceType.SURFACE_VIEW ||
                    currentSurfaceType == SurfaceType.TEXTURE_VIEW) {
                    adjustSurfaceLayout()
                }
            }
        }
    }

    /**
     * 缓冲更新监听器
     */
//    private val mOnBufferingUpdateListener = IPlayer.OnBufferingUpdateListener { percent ->
//        App.mainHandler.post {
//            listener?.onBufferingUpdate(percent)
//        }
//    }

    /**
     * 信息监听器（首帧渲染、缓冲等事件）
     *
     * 根据阿里云官方文档：
     * - onInfo 回调参数为 InfoBean 对象
     * - InfoBean 包含：code (InfoCode)、msg (String)、value (long)
     * - 常用 InfoCode：
     *   - CurrentPosition：当前播放进度
     *   - BufferedPosition：当前缓冲位置
     *   - BufferingStart/End：缓冲开始/结束
     *   - FirstFrameRendered：首帧渲染
     *   - LoadingStart/End：加载开始/结束
     *   - LoopingStart：循环开始
     */
    private val mOnInfoListener = IPlayer.OnInfoListener { infoBean ->
        App.mainHandler.post {
            val code = infoBean.code  // InfoCode 枚举
            val msg = infoBean.extraMsg  // 信息内容
            val value = infoBean.extraValue  // 信息值

            log("AliVideoPlayer", "onInfo: code=$code, msg=$msg, value=$value")

            when (code) {
                InfoCode.BufferedPosition -> {
                    // 缓冲位置更新
                    val percent = if (duration > 0) ((value.toFloat() / duration) * 100).toInt() else 0
                    listener?.onBufferingUpdate(percent)
                }
                is InfoCode -> {
                    // 首帧渲染或其他事件
                    listener?.onFirstFrameRendered()
                    // 首帧渲染 = 视频真正开始播放
                    if (_state == PlayerState.PREPARING || _state == PlayerState.PREPARED) {
                        val dur = duration
                        if (_state == PlayerState.PREPARING) {
                            listener?.onPrepared(dur)
                        }
                        _state = PlayerState.PLAYING
                        startProgressTracking()
                        log("AliVideoPlayer", "⚡ onInfo: 首帧渲染！修正为 PLAYING")
                    }
                }
                else -> {
                    log("AliVideoPlayer", "⚠️ onInfo: 未处理的 code=$code, msg=$msg")
                }
            }

            // 透传给外部监听器（转换为 int）
            val infoCodeInt = when (code) {
                is InfoCode -> code.ordinal
                else -> 0
            }
            listener?.onInfo(infoCodeInt, value.toInt())
        }
        true
    }

    /**
     * 加载状态监听器（加载中/加载结束）
     */
    private val mOnLoadingStatusListener = object : IPlayer.OnLoadingStatusListener {
        override fun onLoadingBegin() {
            App.mainHandler.post {
                log("AliVideoPlayer", "onLoadingBegin")
                listener?.onBufferingStart()
            }
        }

        override fun onLoadingProgress(var1: Int, var2: Float) {}

        override fun onLoadingEnd() {
            App.mainHandler.post {
                log("AliVideoPlayer", "onLoadingEnd")
                listener?.onBufferingEnd()
            }
        }
    }

    /**
     * 播放器状态变化监听器（阿里云核心监听器）
     *
     * 根据阿里云官方文档，播放器状态值：
     * - 0: idle（空闲）
     * - 1: initialized（已初始化）
     * - 2: prepared（已准备）
     * - 3: started（已开始）
     * - 4: paused（已暂停）
     * - 5: stopped（已停止）
     * - 6: completion（已完成）
     * - 7: error（错误）
     *
     * 此监听器用于同步内部 PlayerState 与阿里云播放器的实际状态。
     */
    private val mOnStateChangedListener = IPlayer.OnStateChangedListener { newState ->
        App.mainHandler.post {
            log("AliVideoPlayer", "onStateChanged: nativeState=$newState")

            // 根据阿里云播放器状态值进行映射（使用 int 类型比较）
            // 阿里云播放器状态：0=idle, 1=initialized, 2=prepared, 3=started, 4=paused, 5=stopped, 6=completion, 7=error
            when (newState) {
                0 -> { // idle
                    if (_state != PlayerState.IDLE && _state != PlayerState.RELEASED) {
                        _state = PlayerState.IDLE
                    }
                }
                1 -> { // initialized
                    log("AliVideoPlayer", "native state: initialized")
                }
                2 -> { // prepared
                    if (_state == PlayerState.PREPARING || _state == PlayerState.IDLE) {
                        _state = PlayerState.PREPARED
                        listener?.onPrepared(duration)
                    }
                }
                3 -> { // started
                    if (_state == PlayerState.PREPARED || _state == PlayerState.PAUSED) {
                        _state = PlayerState.PLAYING
                        startProgressTracking()
                    }
                }
                4 -> { // paused
                    if (_state == PlayerState.PLAYING) {
                        _state = PlayerState.PAUSED
                        stopProgressTracking()
                    }
                }
                5 -> { // stopped
                    stopProgressTracking()
                    if (_state != PlayerState.STOPPED && _state != PlayerState.IDLE && _state != PlayerState.RELEASED) {
                        _state = PlayerState.STOPPED
                    }
                }
                6 -> { // completion
                    stopProgressTracking()
                    if (_state != PlayerState.COMPLETED) {
                        _state = PlayerState.COMPLETED
                        listener?.onComplete()
                    }
                }
                7 -> { // error
                    stopProgressTracking()
                    if (_state != PlayerState.ERROR) {
                        _state = PlayerState.ERROR
                    }
                }
                else -> {
                    log("AliVideoPlayer", "⚠️ unknown native state: $newState")
                }
            }
        }
    }

    // ==================== 内部：配置应用方法 ====================

    /**
     * 应用循环模式
     */
    private fun applyLoopMode() {
        val player = aliPlayer ?: return
        when (loopMode) {
            LoopMode.SINGLE -> {
                player.isLoop = true
                log("AliVideoPlayer", "loop mode: SINGLE (isLoop=true)")
            }
            LoopMode.ALL, LoopMode.NONE -> {
                player.isLoop = false
                log("AliVideoPlayer", "loop mode: $loopMode (isLoop=false)")
            }
        }
    }

    /**
     * 应用变速
     */
    private fun applySpeed() {
        aliPlayer?.speed = speed
        log("AliVideoPlayer", "speed applied: $speed")
    }

    // ==================== 内部：错误处理 ====================

    /**
     * 处理准备阶段错误（支持重试）
     */
    private fun handlePrepareError(error: ErrorInfo) {
        if (retryLeft > 0) {
            retryLeft--
            log("AliVideoPlayer", "retry prepare ($retryLeft left), error: ${error.msg}")
            App.mainHandler.postDelayed({
                if (_state == PlayerState.PREPARING) {
                    pendingPrepare?.let {
                        doPrepareInternal(
                            sourceType = it.sourceType,
                            url = it.url,
                            vid = it.vid,
                            playAuth = it.playAuth,
                            accessKeyId = it.accessKeyId,
                            accessKeySecret = it.accessKeySecret,
                            securityToken = it.securityToken,
                            region = it.region
                        )
                    }
                }
            }, 1000)  // 1 秒后重试
        } else {
            log("AliVideoPlayer", "no more retries, error: ${error.msg}")
            _state = PlayerState.ERROR
            listener?.onError(error.code.value, -1)
        }
    }

    // ==================== 内部：生命周期管理 ====================

    /**
     * 释放 AliyunVodPlayer 实例
     */
    private fun releaseAliPlayer() {
        stopProgressTracking()
        aliPlayer?.release()
        aliPlayer = null
        log("AliVideoPlayer", "AliyunVodPlayer released")
    }

    // ==================== 内部：进度追踪 ====================

    /**
     * 启动进度追踪协程
     *
     * 定时获取 AliyunVodPlayer 的当前位置和总时长，通过监听器回调。
     */
    private fun startProgressTracking() {
        // 如果已经在运行且状态正确，不需要重启
        if (progressJob != null && progressJob!!.isActive && _state == PlayerState.PLAYING) {
            log("AliVideoPlayer", "progress tracking already running")
            return
        }

        stopProgressTracking()
        log("AliVideoPlayer", "starting progress tracking (state: $_state)")

        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { aliPlayer?.currentPosition ?: 0L } catch (_: Exception) { 0L }
                val dur = try { aliPlayer?.duration ?: 0L } catch (_: Exception) { 0L }
                listener?.onProgress(pos, dur)
                delay(progressIntervalMs.toLong())
            }
            log("AliVideoPlayer", "progress tracking stopped (loop exited, state: $_state)")
        }
    }

    /**
     * 停止进度追踪协程
     */
    private fun stopProgressTracking() {
        if (progressJob != null) {
            log("AliVideoPlayer", "stopping progress tracking")
            progressJob?.cancel()
            progressJob = null
        }
    }

    // ==================== 内部：SurfaceView/TextureView 画面自适应 ====================

    /**
     * 根据当前缩放模式调整 SurfaceView 或 TextureView 的布局尺寸
     *
     * 调用时机：
     * - onVideoSizeChanged 回调中（获取到视频尺寸后）
     * - videoScaleMode 属性改变时（切换缩放模式）
     *
     * 仅对 SurfaceView 和 TextureView 生效，AliDisplayView 有自己的缩放控制。
     */
    private fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.SURFACE_VIEW -> adjustSurfaceViewLayout()
            SurfaceType.TEXTURE_VIEW -> adjustTextureViewLayout()
            else -> {}  // PlayerView 不需要手动调整
        }
    }

    /**
     * 调整 SurfaceView 布局尺寸以适应视频宽高比
     *
     * 算法根据 [videoScaleMode] 选择不同的适配策略：
     * - FIT_CENTER：保持比例，完整显示视频（可能有黑边）
     * - CROP_CENTER：保持比例，填满容器（可能裁剪边缘）
     * - STRETCH：拉伸填满容器（可能变形）
     */
    private fun adjustSurfaceViewLayout() {
        val sv = surfaceView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = sv.parent as? android.view.ViewGroup ?: return

        // 使用 post 确保 View 已完成布局测量
        App.mainHandler.post {
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log("AliVideoPlayer", "adjustSurfaceViewLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            // 更新 SurfaceView LayoutParams
            val params = sv.layoutParams
            params.width = targetWidth
            params.height = targetHeight

            // 居中显示（通过 gravity）
            if (params is android.widget.FrameLayout.LayoutParams) {
                params.gravity = android.view.Gravity.CENTER
            }

            sv.layoutParams = params
        }
    }

    /**
     * 调整 TextureView 布局尺寸以适应视频宽高比
     *
     * 与 SurfaceView 类似，但 TextureView 支持矩阵变换，
     * 此处使用 LayoutParams 方式实现基础版自适应。
     */
    private fun adjustTextureViewLayout() {
        val tv = textureView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = tv.parent as? android.view.ViewGroup ?: return

        // 使用 post 确保 View 已完成布局测量
        App.mainHandler.post {
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log("AliVideoPlayer", "adjustTextureViewLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            // 更新 TextureView LayoutParams
            val params = tv.layoutParams
            params.width = targetWidth
            params.height = targetHeight

            // 居中显示（通过 gravity）
            if (params is android.widget.FrameLayout.LayoutParams) {
                params.gravity = android.view.Gravity.CENTER
            }

            tv.layoutParams = params
        }
    }

    /**
     * 根据缩放模式计算目标尺寸
     *
     * @param videoWidth 视频原始宽度
     * @param videoHeight 视频原始高度
     * @param containerWidth 容器宽度
     * @param containerHeight 容器高度
     * @param scaleMode 缩放模式
     * @return Pair<目标宽度, 目标高度>
     */
    private fun calculateTargetSize(
        videoWidth: Int,
        videoHeight: Int,
        containerWidth: Int,
        containerHeight: Int,
        scaleMode: VideoScaleMode
    ): Pair<Int, Int> {
        val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
        val containerAspect = containerWidth.toFloat() / containerHeight.toFloat()

        return when (scaleMode) {
            VideoScaleMode.FIT_CENTER -> {
                // 保持比例，完整显示视频（可能有黑边）
                if (videoAspect > containerAspect) {
                    // 视频更宽 → 以宽度为准，高度按比例缩放
                    Pair(containerWidth, (containerWidth / videoAspect).toInt())
                } else {
                    // 视频更高 → 以高度为准，宽度按比例缩放
                    Pair((containerHeight * videoAspect).toInt(), containerHeight)
                }
            }
            VideoScaleMode.CROP_CENTER -> {
                // 保持比例，填满容器（可能裁剪边缘）
                if (videoAspect > containerAspect) {
                    // 视频更宽 → 以高度为准，宽度超出部分会被裁剪
                    Pair((containerHeight * videoAspect).toInt(), containerHeight)
                } else {
                    // 视频更高 → 以宽度为准，高度超出部分会被裁剪
                    Pair(containerWidth, (containerWidth / videoAspect).toInt())
                }
            }
            VideoScaleMode.STRETCH -> {
                // 拉伸填满容器（可能变形）
                Pair(containerWidth, containerHeight)
            }
        }
    }
}
