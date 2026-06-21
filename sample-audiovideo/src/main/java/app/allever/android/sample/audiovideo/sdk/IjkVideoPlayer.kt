package app.allever.android.sample.audiovideo.sdk

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.android.base.IPlayerKernal
import app.allever.android.sample.audiovideo.android.base.IjkPlayerKernal
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.File

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
class IjkVideoPlayer {
    
    //TAG
    private val TAG = IjkVideoPlayer::class.java.simpleName

    // ==================== 内部组件 ====================

    /** IjkMediaPlayer 实例 */
//    private var ijkMediaPlayer: IjkMediaPlayer? = null
    private val engine: IPlayerKernal<*> = IjkPlayerKernal().apply { 
        registerListener(object : IPlayerKernal.IListener {
            override fun onPrepared() {
               log(TAG, "onPrepared (current state: $_state)")

                // 停止 PREPARING 状态监控（onPrepared 已到达）
                stopPreparingStateMonitor()

                // 状态检查：只处理 PREPARING 状态（防止重复回调）
                // 但如果已经是 PLAYING（竞态情况），也允许通过以触发 onPrepared 回调
                if (_state != PlayerState.PREPARING && _state != PlayerState.PLAYING) {
                   log(TAG, "onPrepared ignored: state=$_state")
                    return
                }

                val dur = duration

                // 只有在非 PLAYING 状态时才更新为 PREPARED
                if (_state != PlayerState.PLAYING) {
                    _state = PlayerState.PREPARED
                }

                // 检查是否需要自动恢复播放（Surface 切换后）
                val shouldAutoResume = switchSurfacePendingPosition >= 0
                val savedPos = switchSurfacePendingPosition
                switchSurfacePendingPosition = -1L  // 重置标记

                listener?.onPrepared(dur)
               log(TAG, "onPrepared (duration=${dur}ms, autoResume=$shouldAutoResume)")

                // 如果是 Surface 切换后的 reprepare，自动恢复播放
                if (shouldAutoResume && savedPos >= 0) {
                   log(TAG, "safeSwitchSurface [方案B]: 自动恢复播放 (position=${formatTime(savedPos)})")
                    App.mainHandler.post {
                        seekToInternal(savedPos)
                        play()
                       log(TAG, "safeSwitchSurface [方案B]: 已恢复播放 (${formatTime(savedPos)})")
                    }
                }

                // ✨✨✨ 关键修复：主动获取视频尺寸（备用方案）
                // IJKPlayer 的 OnVideoSizeChangedListener 可能不回调，
                // 因此在 onPrepared 后立即尝试获取视频尺寸并触发自适应
                tryFetchVideoSizeAndAdjustLayout()

                // 如果有缓存的 seekTo，在 prepared 后执行
                if (pendingSeekPosition > 0) {
                    val pos = pendingSeekPosition
                    pendingSeekPosition = 0
                    seekToInternal(pos)
                }
            }

            override fun onCompletion() {
                log(TAG, "onCompletion")
                stopProgressTracking()

                when (loopMode) {
                    LoopMode.SINGLE -> {
                        // 单曲循环：重新开始播放
                        seekTo(0)
                        startProgressTracking()
                        listener?.onLoopRestart()
                    }
                    LoopMode.ALL -> {
                        // 列表循环：通知上层切换下一个
                        _state = PlayerState.COMPLETED
                        listener?.onComplete()
                    }
                    LoopMode.NONE -> {
                        // 不循环
                        _state = PlayerState.COMPLETED
                        listener?.onComplete()
                    }
                }
            }

            override fun onError(code: Int, msg: String) {
                log(TAG, "onError: $code, $msg")
                if (_state == PlayerState.PREPARING) {
                    handlePrepareError(Exception("prepare error: $msg"))
                } else {
                    _state = PlayerState.ERROR
                    listener?.onError(code, msg)
                }
            }

            override fun onBufferingUpdate(percent: Int) {
//                log(TAG, "onBufferingUpdate: $percent")
                listener?.onBufferingUpdate(percent)
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                log(TAG, "onVideoSizeChanged: ${width}x${height}")
                if (width > 0 && height > 0) {
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
                log(TAG, "onIsPlayingChanged: $isPlaying")
            }

        })
    }

    /** 监听器回调 */
    private var listener: IVideoPlayerListener? = null

    /** Context 引用 */
    private val context: Context get() = App.context

    // ==================== Surface 绑定（两种模式）====================

    /** SurfaceView 绑定（推荐方式，性能好）*/
    private var surfaceView: SurfaceView? = null

    /** TextureView 绑定（高级方式，支持变换）*/
    private var textureView: TextureView? = null

    /** 当前绑定的 Surface 类型 */
    private enum class SurfaceType { NONE, SURFACE_VIEW, TEXTURE_VIEW }
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
               log(TAG, "state: $old -> $value")
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
            engine.volume(value)
        }

    /**
     * SurfaceView/TextureView 缩放模式（默认 FIT_CENTER）
     *
     * 仅对 SurfaceView 和 TextureView 生效。
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            adjustSurfaceLayout()
        }



    // ==================== 内部状态 ====================

    /** 进度追踪协程 */
    private var progressJob: Job? = null

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


    /** PREPARING 状态监控协程（防止状态不一致）*/
    private var preparingMonitorJob: Job? = null

    /**
     * 待执行的 prepare 参数（当 Surface 未就绪时缓存 setSource 调用）
     */
    private data class PendingPrepare(
        val uri: Uri,
        val headers: Map<String, String>?,
        val assetPath: String?
    )
    private var pendingPrepare: PendingPrepare? = null

    /** 切换 Surface 后待恢复的播放位置（-1 表示无需恢复） */
    private var switchSurfacePendingPosition: Long = -1L

    /** 是否正在执行 safeSwitchSurface（防止重复调用） */
    @Volatile
    private var isSafeSwitching: Boolean = false

    // ==================== 监听器实例（避免重复创建）====================
    

    private var pendingSeekPosition: Long = 0
    

    // ==================== Surface 绑定 ====================

    /**
     * 绑定 SurfaceView 用于视频渲染（推荐方式）
     *
     * 特点：
     * - 性能优异，独立窗口渲染
     * - Surface 创建是异步的，需要等待 surfaceCreated 回调
     * - 不支持变换（旋转/缩放/透明度）
     * - 适用于大多数场景
     *
     * @param surfaceView 外部传入的 SurfaceView 实例
     */
    fun attach(surfaceView: SurfaceView) {
        detach()

        this.surfaceView = surfaceView
        this.currentSurfaceType = SurfaceType.SURFACE_VIEW
        this.isSurfaceReady = false

        setupSurfaceCallback(surfaceView)

       log(TAG, "attach SurfaceView")
    }

    /**
     * 绑定 TextureView 用于视频渲染（高级方式）
     *
     * 特点：
     * - 支持矩阵变换（旋转/缩放/透明度）
     * - Surface 通常立即可用
     * - 性能略低于 SurfaceView
     * - 适用于需要特效或动画的场景
     *
     * @param textureView 外部传入的 TextureView 实例
     */
    fun attach(textureView: TextureView) {
        detach()

        this.textureView = textureView
        this.currentSurfaceType = SurfaceType.TEXTURE_VIEW
        this.isSurfaceReady = false

        setupTextureCallback(textureView)

       log(TAG, "attach TextureView")
    }

    /**
     * 解绑当前 Surface 并清理资源
     *
     * 注意：此方法不会释放 IjkMediaPlayer，
     * 仅解绑 Surface 以便后续重新绑定或切换 Surface 类型。
     */
    fun detach() {
       log(TAG, "detach")

        // 移除 Surface 回调
        surfaceView?.holder?.removeCallback(surfaceHolderCallback)
        textureView?.surfaceTextureListener = null

        // 清空绑定
        surfaceView = null
        textureView = null
        currentSurfaceType = SurfaceType.NONE
        isSurfaceReady = false

        // 从 IjkMediaPlayer 移除 Surface
        try {
            engine.setSurface(null)
        } catch (e: Exception) {
           log(TAG, "detach error: ${e.message}")
        }
    }

    // ==================== 安全的 Surface 切换 API ====================

    /**
     * 安全地切换 Surface（推荐使用此方法）
     *
     * **解决 MediaCodec 状态机竞态条件问题：**
     *
     * 问题背景：
     * 在播放过程中直接调用 detach() + attach() 可能导致解码器异常。
     *
     * 解决方案（方案 B：stop → 切换 → reprepare）：
     * 1. stop() 完全停止 IjkMediaPlayer（清空所有缓冲区）
     * 2. detach + attach 安全切换 Surface
     * 3. 使用保存的数据源重新 prepare
     * 4. 在 onPrepared 回调中恢复播放位置并继续播放
     */

    /**
     * 安全切换到 SurfaceView
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
     */
    private fun safeSwitchSurface(
        targetAction: () -> Unit,
        targetName: String,
        delayMs: Long = 100L
    ) {
        // ✨ 防重入检查：如果正在切换，忽略重复调用
        if (isSafeSwitching) {
           log(TAG, "safeSwitchSurface [方案B]: ⚠️ 忽略重复调用（正在切换到 $targetName）")
            return
        }

        // 标记开始切换
        isSafeSwitching = true

        try {
            // 1. 记录当前状态
            val wasPlaying = (_state == PlayerState.PLAYING || _state == PlayerState.PAUSED ||
                    _state == PlayerState.PREPARING)  // ✅ 扩展：包含 PREPARING 状态
            val savedPosition = currentPosition

           log(TAG, "safeSwitchSurface [方案B]: 开始切换到 $targetName" +
                    " (wasPlaying=$wasPlaying, position=${formatTime(savedPosition)}, state=$_state)")

            // 2. 完全停止 IjkMediaPlayer（清空所有缓冲区和渲染队列）
            if (_state != PlayerState.IDLE && _state != PlayerState.STOPPED && _state != PlayerState.RELEASED) {
                stop()
               log(TAG, "safeSwitchSurface [方案B]: 已 stop()，清空所有缓冲区")
            }

            // 3. 如果需要恢复播放，保存位置信息（扩展条件：PREPARING 状态也保存）
            if (wasPlaying && savedPosition >= 0 && (currentUri != null || currentAssetPath != null)) {
                switchSurfacePendingPosition = savedPosition
               log(TAG, "safeSwitchSurface [方案B]: 待恢复位置 ${formatTime(savedPosition)}")
            }

            // 4. 使用 postDelayed 延迟执行切换操作
            App.mainHandler.postDelayed({
                try {
               log(TAG, "safeSwitchSurface [方案B]: 执行切换到 $targetName")

                // 执行实际的切换操作（detach + attach）
                targetAction()

                // 5. 重新准备数据源（因为已经 stop()，必须 reprepare）
                if (currentUri != null || currentAssetPath != null) {
                   log(TAG, "safeSwitchSurface [方案B]: 重新 prepare 数据源" +
                            " (autoResume=${switchSurfacePendingPosition >= 0})")

                    // ✨ 关键修复：根据数据源类型选择正确的 prepare 方式
                    // 原因：attach() 后 isSurfaceReady=false，如果走 doSetSource 会被缓存到 pendingPrepare
                    // 而 surfaceCreated/surfaceTextureAvailable 可能不会再次回调（Surface 已存在）
                    // 导致 onPrepared 永远不会被调用！
                    if (currentAssetPath != null && currentAssetPath!!.isNotEmpty()) {
                        // Assets 数据源：必须手动处理文件复制 + prepare
                        // 不能直接调用 setAssetSource()，因为它内部会调用 doSetSource()
                        // 而 doSetSource() 会检查 isSurfaceReady，导致 prepare 被缓存不执行
                       log(TAG, "safeSwitchSurface [方案B]: 使用 setAssetSource (assets)")

                        try {
                            val cacheFile = File(context.cacheDir, "asset_video_${currentAssetPath!!.hashCode()}")

                            // 如果缓存文件不存在，从 assets 重新复制
                            if (!cacheFile.exists()) {
                                context.assets.open(currentAssetPath!!).use { input ->
                                    cacheFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                               log(TAG, "safeSwitchSurface [方案B]: 已重新复制 asset 文件")
                            }

                            // 直接使用 doPrepareInternal，跳过 isSurfaceReady 检查
                            val cacheUri = Uri.fromFile(cacheFile)
                            currentUri = cacheUri
                            doPrepareInternal(cacheUri, currentHeaders)

                        } catch (e: Exception) {
                           log(TAG, "safeSwitchSurface [方案B]: Asset 处理失败 - ${e.message}")
                            switchSurfacePendingPosition = -1L
                            _state = PlayerState.ERROR
                            listener?.onError(PlayerErrorCode.ASSET_COPY_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.ASSET_COPY_FAILED, e.message))
                        }

                    } else if (currentUri != null) {
                        // 普通 URI（HTTP/本地文件/Content Provider）：直接 prepare
                        // 跳过 isSurfaceReady 检查，确保立即触发 onPrepared
                       log(TAG, "safeSwitchSurface [方案B]: 使用 doPrepareInternal (uri=$currentUri)")
                        doPrepareInternal(currentUri!!, currentHeaders)
                    }

                    // 注意：
                    // - 如果 switchSurfacePendingPosition >= 0（之前在播放），
                    //   onPrepared 回调会自动 seekTo + play
                    // - 如果 switchSurfacePendingPosition < 0（之前未播放），
                    //   仅 reprepare，不自动播放，等待用户操作
                } else {
                   log(TAG, "safeSwitchSurface [方案B]: 切换完成（无数据源）")
                }
            } catch (e: Exception) {
               log(TAG, "safeSwitchSurface [方案B]: 切换失败 - ${e.message}")
                switchSurfacePendingPosition = -1L  // 重置
                _state = PlayerState.ERROR
                listener?.onError(PlayerErrorCode.SURFACE_SWITCH_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.SURFACE_SWITCH_FAILED, e.message))
            } finally {
                // ✨ 无论成功失败，都重置切换标志，允许下次切换
                isSafeSwitching = false
               log(TAG, "safeSwitchSurface [方案B]: 切换流程结束")
            }
        }, delayMs)

        } catch (e: Exception) {
            // 外层异常：在 stop() 或保存状态时出错
           log(TAG, "safeSwitchSurface [方案B]: 准备阶段失败 - ${e.message}")
            isSafeSwitching = false  // 重置标志
            switchSurfacePendingPosition = -1L
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message))
        }
    }

    /**
     * 设置 SurfaceHolder 回调（用于 SurfaceView）
     */
    private fun setupSurfaceCallback(sv: SurfaceView) {
        sv.holder.addCallback(surfaceHolderCallback)

        // 如果 Surface 已经可用（例如复用的情况）
        if (sv.holder.surface.isValid) {
            isSurfaceReady = true
            bindSurface(sv.holder.surface)
            executePendingPrepare()
        }
    }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
           log(TAG, "surfaceCreated")
            isSurfaceReady = true
            bindSurface(holder.surface)
            executePendingPrepare()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
           log(TAG, "surfaceChanged: ${width}x${height}")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
           log(TAG, "surfaceDestroyed")
            isSurfaceReady = false
            // Surface 销毁时移除绑定，防止崩溃
            try {
                engine.setSurface(null)
            } catch (e: Exception) {
               log(TAG, "surfaceDestroyed error: ${e.message}")
            }
        }
    }

    /**
     * 设置 SurfaceTexture 回调（用于 TextureView）
     */
    private fun setupTextureCallback(tv: TextureView) {
        tv.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
               log(TAG, "surfaceTextureAvailable: ${width}x${height}")
                isSurfaceReady = true
                bindSurface(Surface(surface))
                executePendingPrepare()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
               log(TAG, "onSurfaceTextureSizeChanged: ${width}x${height}")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
               log(TAG, "onSurfaceTextureDestroyed")
                isSurfaceReady = false
                try {
                    engine.setSurface(null)
                } catch (e: Exception) {
                   log(TAG, "onSurfaceTextureDestroyed error: ${e.message}")
                }
                return true  // 返回 true 表示我们已处理释放
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // 每帧更新时回调，通常不需要处理
            }
        }

        // 如果 SurfaceTexture 已经可用
        if (tv.isAvailable && tv.surfaceTexture != null) {
            isSurfaceReady = true
            bindSurface(Surface(tv.surfaceTexture))
            executePendingPrepare()
        }
    }

    /**
     * 将 Surface 绑定到 IjkMediaPlayer
     */
    private fun bindSurface(surface: Surface?) {
        try {
            engine.setSurface(surface)
           log(TAG, "bindSurface: success")
        } catch (e: Exception) {
           log(TAG, "bindSurface error: ${e.message}")
        }
    }

    /**
     * 执行待处理的 prepare 操作（Surface 就绪后调用）
     */
    private fun executePendingPrepare() {
        val pending = pendingPrepare ?: return
        pendingPrepare = null

       log(TAG, "executePendingPrepare")

        doSetSource(pending.uri, pending.headers, pending.assetPath)
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
     * 由于 IjkMediaPlayer 无法直接读取 Assets 中的文件，
     * 此方法会将文件复制到内部缓存目录后再加载。
     *
     * @param path Assets 中的相对路径（如 "video/test.mp4"）
     */
    fun setAssetSource(path: String) {
        try {
            val cacheFile = File(context.cacheDir, "asset_video_${path.hashCode()}")

            // 如果缓存文件不存在或需要更新，重新复制
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
           log(TAG, "Surface 未就绪，缓存 prepare 操作")
            pendingPrepare = PendingPrepare(uri, headers, assetPath)
            return
        }

        // 执行实际的 prepare
        doPrepareInternal(uri, headers)
    }

    /**
     * 执行内部 prepare 操作
     */
    private fun doPrepareInternal(uri: Uri, headers: Map<String, String>?) {
       log(TAG, "doPrepareInternal: $uri")

        try {
            _state = PlayerState.PREPARING

            // 重置 IjkMediaPlayer
            engine.reset()
            engine.setSource(uri, headers)

            // 绑定当前 Surface
            when (currentSurfaceType) {
                SurfaceType.SURFACE_VIEW -> {
//                    surfaceView?.holder?.let { engine.setDisplay(it) }
                    surfaceView?.holder?.let { engine.setSurface(it.surface) }
                }
                SurfaceType.TEXTURE_VIEW -> {
                    textureView?.let { engine.setSurface(Surface(it.surfaceTexture)) }
                }
                else -> {}
            }

            // 应用当前参数 
            engine.volume(volume)
            engine.speed(speed)
            engine.loopMode(loopMode)

            // 异步准备
            engine.prepareAsync()

            // ✨ 关键修复：启动 PREPARING 状态监控协程
            // 防止 IJKPlayer 异步特性导致状态不一致：
            // - IjkMediaPlayer 可能在 onPrepared 回调前就开始播放
            //# - 此时需要主动检测并修正状态
            startPreparingStateMonitor()

        } catch (e: Exception) {
           log(TAG, "doPrepareInternal error: ${e.message}")
            handlePrepareError(e)
        }
    }

    /**
     * 处理 prepare 阶段的错误（可能触发重试）
     */
    private fun handlePrepareError(error: Exception) {
       log(TAG, "handlePrepareError: ${error.message}, retryLeft=$retryLeft")

        if (retryLeft > 0) {
            retryLeft--
           log(TAG, "正在重试... ($retryLeft 次剩余)")

            App.mainHandler.postDelayed({
                if (_state != PlayerState.RELEASED) {
                    currentUri?.let { doPrepareInternal(it, currentHeaders) }
                }
            }, 1000)  // 1 秒后重试
        } else {
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.RETRY_EXHAUSTED, "Retry attempts exhausted")
        }
    }

    // ==================== 播放控制 ====================

    /**
     * 开始或继续播放
     *
     * 行为逻辑：
     * - PREPARED/PAUSED/COMPLETED 状态：从当前位置继续播放
     * - PREPARING 状态：尝试播放（防止异步竞态导致状态不一致）
     * - 其他状态：不执行任何操作（需先 setSource + wait for PREPARED）
     */
    fun play() {
       log(TAG, "play (state=$_state)")

        when (_state) {
            PlayerState.PREPARED,
            PlayerState.PAUSED,
            PlayerState.COMPLETED -> {
                doPlay()
            }
            PlayerState.PREPARING -> {
                // 防止竞态：IjkMediaPlayer 可能已经准备好但回调还未触发
                // 尝试直接 start()，如果成功则更新状态
                try {
                    val isPrepared = try {
                        // 通过 duration > 0 判断是否已准备就绪
                        engine.getDuration() > 0
                    } catch (_: Exception) { false }

                    if (isPrepared) {
                       log(TAG, "play: 检测到已准备就绪，强制播放")
                        _state = PlayerState.PREPARED  // 先修正状态
                        doPlay()
                    } else {
                       log(TAG, "play ignored: 正在准备中...")
                    }
                } catch (e: Exception) {
                   log(TAG, "play (PREPARING) error: ${e.message}")
                }
            }
            else -> {
               log(TAG, "play ignored: state=$_state")
            }
        }
    }

    /**
     * 执行实际的播放操作
     *
     * 修复说明：
     * - IjkMediaPlayer.start() 是异步的，调用后 isPlaying 可能不会立即返回 true
     * - 因此不能依赖 isPlaying 的即时返回值来判断是否成功
     * - 应该在调用 start() 后立即更新状态为 PLAYING（信任调用成功）
     * - 如果后续发现实际未播放，由 PREPARING Monitor 或 Progress Sync 协程修正
     *
     * 重要修复（IJKPlayer 已知 bug）：
     * - IJKPlayer 在 pause() 后调用 start() 可能导致播放位置重置为 0
     * - 因此需要在 start() 前保存当前播放位置，并在 start() 后验证是否需要恢复
     */
    private fun doPlay() {
        try {
            // 记录调用前的状态（用于日志）
            val previousState = _state

            // ✨ 关键修复：保存当前播放位置（防止 IJKPlayer 重置位置 bug）
            var savedPosition: Long = -1L
            if (previousState == PlayerState.PAUSED) {
                try {
                    savedPosition = engine.getCurrentPosition()
                   log(TAG, "doPlay: 保存暂停位置 = ${savedPosition}ms")
                } catch (_: Exception) {
                    savedPosition = -1L
                }
            }

            // 调用 start() 开始播放
            engine.start()

            // ✨ 关键修复：立即更新状态为 PLAYING
            // 不要依赖 isPlaying 的即时返回值（可能是异步的）
            _state = PlayerState.PLAYING
            listener?.onStateChanged(previousState, PlayerState.PLAYING)

            // 启动进度追踪协程
            startProgressTracking()

            // ✨ 关键修复：播放开始后再次尝试获取视频尺寸
            // 某些视频格式在 onPrepared 时无法获取正确尺寸，
            // 需要等到真正开始播放后才能获取到
            if (videoWidth <= 0 || videoHeight <= 0) {
               log(TAG, "doPlay: 视频尺寸为空 (${videoWidth}x${videoHeight})，尝试主动获取")
                tryFetchVideoSizeAndAdjustLayout()
            }

           log(TAG, "doPlay: $previousState -> PLAYING (已调用 start())")

            // ✨✨✨ 核心修复：延迟检查并恢复播放位置
            // IJKPlayer 的 start() 是异步的，可能在几毫秒后才真正开始播放
            // 此时 currentPosition 可能暂时返回 0 或错误值
            // 需要延迟一小段时间后再检查并恢复位置
            if (savedPosition > 0 && previousState == PlayerState.PAUSED) {
                App.mainHandler.postDelayed({
                    try {
                        if (_state == PlayerState.PLAYING) {
                            val currentPosition = engine.getCurrentPosition()

                            // 如果当前位置明显小于保存的位置（误差 > 500ms），说明位置被重置了
                            if (currentPosition < savedPosition - 500) {
                               log(TAG, "⚠️ doPlay: 检测到位置重置！" +
                                        " 保存=${savedPosition}ms, 当前=${currentPosition}ms, 正在恢复...")

                                // seekTo 到保存的位置
                                engine.seekTo(savedPosition)

                               log(TAG, "✅ doPlay: 已恢复位置到 ${savedPosition}ms")
                            } else {
                               log(TAG, "doPlay: 位置正常，保存=${savedPosition}ms, 当前=${currentPosition}ms")
                            }
                        }
                    } catch (_: Exception) {
                        // 忽略异常
                    }
                }, 100)  // 延迟 100ms 检查（给 IJKPlayer 足够时间启动）
            }

        } catch (e: Exception) {
           log(TAG, "doPlay error: ${e.message}")
            // 如果 start() 抛出异常，回退到之前的状态或标记错误
            _state = PlayerState.ERROR
            listener?.onError(PlayerErrorCode.IJK_MEDIA_PLAYER_INTERNAL_ERROR, PlayerErrorCode.formatError(PlayerErrorCode.IJK_MEDIA_PLAYER_INTERNAL_ERROR, e.message))
        }
    }

    /**
     * 暂停播放
     *
     * 仅在 PLAYING 状态下有效。
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
     * 停止播放
     *
     * 停止后会回到初始状态，需要重新 setSource + play 才能再次播放。
     */
    fun stop() {
       log(TAG, "stop (state=$_state)")

        try {
            stopProgressTracking()
            engine.stop()
            _state = PlayerState.IDLE
        } catch (e: Exception) {
           log(TAG, "stop error: ${e.message}")
        }
    }

    /**
     * 跳转到指定位置（毫秒）
     *
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
       log(TAG, "seekTo: $positionMs ms (state=$_state)")

        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return

        // 标记正在 seeking
        isSeeking = true

        // 暂停进度追踪（seek 过程中位置不稳定）
        stopProgressTracking()

        when (_state) {
            PlayerState.PREPARING -> {
                // 准备阶段，缓存 seek 位置，prepared 后执行
                pendingSeekPosition = positionMs
            }
            PlayerState.PREPARED,
            PlayerState.PLAYING,
            PlayerState.PAUSED,
            PlayerState.COMPLETED -> {
                seekToInternal(positionMs)
            }
            else -> {}
        }
    }

    /**
     * 内部 seek 实现
     */
    private fun seekToInternal(positionMs: Long) {
        try {
            engine.seekTo(positionMs)
           log(TAG, "seekToInternal: $positionMs ms")

            // IjkMediaPlayer 会通过 OnSeekCompleteListener 通知完成, 没回调OnSeekCompleteListener
            // 此时不需要立即恢复进度追踪，等待回调即可

            // 延迟重置标志并确保进度追踪正常运行（seek 是异步操作）
            //没回调OnSeekCompleteListener，加上
            App.mainHandler.postDelayed({
                isSeeking = false
                // 确保 seek 完成后进度追踪仍在运行
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    log("ExoVideoPlayer", "restart progress tracking after seek")
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
           log(TAG, "seekToInternal error: ${e.message}")
            isSeeking = false

            // 出错时恢复进度追踪
            if (_state == PlayerState.PLAYING) {
                startProgressTracking()
            }
        }
    }

    // ==================== 监听器设置 ====================

    /**
     * 设置播放事件监听器
     *
     * @param listener 监听器实现
     */
    fun setListener(listener: IVideoPlayerListener) {
        this.listener = listener
    }

    // ==================== 进度追踪 ====================

    /**
     * 启动进度追踪协程
     *
     * 定时获取 IjkMediaPlayer 的当前位置和总时长，通过监听器回调。
     * 同时会定期同步 IjkMediaPlayer 实际播放状态，防止状态不一致。
     */
    private fun startProgressTracking() {
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
                    val d = engine.getDuration()
                    if (d < 0) 0L else d
                } catch (_: Exception) { 0L }

                if (pos >= 0 && dur > 0) {
                    listener?.onProgress(pos, dur)
                }

                // 定期同步状态和视频尺寸（每5次检查一次，约1秒）
                if ((System.currentTimeMillis() / progressIntervalMs) % 5 == 0L) {
                    syncStateWithPlayer()

                    // ✨ 定期检查视频尺寸（如果还未获取到）
                    if (videoWidth <= 0 || videoHeight <= 0) {
                        tryFetchVideoSizeAndAdjustLayout()
                    }
                }

                delay(progressIntervalMs.toLong())
            }
        }
    }

    /**
     * 停止进度追踪协程
     */
    private fun stopProgressTracking() {
        if (progressJob != null) {
           log(TAG, "stopping progress tracking")
            progressJob?.cancel()
            progressJob = null
        }
    }

    // ==================== PREPARING 状态监控（核心修复）====================

    /**
     * 启动 PREPARING 状态监控协程
     *
     * **核心修复：解决"已经在播放了，状态还是 PREPARING"的 bug**
     *
     * 问题根源：
     * IjkMediaPlayer 使用 prepareAsync() 异步准备，
     * 某些情况下（特别是本地文件或缓存良好的在线视频），
     * IjkMediaPlayer 可能在 onPrepared 回调到达之前就已经开始播放。
     *
     * 此时会出现状态不一致：
     * - IjkMediaPlayer 内部状态：PLAYING（正在播放）
     * - 我们的状态变量：PREPARING（还在等待 onPrepared）
     *
     * 此协程的作用：
     * 在 PREPARING 状态下定期检测 IjkMediaPlayer 的实际播放状态，
     * 如果发现已经在播放，立即修正我们的状态为 PLAYING/PAUSED/COMPLETED 等。
     *
     * 调用时机：doPrepareInternal() 中 prepareAsync() 之后立即启动
     * 停止时机：状态变为非 PREPARING 时自动停止
     */
    private fun startPreparingStateMonitor() {
        stopPreparingStateMonitor()

       log(TAG, "starting PREPARING state monitor")

        preparingMonitorJob = CoroutineScope(Dispatchers.Main).launch {
            var checkCount = 0
            val maxCheckTime = 30000L  // 最大监控时间 30 秒
            val startTime = System.currentTimeMillis()

            while (isActive && _state == PlayerState.PREPARING) {
                checkCount++

                // 每 200ms 检查一次（与 progressIntervalMs 一致）
                try {
                    val actualIsPlaying = engine.isPlaying()

                    if (actualIsPlaying) {
                        // ✨ 检测到 IjkMediaPlayer 正在播放！
                       log(TAG, "⚡ PREPARING Monitor: 检测到正在播放！修正状态")

                        // 获取视频信息
                        val dur = try {
                            engine.getDuration()
                        } catch (_: Exception) { 0L }

                        val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }

                        if (dur > 0) {
                            // 有 duration → 已准备就绪
                           log(TAG, "PREPARING Monitor: duration=$dur, position=$pos")

                            // ✨ 关键修复：检查是否需要自动恢复播放（Surface 切换后）
                            // 原因：IJKPlayer 可能不触发 onPrepared 回调，
                            // 此时 PREPARING Monitor 会代替 onPrepared 完成状态转换，
                            // 因此必须在这里也处理位置恢复逻辑！
                            val shouldAutoResume = switchSurfacePendingPosition >= 0
                            val savedPos = switchSurfacePendingPosition
                            switchSurfacePendingPosition = -1L  // 重置标记

                            // 通知准备完成（如果还没通知过）
                            listener?.onPrepared(dur)

                           log(TAG, "PREPARING Monitor: autoResume=$shouldAutoResume" +
                                    (if (shouldAutoResume) ", savedPosition=${formatTime(savedPos)}" else ""))

                            // 如果是 Surface 切换后的 reprepare，自动恢复播放位置
                            if (shouldAutoResume && savedPos >= 0) {
                               log(TAG, "PREPARING Monitor [方案B]: 自动恢复播放 " +
                                        "(position=${formatTime(savedPos)})")
                                // 注意：此时播放器已经在播放（isPlaying=true）
                                // 必须立即 seekTo 到保存的位置
                                seekToInternal(savedPos)
                               log(TAG, "PREPARING Monitor [方案B]: 已恢复播放 " +
                                        "(${formatTime(savedPos)})")
                            }

                            // 更新为正确状态
                            _state = PlayerState.PLAYING
                            listener?.onStateChanged(PlayerState.PREPARING, PlayerState.PLAYING)

                            // 启动进度追踪
                            startProgressTracking()

                            return@launch  // 任务完成，退出监控
                        } else {
                            // 还没有 duration，继续等待
                           log(TAG, "PREPARING Monitor: isPlaying=true 但 duration=0，继续等待")
                        }
                    } else {
                        // 检查是否已经 prepared 但没在播放
                        val dur = try {
                            engine.getDuration()
                        } catch (_: Exception) { 0L }

                        if (dur > 0 && checkCount > 5) {
                            // duration > 0 且检查超过 1 秒 → 视为已准备好但未开始播放
                           log(TAG, "PREPARING Monitor: 检测到已准备就绪(duration=$dur)，更新状态为 PREPARED")
                            _state = PlayerState.PREPARED
                            listener?.onStateChanged(PlayerState.PREPARING, PlayerState.PREPARED)
                            listener?.onPrepared(dur)

                            return@launch  // 任务完成，退出监控
                        }
                    }

                    // 超时保护：30秒后停止监控（防止无限运行）
                    if (System.currentTimeMillis() - startTime > maxCheckTime) {
                       log(TAG, "PREPARING Monitor: 超时(30s)，停止监控")
                        return@launch
                    }

                } catch (_: Exception) {
                    // 忽略异常，继续监控
                }

                delay(progressIntervalMs.toLong())  // 200ms 检查一次
            }

           log(TAG, "PREPARING state monitor stopped (state=$_state)")
        }
    }

    /**
     * 停止 PREPARING 状态监控协程
     */
    private fun stopPreparingStateMonitor() {
        if (preparingMonitorJob != null) {
           log(TAG, "stopping PREPARING state monitor")
            preparingMonitorJob?.cancel()
            preparingMonitorJob = null
        }
    }

    /**
     * 同步内部状态与 IjkMediaPlayer 实际状态
     *
     * 防止因异步操作或竞态条件导致的状态不一致。
     * 例如：IjkMediaPlayer 已经在播放，但我们的状态还是 PREPARING。
     */
    private fun syncStateWithPlayer() {
        try {
            val actualIsPlaying = engine.isPlaying()

            if (actualIsPlaying && _state != PlayerState.PLAYING) {
                // IjkMediaPlayer 正在播放但我们的状态不是 PLAYING → 修正状态
               log(TAG, "syncState: 检测到不一致，修正: $_state -> PLAYING")
                _state = PlayerState.PLAYING
                listener?.onStateChanged(_state, PlayerState.PLAYING)
            } else if (!actualIsPlaying && _state == PlayerState.PLAYING) {
                // IjkMediaPlayer 已停止播放但我们的状态还是 PLAYING → 修正状态
                val pos = try { engine.getCurrentPosition() } catch (_: Exception) { 0L }
                val dur = try {
                    engine.getDuration()
                } catch (_: Exception) { 0L }

                if (pos > 0 && dur > 0 && pos >= dur - 100) {
                    // 接近末尾（误差100ms），视为播放完成
                   log(TAG, "syncState: 检测到播放完成")
                    _state = PlayerState.COMPLETED
                    listener?.onStateChanged(PlayerState.PLAYING, PlayerState.COMPLETED)
                    listener?.onComplete()
                } else {
                    // 其他原因停止，视为暂停
                   log(TAG, "syncState: 检测到暂停")
                    _state = PlayerState.PAUSED
                    listener?.onStateChanged(PlayerState.PLAYING, PlayerState.PAUSED)
                }
            }
        } catch (_: Exception) {
            // 忽略同步过程中的异常
        }
    }

    // ==================== 内部：视频尺寸获取与自适应 ====================

    /**
     * 主动获取视频尺寸并触发画面自适应（备用方案）
     *
     * **核心修复：解决 OnVideoSizeChangedListener 不回调的问题**
     *
     * IJKPlayer 的已知问题：
     * - setOnVideoSizeChangedListener 在某些情况下不回调
     * - 特别是对于某些视频格式或网络视频
     *
     * 解决方案：
     * - 在 onPrepared 后主动调用此方法
     * - 通过 IjkMediaPlayer.getVideoWidth()/getVideoHeight() 获取尺寸
     * - 如果获取失败，延迟重试最多 5 次
     */
    private fun tryFetchVideoSizeAndAdjustLayout(retryCount: Int = 0) {
        val maxRetries = 5

        try {
            // ✨ 主动从 IjkMediaPlayer 获取视频尺寸
            val w = engine.getVideoWidth()
            val h = engine.getVideoHeight()

           log(TAG, "tryFetchVideoSize: 尝试 #$retryCount, size=${w}x${h}")

            if (w > 0 && h > 0) {
                // ✅ 成功获取到有效尺寸

                // 检查是否与当前记录的尺寸不同（避免重复调整）
                if (w != videoWidth || h != videoHeight) {
                   log(TAG, "✨ 主动获取到视频尺寸: ${videoWidth}x${videoHeight} -> ${w}x${h}")

                    videoWidth = w
                    videoHeight = h

                    // 通知监听器
                    listener?.onVideoSizeChanged(w, h)

                    // 触发画面自适应
                    adjustSurfaceLayout()
                } else {
                   log(TAG, "视频尺寸未变化: ${w}x${h}")
                }

                return  // 成功，不需要重试
            } else {
                // ❌ 尺寸无效，需要重试
                if (retryCount < maxRetries) {
                   log(TAG, "视频尺寸无效 (${w}x${h})，将在 ${(retryCount + 1) * 200}ms 后重试...")

                    App.mainHandler.postDelayed({
                        tryFetchVideoSizeAndAdjustLayout(retryCount + 1)
                    }, (retryCount + 1) * 200L)  // 渐进式延迟：200ms, 400ms, 600ms...
                } else {
                   log(TAG, "⚠️ 已重试 $maxRetries 次仍无法获取视频尺寸")
                }
            }

        } catch (_: Exception) {
            // 异常时也尝试重试
            if (retryCount < maxRetries) {
               log(TAG, "获取视频尺寸异常，重试中...")
                App.mainHandler.postDelayed({
                    tryFetchVideoSizeAndAdjustLayout(retryCount + 1)
                }, (retryCount + 1) * 200L)
            }
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
     * 仅对 SurfaceView 和 TextureView 生效。
     */
    private fun adjustSurfaceLayout() {
        when (currentSurfaceType) {
            SurfaceType.SURFACE_VIEW -> VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, videoScaleMode)
            SurfaceType.TEXTURE_VIEW -> VideoHelper.adjustRenderViewLayout(textureView, videoWidth, videoHeight, videoScaleMode)
            else -> {}
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
    
    // ==================== 生命周期管理 ====================

    /**
     * 释放所有资源
     *
     * 必须在不再使用时调用，否则会导致内存泄漏和资源占用。
     * 调用后此对象不可再使用。
     */
    fun release() {
       log(TAG, "release (state=$_state)")

        // 更新状态
        _state = PlayerState.RELEASED

        // 停止进度追踪
        stopProgressTracking()

        // 停止 PREPARING 状态监控
        stopPreparingStateMonitor()

        // 解绑 Surface
        detach()

        // 清除监听器
        engine.release()

        // 清空引用
        listener = null
        currentUri = null
        currentHeaders = null
        pendingPrepare = null
    }



    private val mOnSeekCompleteListener = IMediaPlayer.OnSeekCompleteListener {
        App.mainHandler.postDelayed({
            //没回调
           log(TAG, "onSeekComplete")

            isSeeking = false

            // Seek 完成后恢复进度追踪
            if (_state == PlayerState.PLAYING) {
                startProgressTracking()
            }
        }, 300)
    }



    private val mOnInfoListener = IMediaPlayer.OnInfoListener { _, what, extra ->
        App.mainHandler.post {
           log(TAG, "onInfo: what=$what, extra=$extra")

            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    // 视频开始渲染（首帧显示）
                   log(TAG, "视频渲染开始（首帧显示）")
                    listener?.onFirstFrameRendered()
                }
                IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> {
                    // 网络带宽信息（单位：bps）
                   log(TAG, "网络带宽: ${extra} bps")
                    listener?.onNetworkBandwidth(extra.toLong())
                }
            }
        }
        true
    }

}
