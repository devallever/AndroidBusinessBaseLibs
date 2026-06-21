package app.allever.android.sample.audiovideo.android

import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.VideoView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.MediaPlayerKernal
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.SurfaceType
import app.allever.android.sample.audiovideo.lib.VideoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    /** PREPARING 状态监控协程 */
    private var preparingMonitorJob: Job? = null

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
            }
            SurfaceType.SURFACE_VIEW -> {
                detachSurfaceView()
            }
            SurfaceType.TEXTURE_VIEW -> {
                detachTextureView()
            }
            SurfaceType.NONE -> {}
            else -> {}
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
    override fun safeSwitchSurface(
        targetAction: () -> Unit,
        targetName: String,
        delayMs: Long
    ) {
        // 防重复调用
        if (isSafeSwitching) {
            log(TAG,"safeSwitchSurface [方案B]: ⚠️ 忽略重复调用（正在切换到 $targetName）")
            return
        }
        
        isSafeSwitching = true
        
        try {
            // 1. 记录当前状态（扩展到 PREPARING 状态）
            val wasPlaying = (_state == PlayerState.PLAYING || _state == PlayerState.PAUSED ||
                    _state == PlayerState.PREPARING)
            val savedPosition = currentPosition

            log(TAG,"safeSwitchSurface [方案B]: 开始切换到 $targetName" +
                    " (wasPlaying=$wasPlaying, position=${formatTime(savedPosition)}, state=$_state)")

            // 2. 完全停止 MediaPlayer（清空所有缓冲区和渲染队列）
            if (_state != PlayerState.IDLE && _state != PlayerState.STOPPED && _state != PlayerState.RELEASED) {
                stop()
                log(TAG,"safeSwitchSurface [方案B]: 已 stop()，清空所有缓冲区")
            }

            // 3. 如果需要恢复播放，保存位置信息
            if (wasPlaying && savedPosition >= 0 && currentUri != null) {
                pendingSeekPosition = savedPosition
                log(TAG,"safeSwitchSurface [方案B]: 待恢复位置 ${formatTime(savedPosition)}")
            }

            // 4. 使用 postDelayed 延迟执行切换操作
            App.mainHandler.postDelayed({
                try {
                    log(TAG,"safeSwitchSurface [方案B]: 执行切换到 $targetName")

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
                        log(TAG,"safeSwitchSurface [方案B]: ⚠️ Surface 未就绪，延迟 50ms 等待...")
                        
                        App.mainHandler.postDelayed({
                            try {
                                performPrepareAfterSwitch()
                            } catch (e: Exception) {
                                handlePrepareFailure(e)
                            }
                        }, 50L)
                    } else {
                        log(TAG,"safeSwitchSurface [方案B]: Surface 已就绪，立即 prepare")
                        try {
                            performPrepareAfterSwitch()
                        } catch (e: Exception) {
                            handlePrepareFailure(e)
                        }
                    }
                } catch (e: Exception) {
                    log(TAG,"safeSwitchSurface [方案B]: 切换失败 - ${e.message}")
                    pendingSeekPosition = -1L
                    _state = PlayerState.ERROR
                    listener?.onError(PlayerErrorCode.SURFACE_SWITCH_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.SURFACE_SWITCH_FAILED, e.message))
                } finally {
                    isSafeSwitching = false
                }
            }, delayMs)

        } catch (e: Exception) {
            log(TAG,"safeSwitchSurface [方案B]: 准备阶段失败 - ${e.message}")
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
            log(TAG,"safeSwitchSurface [方案B]: 切换完成（无数据源）")
            return
        }

        log(TAG,"safeSwitchSurface [方案B]: 重新准备数据源" +
                " (autoResume=${pendingSeekPosition >= 0})")

        if (currentAssetPath != null && currentAssetPath!!.isNotEmpty()) {
            // Assets 数据源：必须手动处理文件复制 + prepare
            log(TAG,"safeSwitchSurface [方案B]: 使用 setAssetSource (assets)")

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
                    log(TAG,"safeSwitchSurface [方案B]: 已重新复制 asset 文件")
                }

                // 直接使用 doPrepareInternal，跳过 isSurfaceReady 检查
                val cacheUri = Uri.fromFile(cacheFile)
                currentUri = cacheUri
                doPrepareInternal(cacheUri, currentHeaders)

            } catch (e: Exception) {
                log(TAG,"safeSwitchSurface [方案B]: Asset 处理失败 - ${e.message}")
                handlePrepareFailure(e)
            }

        } else if (currentUri != null) {
            // 普通 URI（HTTP/本地文件/Content Provider）：直接 prepare
            log(TAG,"safeSwitchSurface [方案B]: 使用 doPrepareInternal (uri=$currentUri)")
            doPrepareInternal(currentUri!!, currentHeaders)
        }

        log(TAG,"safeSwitchSurface [方案B]: prepare 完成，等待 onPrepared 或 PREPARING Monitor")
    }

    /**
     * 处理 prepare 失败的情况
     */
    private fun handlePrepareFailure(e: Exception) {
        log(TAG,"safeSwitchSurface [方案B]: prepare 失败 - ${e.message}")
        pendingSeekPosition = -1L
        _state = PlayerState.ERROR
        listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
    }

    // ==================== 播放控制 ====================

    /**
     * 开始播放 或 从暂停恢复播放
     *
     * - PREPARED/COMPLETED → 开始播放
     * - PAUSED → 恢复播放
     * - 其他状态 → 忽略
     */
    override fun play() {
        when (_state) {
            PlayerState.PREPARED, PlayerState.COMPLETED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                startPreparingStateMonitor()
                log(TAG,"play() -> PLAYING (from $_state)")
            }
            PlayerState.PAUSED -> {
                engine.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
                log(TAG,"play() -> PLAYING (from PAUSED)")
            }
            else -> {
                log(TAG,"play() ignored (current state: $_state)")
            }
        }
    }

    /**
     * 停止播放（保留资源，可重新 prepare）
     */
    override fun stop() {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return

        stopProgressTracking()
        stopPreparingMonitor()

        try {
            engine.stop()
        } catch (_: Exception) {}

        _state = PlayerState.STOPPED
        log(TAG,"stop() -> STOPPED")
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

    // ==================== 私有方法：准备流程 ====================

    /**
     * 执行实际的 prepare 操作
     */
    override fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?) {
        val srcUri = uri ?: return

        initPlayer()

        try {
            // 重置 MediaPlayer（重要：确保处于 Idle 状态）
            engine.reset()

            engine.setSource(srcUri, headers)

            // 绑定当前 Surface（必须在 reset 之后、prepareAsync 之前）
            bindCurrentSurface()

            // 异步准备
            engine.prepareAsync()

            // 切换到 PREPARING 状态
            _state = PlayerState.PREPARING
            log(TAG,"state -> PREPARING (prepare: $srcUri)")

            // 启动 PREPARING 状态监控（作为 onPrepared 的备用方案）
            startPreparingStateMonitor()

        } catch (e: Exception) {
            log(TAG,"prepare error: ${e.message}")
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
                            engine.setSurface(holder.surface)
                            surfaceBound = true
                            log(TAG,"bindSurface: VideoView success")
                        } catch (e: Exception) {
                            log(TAG,"bindSurface: VideoView error - ${e.message}")
                        }
                    } else {
                        log(TAG,"bindSurface: VideoView invalid")
                    }
                }
            }
            SurfaceType.SURFACE_VIEW -> {
                surfaceView?.holder?.let { holder ->
                    if (holder.surface.isValid) {
                        try {
                            engine.setSurface(holder.surface)
                            surfaceBound = true
                            log(TAG,"bindSurface: SurfaceView success")
                        } catch (e: Exception) {
                            log(TAG,"bindSurface: SurfaceView error - ${e.message}")
                        }
                    } else {
                        log(TAG,"bindSurface: SurfaceView invalid")
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
                                engine.setSurface(surface)
                                surfaceBound = true
                                log(TAG,"bindSurface: TextureView success")
                            } else {
                                log(TAG,"bindSurface: TextureView Surface invalid")
                                surface.release()
                            }
                        } catch (e: Exception) {
                            log(TAG,"bindSurface: TextureView error - ${e.message}")
                        }
                    } else {
                        log(TAG,"bindSurface: TextureView not ready " +
                                "(isAvailable=${tv.isAvailable}, surfaceTexture=$surfaceTexture)")
                    }
                }
            }
            else -> {}
        }

        if (!surfaceBound && currentSurfaceType != SurfaceType.NONE) {
            log(TAG,"bindSurface: ⚠️ Surface not bound, may have no video output")
        }
    }

    // ==================== 私有方法：配置应用 ====================

    // ==================== 私有方法：资源释放 ====================

    /**
     * 释放 MediaPlayer 实例
     */
    override fun releasePlayer() {
        super.releasePlayer()
        stopPreparingMonitor()
    }

    // ==================== 内部：PREPARING State Monitor ====================

    /**
     * 启动 PREPARING 状态监控协程
     *
     * 作为 onPrepared 回调的备用方案：
     * 在某些情况下（特别是快速 stop+reprepare），
     * MediaPlayer 可能不触发 OnPreparedListener，
     * 此时会通过轮询检测播放是否已经开始。
     *
     * ⚠️ 重要：PREPARING 状态下不能调用 getDuration()，
     * 否则会触发 MediaPlayer 错误 (-38, 0)。
     * 只使用 isPlaying() 来检测是否已准备好。
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
                    // ✅ 只检查 isPlaying()，不在 PREPARING 状态调用 getDuration()
                    val actualIsPlaying = engine.isPlaying()

                    if (actualIsPlaying) {
                        // 正在播放 → 已准备就绪（延迟获取 duration 避免状态冲突）
                        log(TAG,"⚡ PREPARING Monitor: 检测到正在播放！修正状态")

                        // 延迟获取 duration（确保 MediaPlayer 已完全进入 PLAYING 状态）
                        var dur = 0L
                        try {
                            // 小延迟后获取，避免在状态转换临界点调用
                            delay(50)
                            dur = engine.getDuration()
                        } catch (_: Exception) {
                            log(TAG,"⚠️ PREPARING Monitor: 获取 duration 失败（可能还在准备中）")
                        }

                        val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                        log(TAG,"PREPARING Monitor: duration=$dur, position=$pos")

                        // ✨ 检查是否需要自动恢复播放（Surface 切换后）
                        val shouldAutoResume = pendingSeekPosition >= 0
                        val savedPos = pendingSeekPosition
                        pendingSeekPosition = -1L  // 重置标记

                        log(TAG,"PREPARING Monitor: autoResume=$shouldAutoResume" +
                                (if (shouldAutoResume) ", savedPosition=${formatTime(savedPos)}" else ""))

                        // 通知准备完成（如果还没通知过）
                        listener?.onPrepared(dur.toLong())

                        // 如果是 Surface 切换后的 reprepare，自动恢复播放位置
                        if (shouldAutoResume && savedPos >= 0) {
                            log(TAG,"PREPARING Monitor [方案B]: 自动恢复播放 " +
                                    "(position=${formatTime(savedPos)})")
                            
                            // Monitor 检测到 isPlaying=true，只需 seekTo
                            seekTo(savedPos.toLong())
                            log(TAG,"PREPARING Monitor [方案B]: 已恢复播放 " +
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
                log(TAG,"PREPARING Monitor: 超时，仍处于 PREPARING 状态")
            }
        }
    }

    /**
     * 停止 PREPARING 状态监控
     */
    private fun stopPreparingMonitor() {
        if (preparingMonitorJob != null) {
            log(TAG,"stopping PREPARING state monitor")
            preparingMonitorJob?.cancel()
            preparingMonitorJob = null
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
            SurfaceType.SURFACE_VIEW -> VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, videoScaleMode)
            SurfaceType.TEXTURE_VIEW -> VideoHelper.adjustRenderViewLayout(textureView, videoWidth, videoHeight, videoScaleMode)
            else -> {}
        }
    }
}
