package app.allever.android.sample.audiovideo.android

import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PendingPrepare
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.SurfaceType
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

abstract class BaseVideoPlayer {

    protected val TAG = this::class.java.simpleName

    protected open lateinit var engine: IPlayerKernal<*>

    protected val engineListener = object : IPlayerKernal.IListener {
        override fun onPrepared() {
            log(TAG, "onPrepared")
            if (_state != PlayerState.PREPARING) return
            val dur = duration
            _state = PlayerState.PREPARED

            // 检查是否需要自动恢复播放（Surface 切换后）
            val shouldAutoResume = pendingSeekPosition >= 0
            val savedPos = pendingSeekPosition
            pendingSeekPosition = -1L  // 重置标记

            listener?.onPrepared(dur)
            log(TAG, "onPlaybackStateChanged: READY (duration=${dur}ms, autoResume=$shouldAutoResume)")

            // 如果是 Surface 切换后的 reprepare，自动恢复播放
            if (shouldAutoResume && savedPos!! >= 0) {
                log(TAG, "safeSwitchSurface [方案B]: 自动恢复播放 (position=${formatTime(savedPos)})")
                seekTo(savedPos)
                play()
                log(TAG, "safeSwitchSurface [方案B]: 已恢复播放 (${formatTime(savedPos)})")
            }
        }

        override fun onCompletion() {
            log(TAG, "onCompletion")
            if (_state == PlayerState.PLAYING) {
                _state = PlayerState.COMPLETED
                stopProgressTracking()
                listener?.onComplete()
            }
        }

        override fun onError(code: Int, msg: String) {
            log(TAG, "onError: $code, $msg")
            if (_state == PlayerState.PREPARING) {
                // 准备阶段出错，尝试重试
                handlePrepareError(Exception(msg))
            } else {
                // 播放阶段出错
                _state = PlayerState.ERROR
                listener?.onError(code, msg)
            }
        }

        override fun onBufferingUpdate(percent: Int) {
//                log(TAG, "onBufferingUpdate: $percent")
            if (percent > 0) {
                listener?.onBufferingUpdate(percent)
            }
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            //log
            log(TAG, "onVideoSizeChanged: $width x $height")
            if (width > 0 && height > 0) {
                // 保存视频原始尺寸
                videoWidth = width
                videoHeight = height

                listener?.onVideoSizeChanged(width, height)

                adjustSurfaceLayout()
            }
        }

        override fun onInfo() {
            log(TAG, "onInfo")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // 如果正在 seek 操作中，忽略临时的 isPlaying 变化
            if (isSeeking) {
                log(TAG, "onIsPlayingChanged ignored during seeking: isPlaying=$isPlaying")
                return
            }

            if (isPlaying) {
                if (_state != PlayerState.PLAYING) {
                    _state = PlayerState.PLAYING
                    startProgressTracking()
                }
            } else {
                if (_state == PlayerState.PLAYING) {
                    _state = PlayerState.PAUSED
                    stopProgressTracking()
                }
            }
        }
    }

    /** SurfaceView 绑定（兼容方式）*/
    protected var surfaceView: SurfaceView? = null

    /** TextureView 绑定（高级方式）*/
    protected var textureView: TextureView? = null

    /** 当前绑定的 Surface 类型 */
    protected var currentSurfaceType: SurfaceType = SurfaceType.NONE

    /** Surface 是否已就绪（可用于渲染）*/
    @Volatile
    protected var isSurfaceReady: Boolean = false

    /** 是否正在执行 seek 操作（防止 seek 过程中误停进度追踪）*/
    @Volatile
    protected var isSeeking: Boolean = false

    // ==================== 状态管理 ====================

    protected var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("Media3Player", "state: $old -> $value")
                field = value
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态 */
    val state get() = _state

    /** 是否正在播放 */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING && engine.isPlaying()

    /** 当前位置（毫秒）*/
    val currentPosition: Long
        get() = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try {
            engine.getDuration()
        } catch (_: Exception) { 0L }

    // ==================== 配置属性 ====================

    /** 循环模式，默认不循环 */
    var loopMode: LoopMode = LoopMode.NONE
        set(value) {
            field = value
            engine.loopMode(value)
        }

    /** 进度回调间隔（毫秒），默认 200ms */
    var progressIntervalMs: Int = 200

    /** 自动重试次数（出错时自动重试 prepare），默认 0 不重试 */
    var retryCount: Int = 0

    /** 变速倍率（0.5 ~ 3.0），默认 1.0 */
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 3.0f)
            engine.speed(value)
        }

    /** 音量（0.0 ~ 1.0），默认 1.0 */
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            engine.volume(field)
        }

    /**
     * 视频缩放模式（默认 FIT_CENTER）
     *
     * 对于 PlayerView：通过 resizeMode 属性控制（立即生效，无需等待视频尺寸）
     * 对于 SurfaceView/TextureView：通过调整布局尺寸实现（需视频尺寸就绪后生效）
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            adjustSurfaceLayout()
        }

    // ==================== 内部状态 ====================

    /** 进度追踪协程 */
    protected var progressJob: Job? = null

    /** 当前数据源 URI */
    protected var currentUri: Uri? = null

    /** 当前数据源 HTTP 头 */
    protected var currentHeaders: Map<String, String>? = null

    /** 当前数据源 Asset 路径（如果是 Assets 文件） */
    protected var currentAssetPath: String? = null

    /** 剩余重试次数 */
    protected var retryLeft: Int = 0

    /** 视频原始宽度（像素）*/
    protected var videoWidth: Int = 0

    /** 视频原始高度（像素）*/
    protected var videoHeight: Int = 0

    /** 监听器回调 */
    protected var listener: IVideoPlayerListener? = null


    protected var pendingPrepare: PendingPrepare? = null

    /** 切换 Surface 后待恢复的播放位置（-1 表示无需恢复） */
    protected var pendingSeekPosition: Long = -1L

    /** SurfaceView 回调 */
    /**
     * SurfaceView 的 SurfaceHolder 回调
     */
    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            log(TAG, "surfaceCreated")
            onSurfaceReady(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log(TAG, "surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            log(TAG, "surfaceDestroyed")
            isSurfaceReady = false
        }
    }


    // ==================== 绑定渲染 ====================

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

        log("Media3Player", "attach SurfaceView (waiting for surface)")

        initPlayer()
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

        log("Media3Player", "attach TextureView (waiting for surface)")

        initPlayer()
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
     * 不释放内部 ExoPlayer 和其他资源。
     */
    abstract fun detach()

    /**
     * 调整 SurfaceView/TextureView 的布局尺寸
     */
    abstract fun adjustSurfaceLayout()

    protected fun detachSurfaceView() {
        surfaceView?.holder?.removeCallback(surfaceHolderCallback)
        surfaceView = null
        isSurfaceReady = false
        log(TAG, "detach SurfaceView")
    }

    protected fun detachTextureView() {
        textureView?.surfaceTextureListener = null
        textureView = null
        isSurfaceReady = false
        log(TAG, "detach TextureView")
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
     * 由于 ExoPlayer 无法直接读取 Assets 中的文件，
     * 此方法会将文件复制到内部缓存目录后再加载。
     *
     * @param path Assets 中的相对路径（如 "video/test.mp4"）
     */
    fun setAssetSource(path: String) {
        try {
            val context = App.context
            val cacheFile = File(context.cacheDir, "asset_video_${path.hashCode()}")

            // 如果缓存文件不存在或 Assets 文件更新了，重新复制
            if (!cacheFile.exists()) {
                context.assets.open(path).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                log(TAG, "copied asset to cache: ${cacheFile.absolutePath}")
            }

            doSetSource(Uri.fromFile(cacheFile), null, path)
        } catch (e: Exception) {
            log(TAG, "setAssetSource error: ${e.message}")
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.ASSET_COPY_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.ASSET_COPY_FAILED, e.message))
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
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log(TAG, "play() -> PLAYING (from ${_state})")
            }
            PlayerState.PAUSED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log(TAG, "play() -> PLAYING (from PAUSED)")
            }
            else -> {
                log(TAG, "play() ignored (current state: $_state)")
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        if (_state == PlayerState.PLAYING) {
            engine.pause()
            _state = PlayerState.PAUSED
            stopProgressTracking()
            log(TAG, "pause() -> PAUSED")
        }
    }

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    fun stop() {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return

        stopProgressTracking()

        engine.stop()
        _state = PlayerState.STOPPED
        log(TAG, "stop() -> STOPPED")
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
            engine.seekTo(positionMs)
            // 延迟重置标志并确保进度追踪正常运行（seek 是异步操作）
            App.mainHandler.postDelayed({
                isSeeking = false
                // 确保 seek 完成后进度追踪仍在运行
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    log(TAG, "restart progress tracking after seek")
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
            log(TAG, "seekTo error: ${e.message}")
            isSeeking = false
        }
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
                log(TAG, "onSurfaceTextureAvailable: ${width}x${height}")
                onSurfaceReady(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                log(TAG, "onSurfaceTextureSizeChanged: ${width}x${height}")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log(TAG, "onSurfaceTextureDestroyed")
                isSurfaceReady = false
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    /**
     * Surface 就绪处理（统一入口）
     */
    private fun onSurfaceReady(surface: Surface) {
        isSurfaceReady = true
        log(TAG, "Surface ready")

        // 将 Surface 设置给 ExoPlayer
        engine.setSurface(surface)

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
        }
    }

    /**
     * 执行实际的 prepare 操作
     */
    protected fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?) {
        val srcUri = uri ?: return

        // 确保 Player 存在
        initPlayer()

        try {
            engine.setSource(srcUri, headers)

            // 切换到 PREPARING 状态（确保进度追踪已停止）
            _state = PlayerState.PREPARING
            log(TAG, "state -> PREPARING (prepare: $srcUri)")
        } catch (e: Exception) {
            log(TAG, "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    /**
     * 执行缓存的 prepare 操作
     */
    protected fun executePendingPrepare() {
        pendingPrepare?.let { pending ->
            log(TAG, "executing pending prepare: ${pending.uri}")
            pendingPrepare = null
            doPrepareInternal(pending.uri, pending.headers)
        }
    }

    // ==================== 私有方法：初始化 ====================

    /**
     * 初始化 Player 实例
     */
    protected fun initPlayer() {
        engine.loopMode(loopMode)
        engine.speed(speed)
        if (volume != 1.0f) {
            engine.volume(volume)
        }
    }


    /**
     * 处理准备错误（可能触发重试）
     */
    protected fun handlePrepareError(e: Exception) {
        if (retryLeft > 0) {
            retryLeft--
            log(TAG, "retrying... ($retryLeft left)")
            App.mainHandler.postDelayed({
                doPrepareInternal(currentUri, currentHeaders)
            }, 1000)
        } else {
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.RETRY_EXHAUSTED, PlayerErrorCode.formatError(PlayerErrorCode.RETRY_EXHAUSTED, e.message))
        }
    }

    // ==================== 私有方法：资源释放 ====================

    /**
     * 释放 ExoPlayer 实例
     */
    protected fun releasePlayer() {
        stopProgressTracking()
        engine.release()
        log(TAG, "released")
    }

    /**
     * 启动进度追踪协程
     *
     * 定时获取 ExoPlayer 的当前位置和总时长，通过监听器回调。
     */
    protected fun startProgressTracking() {
        // 如果已经在运行且状态正确，不需要重启
        if (progressJob != null && progressJob!!.isActive && _state == PlayerState.PLAYING) {
            log(TAG, "progress tracking already running")
            return
        }

        stopProgressTracking()
        log(TAG, "starting progress tracking (state: $_state)")

        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                val dur = try {
                    engine.getDuration()
                } catch (_: Exception) { 0L }
                listener?.onProgress(pos, dur)
                delay(progressIntervalMs.toLong())
            }
            log(TAG, "progress tracking stopped (loop exited, state: $_state)")
        }
    }

    /**
     * 停止进度追踪协程
     */
    protected fun stopProgressTracking() {
        if (progressJob != null) {
            log(TAG, "stopping progress tracking")
            progressJob?.cancel()
            progressJob = null
        }
    }

    /**
     * 释放所有资源，调用后不可再使用此实例
     */
    fun release() {
        detach()
        releasePlayer()
        currentUri = null
        currentHeaders = null
        currentAssetPath = null
        pendingPrepare = null
        pendingSeekPosition = -1L
        _state = PlayerState.RELEASED
        log(TAG, "release() -> RELEASED")
    }

    // ==================== 监听器设置 ====================

    /**
     * 设置播放事件监听器
     */
    fun setVideoPlayerListener(listener: IVideoPlayerListener?) {
        this.listener = listener
    }

    /**
     * 执行实际的 setSource 操作
     */
    protected fun doSetSource(uri: Uri, headers: Map<String, String>?, assetPath: String?) {
        if (_state == PlayerState.RELEASED) return

        // 停止当前的进度追踪（切换数据源前必须清理）
        stopProgressTracking()

        currentUri = uri
        currentHeaders = headers
        currentAssetPath = assetPath
        retryLeft = retryCount

        // 如果 Surface 未就绪，缓存待执行的 prepare
        if (!isSurfaceReady && currentSurfaceType != SurfaceType.NONE) {
            log(TAG, "Surface not ready, caching prepare request")
            pendingPrepare = PendingPrepare(uri, headers, assetPath)
            _state = PlayerState.PREPARING
            return
        }

        doPrepareInternal(uri, headers)
    }


    /**
     * 安全切换到 SurfaceView
     *
     * @param surfaceView 目标 SurfaceView 实例
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaCodec 状态稳定
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
     * @param delayMs 延迟时间（毫秒），默认 100ms，确保 MediaCodec 状态稳定
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
     * **解决 MediaCodec 缓冲区残留问题（方案 B：stop + reprepare）：**
     *
     * 问题背景：
     * 方案 A（pause + 100ms延迟）只能解决状态机问题，但无法解决缓冲区残留问题：
     * ```
     * MediaCodec$CodecException: Decoder failed: c2.qti.avc.decoder
     *     at releaseOutputBuffer(Native Method)
     * ```
     *
     * 原因分析：
     * 1. pause() 后，ExoPlayer 内部仍有未处理的帧在渲染队列中
     * 2. 切换 Surface 后，这些帧尝试渲染到已失效的旧 Surface
     * 3. 导致 releaseOutputBuffer() 失败，解码器崩溃
     *
     * 解决方案（方案 B：stop → 切换 → reprepare）：
     * 1. stop() 完全停止 ExoPlayer（清空所有缓冲区和渲染队列）
     * 2. detach + attach 安全切换 Surface
     * 3. 使用保存的数据源重新 prepare
     * 4. 在 onPrepared 回调中恢复播放位置并继续播放
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
    protected fun safeSwitchSurface(
        targetAction: () -> Unit,
        targetName: String,
        delayMs: Long = 100L
    ) {
        // 1. 记录当前状态
        val wasPlaying = (_state == PlayerState.PLAYING || _state == PlayerState.PAUSED)
        val savedPosition = currentPosition

        log(TAG, "safeSwitchSurface [方案B]: 开始切换到 $targetName" +
                " (wasPlaying=$wasPlaying, position=${formatTime(savedPosition)})")

        // 2. 完全停止 ExoPlayer（清空所有缓冲区和渲染队列）
        if (_state != PlayerState.IDLE && _state != PlayerState.STOPPED && _state != PlayerState.RELEASED) {
            stop()
            log(TAG, "safeSwitchSurface [方案B]: 已 stop()，清空所有缓冲区")
        }

        // 3. 如果需要恢复播放，保存位置信息
        if (wasPlaying && savedPosition >= 0 && currentUri != null) {
            pendingSeekPosition = savedPosition
            log(TAG, "safeSwitchSurface [方案B]: 待恢复位置 ${formatTime(savedPosition)}")
        }

        // 4. 使用 postDelayed 延迟执行切换操作
        App.mainHandler.postDelayed({
            try {
                log(TAG, "safeSwitchSurface [方案B]: 执行切换到 $targetName")

                // 执行实际的切换操作（detach + attach）
                targetAction()

                // 5. 重新准备数据源（因为已经 stop()，必须 reprepare）
                if (currentUri != null) {
                    log(TAG, "safeSwitchSurface [方案B]: 重新 prepare 数据源" +
                            " (autoResume=${pendingSeekPosition >= 0})")
                    doSetSource(currentUri!!, currentHeaders, currentAssetPath)

                    // 注意：
                    // - 如果 pendingSeekPosition >= 0（之前在播放），
                    //   onPrepared 回调会自动 seekTo + play
                    // - 如果 pendingSeekPosition < 0（之前未播放），
                    //   仅 reprepare，不自动播放，等待用户操作
                } else {
                    log(TAG, "safeSwitchSurface [方案B]: 切换完成（无数据源）")
                }
            } catch (e: Exception) {
                log(TAG, "safeSwitchSurface [方案B]: 切换失败 - ${e.message}")
                pendingSeekPosition = -1L  // 重置
                _state = PlayerState.ERROR
                listener?.onError(PlayerErrorCode.SURFACE_SWITCH_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.SURFACE_SWITCH_FAILED, e.message))
            }
        }, delayMs)
    }

}