package app.allever.android.sample.audiovideo.android

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.View
import android.widget.VideoView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


/**
 * Android VideoView 视频播放封装
 *
 * 职责：
 * - 封装 VideoView + MediaPlayer 完整生命周期
 * - 管理状态机转换（复用 [PlayerState]）
 * - 提供进度追踪、变速、音量、循环等能力
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * VideoView 由外部传入，本类不创建 UI 组件。
 *
 * 使用示例：
 * ```kotlin
 * val player = AndroidVideoViewPlayer()
 * player.attach(videoView)
 * player.listener = object : IVideoPlayerListener { ... }
 * player.setSource("https://example.com/video.mp4")
 * // onPrepared 后调用 player.play()
 * player.pause()
 * player.play()  // 继续
 * player.detach()  // 页面不可见时
 * player.release()  // 不再使用时
 * ```
 */
class AndroidVideoViewPlayer {

    private var videoView: VideoView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var listener: IVideoPlayerListener? = null

    // ==================== 状态 ====================

    private var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("VideoPlayer", "state: $old -> $value")
                field = value
                listener?.onStateChanged(old, value)
            }
        }

    /** 当前状态 */
    val state get() = _state

    /** 是否正在播放 */
    val isPlaying: Boolean
        get() = _state == PlayerState.PLAYING && videoView?.isPlaying == true

    /** 当前位置（毫秒） */
    val currentPosition: Long
        get() = try { videoView?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try { videoView?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }

    // ==================== 配置 ====================

    /** 循环模式 */
    var loopMode: LoopMode = LoopMode.NONE

    /** 进度回调间隔（毫秒），默认 200ms */
    var progressIntervalMs: Int = 200

    /** 自动重试次数（出错时自动重试 prepare），默认 0 不重试 */
    var retryCount: Int = 0

    /** 变速倍率（0.5 ~ 3.0），默认 1.0（prepared 后生效） */
    var speed: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 3.0f)
            applySpeed()
        }

    /** 音量（0.0 ~ 1.0），默认 1.0（prepared 后生效） */
    var volume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(field, field)
        }

    /**
     * VideoView 缩放模式（默认 FIT_CENTER）
     *
     * 通过调整 VideoView 的布局尺寸实现不同的显示效果：
     * - FIT_CENTER: 保持比例，完整显示（可能有黑边）
     * - CROP_CENTER: 保持比例，填满容器（可能裁剪边缘）
     * - STRETCH: 拉伸填满容器（可能变形）
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            if (videoWidth > 0 && videoHeight > 0) {
                adjustVideoViewLayout()
            }
        }

    // ==================== 内部状态 ====================

    private var progressJob: Job? = null
    private var currentUri: Uri? = null
    private var currentHeaders: Map<String, String>? = null
    private var retryLeft: Int = 0
    private var isAssetSource: Boolean = false
    private var pendingSpeed: Float? = null
    private var pendingVolume: Float? = null

    /** 视频原始宽度 */
    private var videoWidth: Int = 0

    /** 视频原始高度 */
    private var videoHeight: Int = 0

    // ==================== 绑定 & 解绑 ====================

    /**
     * 绑定 VideoView（必须在播放前调用一次）
     *
     * @param videoView 外部创建的 VideoView 实例
     */
    fun attach(videoView: VideoView) {
        this.videoView = videoView
        setupVideoViewListeners()
    }

    /**
     * 解绑 VideoView（页面 onPause/onDestroyView 时调用，不释放内部资源）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     */
    fun detach() {
        stopProgressTracking()
        removeVideoViewListeners()
        videoView = null
        // 注意：不解绑时 VideoView 可能仍在后台占用资源，
        // 如果需要完全停止应先调 stop()
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
        setSource(uri)
    }

    /**
     * 设置 assets 目录下的视频文件并准备（不自动播放）
     *
     * @param assetPath assets 目录下的相对路径，如 "video/test.mp4"
     */
    fun setAssetSource(assetPath: String) {
        isAssetSource = true
        currentUri = Uri.parse("file:///android_asset/$assetPath")
        currentHeaders = null
        retryLeft = retryCount
        doPrepare()
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
        currentUri = uri
        currentHeaders = headers
        retryLeft = retryCount
        doPrepare()
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
                videoView?.start()
                _state = PlayerState.PLAYING
                startProgressTracking()
            }
            PlayerState.PAUSED -> {
                // 使用 start() 恢复，比 resume() 更可靠
                videoView?.start()
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
            videoView?.pause()
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
            videoView?.stopPlayback()
            _state = PlayerState.STOPPED
            mediaPlayer = null
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
            videoView?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            log("VideoPlayer", "seekTo error: ${e.message}")
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
        try {
            videoView?.stopPlayback()
        } catch (_: Exception) {}
        videoView = null
        mediaPlayer = null
        currentUri = null
        currentHeaders = null
        _state = PlayerState.RELEASED
    }

    // ==================== 内部：VideoView 监听器设置 ====================

    private fun setupVideoViewListeners() {
        videoView?.setOnPreparedListener { mp ->
            mediaPlayer = mp
            _state = PlayerState.PREPARED

            // 获取视频尺寸
            val w = mp.videoWidth
            val h = mp.videoHeight
            if (w > 0 && h > 0) {
                videoWidth = w
                videoHeight = h
                listener?.onVideoSizeChanged(w, h)
                adjustVideoViewLayout()
            }

            // 应用之前缓存的变速和音量设置
            pendingSpeed?.let { applySpeed(); pendingSpeed = null }
            pendingVolume?.let {
                mp.setVolume(it, it)
                pendingVolume = null
            }

            listener?.onPrepared(mp.duration.toLong())
        }

        videoView?.setOnCompletionListener {
            stopProgressTracking()
            when (loopMode) {
                LoopMode.SINGLE -> {
                    // 单曲循环：seek 到开头重新播放
                    videoView?.seekTo(0)
                    videoView?.start()
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

        videoView?.setOnErrorListener { _, what, extra ->
            stopProgressTracking()
            _state = PlayerState.ERROR
            val handled = listener?.onError(what, extra) ?: false
            if (!handled && retryLeft > 0) {
                retryLeft--
                log("VideoPlayer", "auto retry, left=$retryLeft")
                postDelayed({ doPrepare() }, 500)
                true
            } else {
                handled
            }
        }

        videoView?.setOnInfoListener { _, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> {
                    log("VideoPlayer", "info: video track lagging")
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    log("VideoPlayer", "info: buffering start")
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    log("VideoPlayer", "info: buffering end")
                }
            }
            listener?.onInfo(what, extra) ?: false
        }

        // 通过 VideoView 的 OnLayoutChangeListener 间接获取视频尺寸变化
        // 更可靠的方式：在 prepared 回调后通过 MediaPlayer 获取
        videoView?.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                if (mediaPlayer != null) {
                    val w = mediaPlayer?.videoWidth ?: 0
                    val h = mediaPlayer?.videoHeight ?: 0
                    if (w > 0 && h > 0) {
                        listener?.onVideoSizeChanged(w, h)
                        v?.removeOnLayoutChangeListener(this)
                    }
                }
            }
        })
    }

    private fun removeVideoViewListeners() {
        videoView?.setOnPreparedListener(null)
        videoView?.setOnCompletionListener(null)
        videoView?.setOnErrorListener(null)
        videoView?.setOnInfoListener(null)
    }

    // ==================== 内部：准备流程 ====================

    private fun doPrepare() {
        val vv = videoView
        if (vv == null) {
            log("VideoPlayer", "doPrepare failed: VideoView not attached")
            return
        }

        try {
            if (isAssetSource) {
                // assets 文件：VideoView 不直接支持 AssetFileDescriptor，
                // 将文件复制到缓存目录后播放
                val assetPath = currentUri?.path?.removePrefix("/android_asset/") ?: return
                log("VideoPlayer", "prepare asset: $assetPath")
                val cacheFile = copyAssetToCache(assetPath)
                videoView?.setVideoPath(cacheFile.absolutePath)
            } else {
                val uri = currentUri ?: return
                if (!uri.scheme.isNullOrEmpty() && uri.scheme!!.startsWith("http") && !currentHeaders.isNullOrEmpty()) {
                    // 带 headers 的 HTTP 请求：VideoView 不支持自定义 headers，
                    // 需要通过 Map 方式或手动设置 MediaPlayer
                    vv.setVideoURI(uri, HashMap(currentHeaders!!))
                } else {
                    vv.setVideoURI(uri)
                }
            }
            _state = PlayerState.PREPARING
        } catch (e: Exception) {
            log("VideoPlayer", "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    private fun handlePrepareError(e: Exception) {
        _state = PlayerState.ERROR
        val handled = listener?.onError(-1, 0) ?: false
        if (!handled && retryLeft > 0) {
            retryLeft--
            log("VideoPlayer", "prepare error auto retry, left=$retryLeft")
            postDelayed({ doPrepare() }, 500)
        }
    }

    // ==================== 内部：进度追踪 ====================

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { videoView?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }
                val dur = try { videoView?.duration?.toLong() ?: 0L } catch (_: Exception) { 0L }
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
        val mp = mediaPlayer
        if (mp != null) {
            try {
                mp.playbackParams = PlaybackParams().apply { speed = this@AndroidVideoViewPlayer.speed }
            } catch (e: Exception) {
                log("VideoPlayer", "setSpeed error: ${e.message}")
            }
        } else {
            // MediaPlayer 还未就绪，缓存待应用
            pendingSpeed = speed
        }
    }

    /**
     * 将 assets 文件复制到缓存目录
     */
    private fun copyAssetToCache(assetPath: String): File {
        val cacheDir = File(App.context.cacheDir, "video_cache")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val outFile = File(cacheDir, assetPath.substringAfterLast("/"))
        if (outFile.exists()) return outFile

        App.context.assets.open(assetPath).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        log("VideoPlayer", "asset copied to: ${outFile.absolutePath}")
        return outFile
    }

    /**
     * 主线程延迟执行
     */
    private fun postDelayed(action: () -> Unit, delayMs: Long) {
        App.mainHandler.postDelayed(action, delayMs)
    }

    // ==================== 内部：自适应布局 ====================

    /**
     * 根据当前缩放模式调整 VideoView 的布局尺寸
     *
     * 调用时机：
     * - onPrepared 回调中（获取到视频尺寸后）
     * - videoScaleMode 属性改变时（切换缩放模式）
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

            log("VideoPlayer", "adjustLayout: " +
                    "video=${videoWidth}x${videoHeight} " +
                    "container=${containerWidth}x${containerHeight} " +
                    "mode=$videoScaleMode -> " +
                    "target=${targetWidth}x${targetHeight}")

            // 更新 VideoView LayoutParams
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
