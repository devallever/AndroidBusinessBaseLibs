package app.allever.android.sample.audiovideo.core.player

import android.net.Uri
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.core.engine.IPlayerEngine
import app.allever.android.sample.audiovideo.core.engine.IPlayerEngineListener
import app.allever.android.sample.audiovideo.core.engine.MediaPlayerEngine
import app.allever.android.sample.audiovideo.core.render.IVideoRender
import app.allever.android.sample.audiovideo.core.render.SurfaceViewRender
import app.allever.android.sample.audiovideo.core.render.TextureViewRender
import app.allever.android.sample.audiovideo.core.render.VideoViewRender
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PendingPrepare
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * 视频播放器协调器（组合模式）
 *
 * ## 职责
 * - 组合 [IPlayerEngine]（引擎）和 [IVideoRender]（渲染器）完成视频播放
 * - 统一状态机转换逻辑，确保线程安全和状态一致性
 * - 管理 Surface/引擎的绑定生命周期
 * - 提供进度追踪、变速、音量、循环、Seek 等通用能力
 * - 处理 Surface 异步就绪的 PendingPrepare 机制
 * - 实现安全的 Surface 切换方案（避免 MediaCodec 状态机竞态条件）
 *
 * ## 设计模式
 * **组合模式**：
 * - 本类通过组合方式集成引擎和渲染器，而非继承
 * - 引擎和渲染器可以独立替换，实现完全解耦
 * - 遵循"多用组合，少用继承"的设计原则
 *
 * ## 架构优势
 * 1. **完全解耦**：引擎和渲染可任意组合（3×3=9 种组合）
 * 2. **单一职责**：Engine 只管播放，Render 只管显示
 * 3. **易于扩展**：新增引擎或渲染只需实现接口
 * 4. **易于测试**：可 Mock Engine 或 Render 进行单测
 *
 * ## 支持的引擎
 * - [MediaPlayerEngine]：Android MediaPlayer（默认）
 * - ExoPlayerEngine：Google ExoPlayer/Media3（未来）
 * - IjkPlayerEngine：Bilibili IJKPlayer（未来）
 *
 * ## 支持的渲染器
 * - [SurfaceViewRender]：SurfaceView 渲染（推荐，性能好）
 * - [TextureViewRender]：TextureView 渲染（支持动画）
 * - [VideoViewRender]：VideoView 渲染（最简单）
 *
 * ## 状态机
 * ```
 * IDLE → PREPARING → PREPARED → PLAYING ↔ PAUSED
 *   ↑                  ↓           ↓
 *   └──────────────────┴───────────┘→ STOPPED → IDLE (可重新 prepare)
 *                                         ↓
 *                                       RELEASED (不可再使用)
 * ```
 *
 * ## 使用示例
 * ```kotlin
 * // 示例 1：使用默认配置（MediaPlayer + SurfaceView）
 * val player = VideoPlayer()
 * player.attach(container)
 * player.setListener(object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 *     override fun onComplete() { log("播放完成") }
 *     override fun onError(code: Int, msg: String) { log("错误: $msg") }
 * })
 * player.setSource("https://example.com/video.mp4")
 *
 * // 示例 2：自定义引擎和渲染器
 * val player = VideoPlayer(
 *     engine = MediaPlayerEngine(),
 *     render = TextureViewRender()
 * )
 * player.attach(container)
 * player.setSource("/sdcard/video.mp4")
 * player.play()
 *
 * // 页面生命周期管理
 * override fun onPause() {
 *     if (player.isPlaying) player.pause()
 *     player.detach()
 * }
 *
 * override fun onResume() {
 *     player.attach(container)
 * }
 *
 * override fun onDestroy() {
 *     player.release()
 * }
 * ```
 *
 * ## 重要提示
 * 1. **必须在 attach 之后才能 setSurface**：Surface 绑定是异步的，需等待就绪
 * 2. **PREPARING 状态不能调用 getDuration**：会触发 MediaPlayer 错误 (-38, 0)
 * 3. **切换 Surface 必须使用 safeSwitchToXxx 方法**：直接 detach+attach 会崩溃
 * 4. **release 后不可再使用**：必须创建新实例
 *
 * @see IPlayerEngine 引擎接口
 * @see IVideoRender 渲染接口
 * @see PlayerState 状态枚举
 */
class VideoPlayer(
    /** 引擎实例（可变，支持运行时切换）*/
    var engine: IPlayerEngine = MediaPlayerEngine(),
    /** 渲染器实例（可变，支持运行时切换）*/
    protected var render: IVideoRender = SurfaceViewRender()
) {

    /** 日志标签 */
    protected val TAG = "VideoPlayer"

    // ==================== 1. 引擎事件监听 ====================

    /**
     * 引擎事件监听器（统一处理所有引擎回调）
     *
     * 将底层引擎的事件转换为上层业务事件，
     * 并执行相应的状态管理和副作用操作。
     */
    private val engineListener = object : IPlayerEngineListener {

        override fun onPrepared() {
            log(TAG, "onPrepared")
            if (_state != PlayerState.PREPARING) return

            val dur = duration
            _state = PlayerState.PREPARED

            // 检查是否需要自动恢复播放（Surface 切换后）
            val shouldAutoResume = pendingSeekPosition >= 0
            val savedPos = pendingSeekPosition
            pendingSeekPosition = -1L

            _playerListener?.onPrepared(dur)

            if (shouldAutoResume && savedPos!! >= 0) {
                log(TAG, "自动恢复播放 (position=${formatTime(savedPos)})")
                seekTo(savedPos)
                play()
            }
        }

        override fun onCompletion() {
            log(TAG, "onCompletion")
            if (_state == PlayerState.PLAYING) {
                _state = PlayerState.COMPLETED
                stopProgressTracking()
                _playerListener?.onComplete()
            }
        }

        override fun onError(code: Int, msg: String) {
            log(TAG, "onError: $code, $msg")

            if (_state == PlayerState.PREPARING) {
                handlePrepareError(Exception(msg))
            } else {
                _state = PlayerState.ERROR
                _playerListener?.onError(code, msg)
            }
        }

        override fun onBufferingUpdate(percent: Int) {
            if (percent > 0) {
                _playerListener?.onBufferingUpdate(percent)
            }
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            log(TAG, "onVideoSizeChanged: $width x $height")

            if (width > 0 && height > 0) {
                videoWidth = width
                videoHeight = height
                _playerListener?.onVideoSizeChanged(width, height)
                render.adjustLayout(videoWidth, videoHeight, videoScaleMode)
            }
        }

        override fun onInfo() {
            log(TAG, "onInfo")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
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

    // ==================== 2. 状态管理 ====================

    /**
     * 内部状态（带日志的状态转换器）
     */
    protected var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log(TAG, "state: $old -> $value")
                field = value
                _playerListener?.onStateChanged(old, value)
            }
        }

    /** 当前状态（只读）*/
    val state get() = _state

    /** 是否正在播放（双重检查）*/
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING && engine.isPlaying()

    /** 当前播放位置（毫秒）*/
    val currentPosition: Long
        get() = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }

    /** 视频总时长（毫秒）*/
    val duration: Long
        get() = try { engine.getDuration() } catch (_: Exception) { 0L }

    // ==================== 3. 配置属性 ====================

    /** 循环模式（默认不循环）*/
    var loopMode: LoopMode = LoopMode.NONE
        set(value) {
            field = value
            engine.setLoopMode(value)
        }

    /** 进度回调间隔（毫秒），默认 200ms*/
    var progressIntervalMs: Int = 200

    /** 自动重试次数（默认 0，不重试）*/
    var retryCount: Int = 0

    /** 变速倍率（0.5x ~ 3.0x），默认 1.0x*/
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 3.0f)
            engine.setSpeed(field)
        }

    /** 音量（0.0 ~ 1.0），默认 1.0*/
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            engine.setVolume(field)
        }

    /** 视频缩放模式（默认 FIT_CENTER）*/
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            render.adjustLayout(videoWidth, videoHeight, value)
        }

    // ==================== 4. 绑定与解绑 ====================

    /** 当前绑定的容器 */
    private var container: ViewGroup? = null

    /** 是否已绑定 */
    private var isAttached: Boolean = false

    /**
     * 绑定到容器并初始化渲染器和引擎
     *
     * @param container 父容器（ViewGroup）
     */
    fun attach(container: ViewGroup) {
        this.container = container
        
        // 初始化并绑定渲染器
        render.attach(container, engine)
        
        // 设置渲染器的 Surface 回调
        setupRenderCallbacks()
        
        // 初始化引擎
        initPlayer()
        
        isAttached = true
        log(TAG, "attached to container (renderName=${render.renderName})")
    }

    /**
     * 解绑当前容器（页面 onPause/onDestroyView 时调用）
     */
    fun detach() {
        stopProgressTracking()
        stopPreparingMonitor()
        
        // 从渲染器解绑 Surface
        engine.setSurface(null, render)
        
        // 解绑渲染器
        render.detach()
        
        isAttached = false
        container = null
        log(TAG, "detached")
    }

    // ==================== 5. 数据源设置与 Prepare ====================

    /** 当前数据源 URI */
    private var currentUri: Uri? = null

    /** 当前数据源 HTTP 请求头 */
    private var currentHeaders: Map<String, String>? = null

    /** 当前数据源 Asset 路径 */
    private var currentAssetPath: String? = null

    /** 待执行的 Prepare 参数 */
    private var pendingPrepare: PendingPrepare? = null

    /**
     * 设置数据源并开始准备（不自动播放）
     *
     * 支持 http/https/file/content/android_asset 协议。
     */
    fun setSource(url: String) {
        val uri = Uri.parse(url)

        if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
            val assetPath = uri.path?.substringAfter("/android_asset/") ?: ""
            setAssetSource(assetPath)
            return
        }

        doSetSource(uri, null, null)
    }

    /**
     * 设置视频数据源并准备（不自动播放）
     */
    fun setSource(uri: Uri, headers: Map<String, String>? = null) {
        if (uri.scheme == "file" && uri.path?.contains("/android_asset/") == true) {
            val assetPath = uri.path?.substringAfter("/android_asset/") ?: ""
            setAssetSource(assetPath)
            return
        }

        doSetSource(uri, headers, null)
    }

    /**
     * 设置 assets 目录下的视频文件并准备
     */
    fun setAssetSource(path: String) {
        try {
            val context = App.context
            val cacheFile = File(context.cacheDir, "asset_video_${path.hashCode()}")

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
            _playerListener?.onError(PlayerErrorCode.ASSET_COPY_FAILED, 
                PlayerErrorCode.formatError(PlayerErrorCode.ASSET_COPY_FAILED, e.message))
        }
    }

    /**
     * 执行实际的 setSource 操作
     */
    protected fun doSetSource(uri: Uri, headers: Map<String, String>?, assetPath: String?) {
        if (_state == PlayerState.RELEASED) return

        stopProgressTracking()

        currentUri = uri
        currentHeaders = headers
        currentAssetPath = assetPath
        retryLeft = retryCount

        if (!render.isSurfaceReady()) {
            log(TAG, "Surface not ready, caching prepare request")
            pendingPrepare = PendingPrepare(uri, headers, assetPath)
            _state = PlayerState.PREPARING
            return
        }

        doPrepareInternal(uri, headers)
    }

    /**
     * 执行实际的 prepare 操作
     */
    protected open fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?) {
        log(TAG, "doPrepareInternal: $uri")
        uri ?: return

        try {
            _state = PlayerState.PREPARING

            engine.reset()
            engine.setSource(uri, headers)

            // 绑定当前 Surface
            render.getSurface()?.let { surface ->
                engine.setSurface(surface, render)
            }

            // 应用当前参数
            engine.setVolume(volume)
            engine.setSpeed(speed)
            engine.setLoopMode(loopMode)

            // 异步准备
            engine.prepareAsync()

            // 启动 PREPARING 状态监控
            startPreparingStateMonitor()

        } catch (e: Exception) {
            log(TAG, "doPrepareInternal error: ${e.message}")
            handlePrepareError(e)
        }
    }

    /**
     * 执行缓存的 prepare 操作
     */
    protected open fun executePendingPrepare() {
        pendingPrepare?.let { pending ->
            log(TAG, "executing pending prepare: ${pending.uri}")
            pendingPrepare = null
            doPrepareInternal(pending.uri, pending.headers)
        }
    }

    // ==================== 6. 播放控制 ====================

    /**
     * 开始播放 或 从暂停恢复播放
     */
    open fun play() {
        when (_state) {
            PlayerState.PREPARED, PlayerState.COMPLETED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                startPreparingStateMonitor()
                log(TAG, "play() -> PLAYING")
            }
            PlayerState.PAUSED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                startPreparingStateMonitor()
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
        log(TAG, "pause (state=$_state)")
        if (_state == PlayerState.PLAYING) {
            try {
                engine.pause()
                _state = PlayerState.PAUSED
                stopProgressTracking()
            } catch (e: Exception) {
                log(TAG, "pause error: ${e.message}")
            }
        }
    }

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    open fun stop() {
        log(TAG, "stop (state=$_state)")
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            stopProgressTracking()
            stopPreparingMonitor()
            engine.stop()
            _state = PlayerState.IDLE
        } catch (e: Exception) {
            log(TAG, "stop error: ${e.message}")
        }
    }

    /**
     * 跳转到指定位置
     */
    open fun seekTo(positionMs: Long) {
        log(TAG, "seekTo $positionMs (state=$_state)")
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            isSeeking = true
            engine.seekTo(positionMs)
            stopProgressTracking()
            App.mainHandler.postDelayed({
                isSeeking = false
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
            log(TAG, "seekTo error: ${e.message}")
            isSeeking = false
        }
    }

    /**
     * 释放所有资源，调用后不可再使用此实例
     */
    fun release() {
        detach()
        
        // 释放引擎
        engine.release()
        
        // 释放渲染器
        render.release()
        
        _playerListener = null
        currentUri = null
        currentHeaders = null
        currentAssetPath = null
        pendingPrepare = null
        pendingSeekPosition = -1L
        _state = PlayerState.RELEASED
        log(TAG, "release() -> RELEASED")
    }

    // ==================== 7. 进度追踪 ====================

    /** 进度追踪协程 */
    protected var progressJob: Job? = null

    protected fun startProgressTracking() {
        if (progressJob != null && progressJob!!.isActive && _state == PlayerState.PLAYING) {
            log(TAG, "progress tracking already running")
            return
        }

        stopProgressTracking()
        log(TAG, "starting progress tracking (state: $_state)")

        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                val dur = try { engine.getDuration() } catch (_: Exception) { 0L }
                _playerListener?.onProgress(pos, dur)
                delay(progressIntervalMs.toLong())
            }
            log(TAG, "progress tracking stopped (loop exited, state: $_state)")
        }
    }

    protected fun stopProgressTracking() {
        if (progressJob != null) {
            log(TAG, "stopping progress tracking")
            progressJob?.cancel()
            progressJob = null
        }
    }

    // ==================== 8. 错误处理与重试 ====================

    /** 剩余重试次数 */
    protected var retryLeft: Int = 0

    protected open fun handlePrepareError(e: Exception) {
        if (retryLeft > 0) {
            retryLeft--
            log(TAG, "retrying... ($retryLeft left)")
            App.mainHandler.postDelayed({
                doPrepareInternal(currentUri, currentHeaders)
            }, 1000)
        } else {
            _state = PlayerState.ERROR
            _playerListener?.onError(PlayerErrorCode.RETRY_EXHAUSTED,
                PlayerErrorCode.formatError(PlayerErrorCode.RETRY_EXHAUSTED, e.message))
        }
    }

    // ==================== 9. Surface 切换（安全切换方案）====================

    /** 待恢复的播放位置 */
    protected var pendingSeekPosition: Long = -1L

    /** 是否正在执行安全切换操作 */
    @Volatile
    protected var isSafeSwitching: Boolean = false

    /** 是否正在 seek 操作中 */
    @Volatile
    protected var isSeeking: Boolean = false

    /**
     * 安全切换到新的渲染器
     *
     * @param newRender 新的渲染器实例
     * @param delayMs 延迟时间（毫秒），默认 100ms
     */
    fun safeSwitchToRender(newRender: IVideoRender, delayMs: Long = 100L) {
        safeSwitchRender(newRender, delayMs)
    }

    /**
     * 安全切换 Surface 的核心实现
     */
    protected fun safeSwitchRender(targetRender: IVideoRender, delayMs: Long) {
        if (isSafeSwitching) {
            log(TAG, "safeSwitchRender: 忽略重复调用（正在切换到 ${targetRender.renderName}）")
            return
        }

        isSafeSwitching = true

        try {
            val wasPlaying = (_state == PlayerState.PLAYING || _state == PlayerState.PAUSED ||
                    _state == PlayerState.PREPARING)
            val savedPosition = currentPosition

            log(TAG, "safeSwitchRender: 开始切换到 ${targetRender.renderName}" +
                    " (wasPlaying=$wasPlaying, position=${formatTime(savedPosition)}, state=$_state)")

            if (_state != PlayerState.IDLE && _state != PlayerState.STOPPED && _state != PlayerState.RELEASED) {
                stop()
                log(TAG, "safeSwitchRender: 已 stop()")
            }

            if (wasPlaying && savedPosition >= 0 && currentUri != null) {
                pendingSeekPosition = savedPosition
                log(TAG, "safeSwitchRender: 待恢复位置 ${formatTime(savedPosition)}")
            }

            App.mainHandler.postDelayed({
                try {
                    log(TAG, "safeSwitchRender: 执行切换到 ${targetRender.renderName}")

                    // 解绑旧渲染器
                    render.detach()

                    // 绑定新渲染器
                    targetRender.attach(container!!, engine)

                    // 【关键】更新渲染器引用！
                    render = targetRender
                    log(TAG, "safeSwitchRender: 已更新 render 引用到 ${render.renderName}")
                    
                    // 设置新渲染器的回调
                    setupRenderCallbacks()

                    val surfaceReady = targetRender.isSurfaceReady()

                    if (!surfaceReady && targetRender.renderName.isNotEmpty()) {
                        log(TAG, "safeSwitchRender: Surface 未就绪，延迟 50ms 等待...")
                        App.mainHandler.postDelayed({
                            try {
                                performPrepareAfterSwitch()
                            } catch (e: Exception) {
                                handlePrepareFailure(e)
                            }
                        }, 50L)
                    } else {
                        log(TAG, "safeSwitchRender: Surface 已就绪，立即 prepare")
                        try {
                            performPrepareAfterSwitch()
                        } catch (e: Exception) {
                            handlePrepareFailure(e)
                        }
                    }
                } catch (e: Exception) {
                    log(TAG, "safeSwitchRender: 切换失败 - ${e.message}")
                    pendingSeekPosition = -1L
                    _state = PlayerState.ERROR
                    _playerListener?.onError(PlayerErrorCode.SURFACE_SWITCH_FAILED,
                        PlayerErrorCode.formatError(PlayerErrorCode.SURFACE_SWITCH_FAILED, e.message))
                } finally {
                    isSafeSwitching = false
                }
            }, delayMs)

        } catch (e: Exception) {
            log(TAG, "safeSwitchRender: 准备阶段失败 - ${e.message}")
            isSafeSwitching = false
            pendingSeekPosition = -1L
            _state = PlayerState.ERROR
            _playerListener?.onError(PlayerErrorCode.PREPARE_FAILED,
                PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
        }
    }

    /**
     * 执行切换后的 prepare 操作
     * 
     * 注意：此时 render 引用已经更新到新渲染器
     */
    protected fun performPrepareAfterSwitch() {
        if (currentUri == null && currentAssetPath == null) {
            log(TAG, "safeSwitchRender: 切换完成（无数据源）")
            return
        }

        log(TAG, "safeSwitchRender: 重新准备数据源" +
                " (autoResume=${pendingSeekPosition >= 0}, renderName=${render.renderName})")

        if (currentAssetPath != null && currentAssetPath!!.isNotEmpty()) {
            try {
                val context = App.context.applicationContext
                val cacheFile = File(context.cacheDir, "asset_video_${currentAssetPath!!.hashCode()}")

                if (!cacheFile.exists()) {
                    context.assets.open(currentAssetPath!!).use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val cacheUri = Uri.fromFile(cacheFile)
                currentUri = cacheUri
                
                // 绑定新 Surface（使用更新后的 render）
                render.getSurface()?.let { surface ->
                    engine.setSurface(surface, render)
                    log(TAG, "performPrepareAfterSwitch: 已绑定 Surface 到引擎")
                } ?: run {
                    log(TAG, "performPrepareAfterSwitch: Surface 为空！")
                }
                
                doPrepareInternal(cacheUri, currentHeaders)

            } catch (e: Exception) {
                handlePrepareFailure(e)
            }

        } else if (currentUri != null) {
            // 绑定新 Surface（使用更新后的 render）
            render.getSurface()?.let { surface ->
                engine.setSurface(surface, render)
                log(TAG, "performPrepareAfterSwitch: 已绑定 Surface 到引擎")
            } ?: run {
                log(TAG, "performPrepareAfterSwitch: Surface 为空！")
            }
            
            log(TAG, "safeSwitchRender: 使用 doPrepareInternal (uri=$currentUri)")
            doPrepareInternal(currentUri!!, currentHeaders)
        }

        log(TAG, "safeSwitchRender: prepare 完成，等待 onPrepared 或 PREPARING Monitor")
    }

    private fun handlePrepareFailure(e: Exception) {
        log(TAG, "safeSwitchRender: prepare 失败 - ${e.message}")
        pendingSeekPosition = -1L
        _state = PlayerState.ERROR
        _playerListener?.onError(PlayerErrorCode.PREPARE_FAILED,
            PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
    }

    // ==================== 10. 布局自适应 ====================

    /** 视频原始宽度 */
    protected var videoWidth: Int = 0

    /** 视频原始高度 */
    protected var videoHeight: Int = 0

    // ==================== 11. PREPARING 状态监控 ====================

    /** PREPARING 状态监控协程 */
    protected var preparingMonitorJob: Job? = null

    protected fun startPreparingStateMonitor() {
        preparingMonitorJob?.cancel()
        preparingMonitorJob = CoroutineScope(Dispatchers.Main).launch {
            val maxCheckTime = 30000L
            val startTime = System.currentTimeMillis()

            while (isActive && System.currentTimeMillis() - startTime < maxCheckTime) {
                if (_state != PlayerState.PREPARING) return@launch

                delay(100)

                try {
                    val actualIsPlaying = engine.isPlaying()

                    if (actualIsPlaying) {
                        log(TAG, "PREPARING Monitor: 检测到正在播放！修正状态")

                        var dur = 0L
                        try {
                            delay(50)
                            dur = engine.getDuration()
                        } catch (_: Exception) {}

                        val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                        log(TAG, "PREPARING Monitor: duration=$dur, position=$pos")

                        val shouldAutoResume = pendingSeekPosition >= 0
                        val savedPos = pendingSeekPosition
                        pendingSeekPosition = -1L

                        _playerListener?.onPrepared(dur)

                        if (shouldAutoResume && savedPos >= 0) {
                            log(TAG, "PREPARING Monitor: 自动恢复播放 (position=${formatTime(savedPos)})")
                            seekTo(savedPos.toLong())
                        }

                        _state = PlayerState.PLAYING
                        _playerListener?.onStateChanged(PlayerState.PREPARING, PlayerState.PLAYING)

                        startProgressTracking()

                        return@launch
                    }
                } catch (_: Exception) {}
            }

            if (_state == PlayerState.PREPARING) {
                log(TAG, "PREPARING Monitor: 超时，仍处于 PREPARING 状态")
            }
        }
    }

    protected fun stopPreparingMonitor() {
        if (preparingMonitorJob != null) {
            log(TAG, "stopping PREPARING state monitor")
            preparingMonitorJob?.cancel()
            preparingMonitorJob = null
        }
    }

    // ==================== 12. 监听器设置与初始化 ====================

    /** 外部事件监听器 */
    protected var _playerListener: IVideoPlayerListener? = null

    /**
     * 设置播放事件监听器
     */
    fun setListener(_playerListener: IVideoPlayerListener?) {
        this._playerListener = _playerListener
    }

    /**
     * 初始化 Player 实例（应用当前配置）
     */
    protected fun initPlayer() {
        engine.init()
        engine.setListener(engineListener)
        engine.setLoopMode(loopMode)
        engine.setSpeed(speed)
        if (volume != 1.0f) {
            engine.setVolume(volume)
        }
    }

    /**
     * 设置渲染器的 Surface 回调
     */
    private fun setupRenderCallbacks() {
        render.setOnSurfaceReadyListener { surface ->
            log(TAG, "render surface ready")
            onRenderSurfaceReady(surface)
        }

        render.setOnSurfaceDestroyedListener {
            log(TAG, "render surface destroyed")
            engine.setSurface(null, render)
        }
    }

    /**
     * 处理渲染器 Surface 就绪
     */
    private fun onRenderSurfaceReady(surface: Surface) {
        // 将 Surface 设置给引擎
        try {
            engine.setSurface(surface, render)
        } catch (e: Exception) {
            log(TAG, "setSurface error: ${e.message}")
        }

        // 如果有待执行的 prepare，立即执行
        pendingPrepare?.let {
            executePendingPrepare()
        }
    }

    /**
     * 获取当前渲染器名称
     */
    fun getCurrentRenderName(): String = render.renderName

    /**
     * 获取当前渲染视图
     */
    fun getRenderView(): View? = render.renderView
}
