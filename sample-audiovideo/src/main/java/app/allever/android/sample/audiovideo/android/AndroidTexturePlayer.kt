package app.allever.android.sample.audiovideo.android

import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
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

/**
 * Android TextureView + MediaPlayer 视频播放封装
 *
 * 职责：
 * - 手动管理 MediaPlayer 完整生命周期
 * - 处理 TextureView 的 Surface 生命周期（available/sizeChanged/destroyed）
 * - 管理状态机转换（复用 [PlayerState]）
 * - 提供进度追踪、变速、音量、循环等能力
 * - 自适应容器布局（FIT_CENTER 模式）
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * TextureView 由外部传入，本类不创建 UI 组件。
 *
 * 与 [AndroidSurfacePlayer] 的核心区别：
 * - TextureView 的 Surface 通过 [TextureView.SurfaceTextureListener] 获取，通常立即可用
 * - 不需要像 SurfaceView 那样处理 pendingPrepare 和 surfaceCreated 异步回调
 * - 支持矩阵变换（旋转/缩放），基础版使用 LayoutParams 方式自适应
 *
 * 使用示例：
 * ```kotlin
 * val player = AndroidTexturePlayer()
 * player.attach(textureView)
 * player.listener = object : IVideoPlayerListener { ... }
 * player.setSource("https://example.com/video.mp4")
 * // onPrepared 后调用 player.play()
 * player.pause()
 * player.play()  // 继续
 * player.detach()  // 页面不可见时
 * player.release()  // 不再使用时
 * ```
 */
class AndroidTexturePlayer {

    private var textureView: TextureView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var listener: IVideoPlayerListener? = null

    // ==================== 状态 ====================

    private var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("TexturePlayer", "state: $old -> $value")
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
     * TextureView 缩放模式（默认 FIT_CENTER）
     *
     * 通过调整 TextureView 的布局尺寸实现不同的显示效果：
     * - FIT_CENTER: 保持比例，完整显示（可能有黑边）
     * - CROP_CENTER: 保持比例，填满容器（可能裁剪边缘）
     * - STRETCH: 拉伸填满容器（可能变形）
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            if (videoWidth > 0 && videoHeight > 0) {
                adjustTextureLayout()
            }
        }

    // ==================== 内部状态 ====================

    private var progressJob: Job? = null
    private var currentUri: Uri? = null
    private var currentHeaders: Map<String, String>? = null
    private var retryLeft: Int = 0
    private var isAssetSource: Boolean = false

    /** Surface 是否可用 */
    private var isSurfaceReady: Boolean = false

    /** 视频原始宽度 */
    private var videoWidth: Int = 0

    /** 视频原始高度 */
    private var videoHeight: Int = 0

    /**
     * 待执行的 setSource 参数（当 Surface 未就绪时缓存）
     *
     * 注意：TextureView 的 Surface 通常立即可用，
     * 此机制仅为极端情况（如 View 尚未 attach 到 Window）兜底。
     */
    private data class PendingPrepare(
        val uri: Uri,
        val headers: Map<String, String>?,
        val assetPath: String?
    )

    private var pendingPrepare: PendingPrepare? = null

    // ==================== 绑定 & 解绑 ====================

    /**
     * 绑定 TextureView（必须在播放前调用一次）
     *
     * 会立即创建 MediaPlayer 并设置 SurfaceTextureListener 监听 Surface 生命周期。
     *
     * @param textureView 外部创建的 TextureView 实例
     */
    fun attach(textureView: TextureView) {
        this.textureView = textureView
        initMediaPlayer()
        setupSurfaceTextureCallback()
    }

    /**
     * 解绑 TextureView（页面 onPause/onDestroyView 时调用，不释放内部资源）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     */
    fun detach() {
        stopProgressTracking()
        removeSurfaceTextureCallback()
        textureView = null
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
            log("TexturePlayer", "seekTo error: ${e.message}")
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
                    log("TexturePlayer", "auto retry, left=$retryLeft")
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
                        log("TexturePlayer", "info: video track lagging")
                    MediaPlayer.MEDIA_INFO_BUFFERING_START ->
                        log("TexturePlayer", "info: buffering start")
                    MediaPlayer.MEDIA_INFO_BUFFERING_END ->
                        log("TexturePlayer", "info: buffering end")
                    MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START ->
                        log("TexturePlayer", "info: video rendering start")
                }
                listener?.onInfo(what, extra) ?: false
            }

            setOnVideoSizeChangedListener { mp, width, height ->
                if (width > 0 && height > 0) {
                    this@AndroidTexturePlayer.videoWidth = width
                    this@AndroidTexturePlayer.videoHeight = height
                    listener?.onVideoSizeChanged(width, height)
                    adjustTextureLayout()
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

    // ==================== 内部：SurfaceTexture 回调 ====================

    private fun setupSurfaceTextureCallback() {
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                log("TexturePlayer", "surfaceTextureAvailable: ${width}x${height}")
                isSurfaceReady = true

                // 将 Surface 绑定到 MediaPlayer
                try {
                    mediaPlayer?.setSurface(Surface(surfaceTexture))
                } catch (e: Exception) {
                    log("TexturePlayer", "setSurface error: ${e.message}")
                }

                // 如果有待执行的 prepare，现在执行
                pendingPrepare?.let { pending ->
                    log("TexturePlayer", "execute pending prepare")
                    pendingPrepare = null
                    doPrepareInternal(pending.uri, pending.headers, pending.assetPath)
                }

                // 如果已有视频尺寸信息，调整布局
                if (videoWidth > 0 && videoHeight > 0) {
                    adjustTextureLayout()
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                log("TexturePlayer", "surfaceTextureSizeChanged: ${width}x${height}")
                // TextureView 尺寸变化时重新计算自适应布局
                adjustTextureLayout()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log("TexturePlayer", "surfaceTextureDestroyed")
                isSurfaceReady = false

                // 记录当前播放状态
                val wasPlaying = _state == PlayerState.PLAYING

                // Surface 即将销毁，暂停播放
                if (wasPlaying) {
                    try {
                        mediaPlayer?.pause()
                        _state = PlayerState.PAUSED
                    } catch (_: Exception) {}
                }
                stopProgressTracking()

                // 返回 true 表示已处理销毁，false 则会由系统再次尝试销毁
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // 每帧渲染回调，一般不需要处理
            }
        }
    }

    private fun removeSurfaceTextureCallback() {
        textureView?.surfaceTextureListener = null
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
            // Surface 未就绪（极少情况），缓存参数等待 onSurfaceTextureAvailable
            log("TexturePlayer", "surface not ready, cache prepare params")
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
                log("TexturePlayer", "prepare asset: $assetPath")
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
            log("TexturePlayer", "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    private fun handlePrepareError(e: Exception) {
        _state = PlayerState.ERROR
        val handled = listener?.onError(PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(PlayerErrorCode.PREPARE_FAILED, e.message)) ?: false
        if (!handled && retryLeft > 0) {
            retryLeft--
            log("TexturePlayer", "prepare error auto retry, left=$retryLeft")
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
            mediaPlayer?.playbackParams = PlaybackParams().apply { speed = this@AndroidTexturePlayer.speed }
        } catch (e: Exception) {
            log("TexturePlayer", "setSpeed error: ${e.message}")
        }
    }

    /**
     * 主线程延迟执行
     */
    private fun postDelayed(action: () -> Unit, delayMs: Long) {
        App.mainHandler.postDelayed(action, delayMs)
    }

    // ==================== 内部：自适应布局 ====================

    /**
     * 根据当前缩放模式调整 TextureView 的布局尺寸
     *
     * 调用时机：
     * - onVideoSizeChanged 回调中（获取到视频尺寸后）
     * - videoScaleMode 属性改变时（切换缩放模式）
     * - onSurfaceTextureSizeChanged 回调中（TextureView 尺寸变化时）
     */
    private fun adjustTextureLayout() {
        val tv = textureView ?: return
        if (videoWidth <= 0 || videoHeight <= 0) return

        val parent = tv.parent as? ViewGroup ?: return

        App.mainHandler.post {
            val containerWidth = parent.width
            val containerHeight = parent.height
            if (containerWidth <= 0 || containerHeight <= 0) return@post

            val (targetWidth, targetHeight) = calculateTargetSize(
                videoWidth, videoHeight,
                containerWidth, containerHeight,
                videoScaleMode
            )

            log("TexturePlayer", "adjustLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            // 更新 TextureView LayoutParams
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
                if (videoAspect > containerAspect) {
                    Pair(containerWidth, (containerWidth / videoAspect).toInt())
                } else {
                    Pair((containerHeight * videoAspect).toInt(), containerHeight)
                }
            }
            VideoScaleMode.CROP_CENTER -> {
                if (videoAspect > containerAspect) {
                    Pair((containerHeight * videoAspect).toInt(), containerHeight)
                } else {
                    Pair(containerWidth, (containerWidth / videoAspect).toInt())
                }
            }
            VideoScaleMode.STRETCH -> {
                Pair(containerWidth, containerHeight)
            }
        }
    }
}
