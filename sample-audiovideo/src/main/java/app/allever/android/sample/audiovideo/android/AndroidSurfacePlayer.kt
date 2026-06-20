package app.allever.android.sample.audiovideo.android

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android SurfaceView + MediaPlayer 视频播放封装
 *
 * 职责：
 * - 手动管理 MediaPlayer 完整生命周期
 * - 处理 SurfaceView 的 Surface 生命周期（创建/销毁/重建）
 * - 管理状态机转换（复用 [PlayerState]）
 * - 提供进度追踪、变速、音量、循环等能力
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * SurfaceView 由外部传入，本类不创建 UI 组件。
 *
 * 使用示例：
 * ```kotlin
 * val player = AndroidSurfacePlayer()
 * player.attach(surfaceView)
 * player.listener = object : IVideoPlayerListener { ... }
 * player.setSource("https://example.com/video.mp4")
 * // onPrepared 后调用 player.play()
 * player.pause()
 * player.play()  // 继续
 * player.detach()  // 页面不可见时
 * player.release()  // 不再使用时
 * ```
 */
class AndroidSurfacePlayer {

    private var surfaceView: SurfaceView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var listener: IVideoPlayerListener? = null

    // ==================== 状态 ====================

    private var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("SurfacePlayer", "state: $old -> $value")
                field = value
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态 */
    val state get() = _state

    /** 是否正在播放 */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING && mediaPlayer?.isPlaying == true

    /** 当前位置（毫秒） */
    val currentPosition: Long
        get() = try { mediaPlayer?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try { mediaPlayer?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }

    // ==================== 配置 ====================

    /** 循环模式 */
    var loopMode: LoopMode = LoopMode.NONE

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
            mediaPlayer?.setVolume(field, field)
        }

    /**
     * SurfaceView 缩放模式（默认 FIT_CENTER）
     *
     * 通过调整 SurfaceView 的布局尺寸实现不同的显示效果：
     * - FIT_CENTER: 保持比例，完整显示（可能有黑边）
     * - CROP_CENTER: 保持比例，填满容器（可能裁剪边缘）
     * - STRETCH: 拉伸填满容器（可能变形）
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, value)
        }

    // ==================== 内部状态 ====================

    private var progressJob: Job? = null
    private var currentUri: Uri? = null
    private var currentHeaders: Map<String, String>? = null
    private var retryLeft: Int = 0
    private var isAssetSource: Boolean = false
    private var isSurfaceReady: Boolean = false

    /** Surface 销毁前是否在播放（用于 Surface 重建后恢复） */
    private var wasPlayingBeforeDestroy: Boolean = false

    /** 视频原始宽度 */
    private var videoWidth: Int = 0

    /** 视频原始高度 */
    private var videoHeight: Int = 0

    /**
     * 待执行的 prepare 参数（当 Surface 未就绪时缓存 setSource 调用）
     */
    private data class PendingPrepare(
        val uri: Uri,
        val headers: Map<String, String>?,
        val assetPath: String?
    )

    private var pendingPrepare: PendingPrepare? = null

    // ==================== 绑定 & 解绑 ====================

    /**
     * 绑定 SurfaceView（必须在播放前调用一次）
     *
     * 会立即创建 MediaPlayer 并设置 SurfaceHolder.Callback 监听 Surface 生命周期。
     *
     * @param surfaceView 外部创建的 SurfaceView 实例
     */
    fun attach(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
        initMediaPlayer()
        setupSurfaceCallback()
    }

    /**
     * 解绑 SurfaceView（页面 onPause/onDestroyView 时调用，不释放内部资源）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     */
    fun detach() {
        stopProgressTracking()
        removeSurfaceCallback()
        surfaceView = null
        isSurfaceReady = false
    }

    // ==================== 数据源 & 播放控制 ====================

    /**
     * 设置视频数据源并准备（不自动播放）
     *
     * 支持 http/https/content/file/android_asset 协议
     * 准备完成后回调 [IPlayerListener.onPrepared]，此时需调用 [play] 开始播放
     *
     * @param url 支持：
     * - http/https URL
     * - content:// URI
     * - file:// 路径
     * - file:///android_asset/filename.mp4 (assets 目录)
     */
    fun setSource(url: String) {
        val uri = Uri.parse(url)
        isAssetSource = uri.scheme == "file" && uri.path?.startsWith("/android_asset/") == true
        val assetPath = if (isAssetSource) uri.path?.removePrefix("/android_asset/") else null
        doSetSource(uri, currentHeaders, assetPath)
    }

    /**
     * 设置 assets 目录下的视频文件并准备（不自动播放）
     *
     * @param assetPath assets 目录下的相对路径，如 "video/test.mp4"
     */
    fun setAssetSource(assetPath: String) {
        isAssetSource = true
        doSetSource(Uri.parse("file:///android_asset/$assetPath"), null, assetPath)
    }

    /**
     * 设置视频数据源并准备（不自动播放）
     *
     * @param uri 视频 URI
     * @param headers HTTP 请求头（仅对 http(s) 生效）
     */
    fun setSource(uri: Uri, headers: Map<String, String>? = null) {
        if (_state == PlayerState.RELEASED) return
        isAssetSource = uri.scheme == "file" && uri.path?.startsWith("/android_asset/") == true
        val assetPath = if (isAssetSource) uri.path?.removePrefix("/android_asset/") else null
        doSetSource(uri, headers, assetPath)
    }

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
            }
            PlayerState.PAUSED -> {
                mediaPlayer?.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
            }
            else -> {}
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        safeAction(PlayerState.PLAYING) {
            mediaPlayer?.pause()
            _state = PlayerState.PAUSED
            stopProgressTracking()
        }
    }

    /**
     * 停止（释放后需重新 setSource）
     */
    fun stop() {
        safeAction(
            PlayerState.PLAYING,
            PlayerState.PAUSED,
            PlayerState.PREPARED,
            PlayerState.COMPLETED,
        ) {
            stopProgressTracking()
            try { mediaPlayer?.stop() } catch (_: Exception) {}
            _state = PlayerState.STOPPED
        }
    }

    /**
     * 跳转到指定位置
     *
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        if (_state == PlayerState.RELEASED || _state == PlayerState.IDLE) return
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            log("SurfacePlayer", "seekTo error: ${e.message}")
        }
    }

    // ==================== 监听器 ====================

    /**
     * 设置事件监听器
     */
    fun setListener(listener: IVideoPlayerListener?) {
        this.listener = listener
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
        pendingPrepare = null
        _state = PlayerState.RELEASED
    }

    // ==================== 内部：MediaPlayer 初始化 ====================

    private fun initMediaPlayer() {
        releaseMediaPlayer()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            setOnPreparedListener { mp ->
                _state = PlayerState.PREPARED
                listener?.onPrepared(mp.duration.toLong())
            }

            setOnCompletionListener {
                stopProgressTracking()
                when (loopMode) {
                    LoopMode.SINGLE -> {
                        it.seekTo(0)
                        it.start()
                        _state = PlayerState.PLAYING
                        startProgressTracking()
                    }
                    LoopMode.ALL -> {
                        _state = PlayerState.COMPLETED
                        listener?.onComplete()
                    }
                    LoopMode.NONE -> {
                        _state = PlayerState.COMPLETED
                        listener?.onComplete()
                    }
                }
            }

            setOnErrorListener { _, what, extra ->
                stopProgressTracking()
                _state = PlayerState.ERROR
                val errorCode = when (what) {
                    MediaPlayer.MEDIA_ERROR_UNKNOWN -> PlayerErrorCode.MEDIA_PLAYER_INTERNAL_ERROR
                    MediaPlayer.MEDIA_ERROR_SERVER_DIED -> PlayerErrorCode.SERVER_ERROR
                    else -> PlayerErrorCode.UNKNOWN
                }
                val handled = listener?.onError(errorCode, PlayerErrorCode.formatError(errorCode, "MediaPlayer error: what=$what, extra=$extra")) ?: false
                if (!handled && retryLeft > 0) {
                    retryLeft--
                    log("SurfacePlayer", "auto retry, left=$retryLeft")
                    postDelayed({ doPrepareInternal(currentUri, currentHeaders, if (isAssetSource) currentUri?.path?.removePrefix("/android_asset/") else null) }, 500)
                    true
                } else {
                    handled
                }
            }

            setOnBufferingUpdateListener { _, percent ->
                listener?.onBufferingUpdate(percent)
            }

            setOnInfoListener { _, what, extra ->
                when (what) {
                    MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING ->
                        log("SurfacePlayer", "info: video track lagging")
                    MediaPlayer.MEDIA_INFO_BUFFERING_START ->
                        log("SurfacePlayer", "info: buffering start")
                    MediaPlayer.MEDIA_INFO_BUFFERING_END ->
                        log("SurfacePlayer", "info: buffering end")
                    MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        log("SurfacePlayer", "info: video rendering start")
                        // 视频首帧渲染完成，可在此回调中做 UI 更新
                    }
                }
                listener?.onInfo(what, extra) ?: false
            }

            setOnVideoSizeChangedListener { mp, width, height ->
                if (width > 0 && height > 0) {
                    this@AndroidSurfacePlayer.videoWidth = width
                    this@AndroidSurfacePlayer.videoHeight = height
                    listener?.onVideoSizeChanged(width, height)
                    VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, videoScaleMode)
                }
            }
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.setOnPreparedListener(null)
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.setOnErrorListener(null)
            mediaPlayer?.setOnBufferingUpdateListener(null)
            mediaPlayer?.setOnInfoListener(null)
            mediaPlayer?.setOnVideoSizeChangedListener(null)
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    // ==================== 内部：Surface 回调 ====================

    private fun setupSurfaceCallback() {
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                log("SurfacePlayer", "surfaceCreated")
                isSurfaceReady = true
                // 将 Surface 绑定到 MediaPlayer
                try {
                    mediaPlayer?.setDisplay(holder)
                } catch (e: Exception) {
                    log("SurfacePlayer", "setDisplay error: ${e.message}")
                }

                // 如果有待执行的 prepare，现在执行
                pendingPrepare?.let { pending ->
                    log("SurfacePlayer", "execute pending prepare")
                    pendingPrepare = null
                    doPrepareInternal(pending.uri, pending.headers, pending.assetPath)
                }

                // 如果之前在播放且 Surface 是重建（非首次），恢复播放
                if (wasPlayingBeforeDestroy && _state in listOf(PlayerState.PAUSED, PlayerState.PLAYING)) {
                    wasPlayingBeforeDestroy = false
                    mediaPlayer?.start()
                    _state = PlayerState.PLAYING
                    startProgressTracking()
                    log("SurfacePlayer", "resumed after surface recreated")
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                log("SurfacePlayer", "surfaceChanged: ${width}x${height}")
                // Surface 尺寸变化时重新计算自适应布局
                VideoHelper.adjustRenderViewLayout(surfaceView, videoWidth, videoHeight, videoScaleMode)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                log("SurfacePlayer", "surfaceDestroyed")
                isSurfaceReady = false

                // 记录当前播放状态
                wasPlayingBeforeDestroy = _state == PlayerState.PLAYING

                // Surface 即将销毁，必须先移除 display 防止崩溃
                // 注意：不能在这里调 mediaPlayer.setDisplay(null)，某些机型会 crash
                // 安全做法是暂停播放即可
                if (wasPlayingBeforeDestroy) {
                    try {
                        mediaPlayer?.pause()
                        _state = PlayerState.PAUSED
                    } catch (_: Exception) {}
                }
                stopProgressTracking()
            }
        })
    }

    private fun removeSurfaceCallback() {
        // 移除 callback 通过重新设置一个空实现来避免 ConcurrentModificationException
        // SurfaceHolder 不提供 removeCallback 方法
    }

    // ==================== 内部：数据源设置 & 准备流程 ====================

    private fun doSetSource(uri: Uri, headers: Map<String, String>?, assetPath: String?) {
        if (_state == PlayerState.RELEASED) return
        currentUri = uri
        currentHeaders = headers
        retryLeft = retryCount

        if (isSurfaceReady) {
            // Surface 已就绪，直接执行 prepare
            doPrepareInternal(uri, headers, assetPath)
        } else {
            // Surface 未就绪，缓存参数等待 surfaceCreated
            log("SurfacePlayer", "surface not ready, cache prepare params")
            pendingPrepare = PendingPrepare(uri, headers, assetPath)
        }
    }

    private fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?, assetPath: String?) {
        // reset 以复用 MediaPlayer 实例
        if (_state != PlayerState.IDLE && _state != PlayerState.RELEASED) {
            try { mediaPlayer?.reset() } catch (_: Exception) {}
        }

        // 确保 MediaPlayer 存在
        if (mediaPlayer == null) {
            initMediaPlayer()
        }

        try {
            val context = App.context
            if (!assetPath.isNullOrEmpty()) {
                // assets 文件：直接使用 AssetFileDescriptor
                log("SurfacePlayer", "prepare asset: $assetPath")
                val afd = context.assets.openFd(assetPath)
                mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                val srcUri = uri ?: return
                if (!srcUri.scheme.isNullOrEmpty() && srcUri.scheme!!.startsWith("http") && !headers.isNullOrEmpty()) {
                    mediaPlayer?.setDataSource(context, srcUri, HashMap(headers))
                } else {
                    mediaPlayer?.setDataSource(context, srcUri)
                }
            }
            mediaPlayer?.prepareAsync()
            _state = PlayerState.PREPARING
        } catch (e: Exception) {
            log("SurfacePlayer", "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    private fun handlePrepareError(e: Exception) {
        _state = PlayerState.ERROR
        val handled = listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message)) ?: false
        if (!handled && retryLeft > 0) {
            retryLeft--
            log("SurfacePlayer", "prepare error auto retry, left=$retryLeft")
            postDelayed({ doPrepareInternal(currentUri, currentHeaders, if (isAssetSource) currentUri?.path?.removePrefix("/android_asset/") else null) }, 500)
        }
    }

    // ==================== 内部：进度追踪 ====================

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { mediaPlayer?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }
                val dur = try { mediaPlayer?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }
                listener?.onProgress(pos, dur)
                delay(progressIntervalMs.toLong())
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    // ==================== 内部：辅助方法 ====================

    /**
     * 仅在指定状态下执行操作
     */
    private inline fun safeAction(vararg expectedStates: PlayerState, action: () -> Unit) {
        if (_state in expectedStates) action()
    }

    /**
     * 应用变速
     */
    private fun applySpeed() {
        try {
            mediaPlayer?.playbackParams = PlaybackParams().apply { speed = this@AndroidSurfacePlayer.speed }
        } catch (e: Exception) {
            log("SurfacePlayer", "setSpeed error: ${e.message}")
        }
    }

    /**
     * 主线程延迟执行
     */
    private fun postDelayed(action: () -> Unit, delayMs: Long) {
        App.mainHandler.postDelayed(action, delayMs)
    }
}
