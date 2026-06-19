package app.allever.android.sample.audiovideo.android

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import app.allever.android.sample.audiovideo.lib.IVideoPlayerListener
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android Media3 (ExoPlayer) 视频播放封装
 *
 * 职责：
 * - 封装 ExoPlayer 完整生命周期
 * - 管理 ExoPlayer 状态与 [PlayerState] 的映射
 * - 处理 PlayerView 绑定/解绑（可选）
 * - 提供进度追踪、变速、音量、循环等能力
 * - 通过 [IVideoPlayerListener] 回调所有事件
 *
 * PlayerView 由外部传入（可选），本类不强制创建 UI 组件。
 * 不传入 PlayerView 时可纯音频模式使用。
 *
 * 使用示例：
 * ```kotlin
 * val player = AndroidMedia3Player()
 *
 * // 可选：绑定 PlayerView 用于视频渲染
 * player.attach(playerView)
 *
 * player.listener = object : IVideoPlayerListener {
 *     override fun onPrepared(durationMs: Long) { player.play() }
 *     override fun onVideoSizeChanged(w, h) { /* 调整视图 */ }
 * }
 *
 * player.setSource("https://example.com/video.mp4")
 * // 或 player.setAssetSource("video/test.mp4")
 *
 * // 页面销毁时
 * player.release()
 * ```
 */
class AndroidMedia3Player {

    private var playerView: PlayerView? = null
    private var exoPlayer: ExoPlayer? = null
    private var listener: IVideoPlayerListener? = null

    // ==================== 状态 ====================

    private var _state: PlayerState = PlayerState.IDLE
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
        get() = _state == PlayerState.PLAYING && exoPlayer?.isPlaying == true

    /** 当前位置（毫秒） */
    val currentPosition: Long
        get() = try { exoPlayer?.currentPosition ?: 0L } catch (_: Exception) { 0L }

    /** 总时长（毫秒），PREPARED 后可用 */
    val duration: Long
        get() = try {
            val dur = exoPlayer?.duration?:0
            if (dur == C.TIME_UNSET || dur < 0) 0L else dur
        } catch (_: Exception) { 0L }

    // ==================== 配置 ====================

    /** 循环模式 */
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
            exoPlayer?.volume = field
        }

    /**
     * PlayerView 缩放模式（默认 FIT_CENTER）
     *
     * 通过 PlayerView 的 resizeMode 属性控制视频显示方式：
     * - FIT_CENTER: 保持比例，完整显示（可能有黑边）
     * - CROP_CENTER: 保持比例，填满容器（可能裁剪边缘）
     * - STRETCH: 拉伸填满容器（可能变形）
     */
    var videoScaleMode: VideoScaleMode = VideoScaleMode.FIT_CENTER
        set(value) {
            field = value
            applyVideoScaleMode()
        }

    // ==================== 内部状态 ====================

    private var progressJob: Job? = null
    private var currentUri: Uri? = null
    private var currentHeaders: Map<String, String>? = null
    private var retryLeft: Int = 0

    /** 是否正在执行 seek 操作（用于防止 seek 过程中误停进度追踪） */
    @Volatile
    private var isSeeking: Boolean = false

    // ==================== 绑定 & 解绑 ====================

    /**
     * 绑定 PlayerView（可选，不绑定则纯音频模式）
     *
     * 必须在播放前调用一次。
     *
     * @param view 外部创建的 PlayerView 实例
     */
    fun attach(view: PlayerView) {
        this.playerView = view
        initExoPlayer()
    }

    /**
     * 解绑 PlayerView（页面 onPause/onDestroyView 时调用，不释放内部资源）
     *
     * 调用后可通过 [attach] 重新绑定继续使用。
     */
    fun detach() {
        stopProgressTracking()
        playerView?.player = null
        playerView = null
    }

    // ==================== 数据源 & 播放控制 ====================

    /**
     * 设置视频数据源并准备（不自动播放）
     *
     * 支持 http/https/content/file/asset 协议
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
        doSetSource(uri, null)
    }

    /**
     * 设置 assets 目录下的视频文件并准备（不自动播放）
     *
     * @param assetPath assets 目录下的相对路径，如 "video/test.mp4"
     */
    fun setAssetSource(assetPath: String) {
        doSetSource(Uri.parse("asset:///$assetPath"), null)
    }

    /**
     * 设置视频数据源并准备（不自动播放）
     *
     * @param uri 视频 URI
     * @param headers HTTP 请求头（仅对 http(s) 生效）
     */
    fun setSource(uri: Uri, headers: Map<String, String>? = null) {
        if (_state == PlayerState.RELEASED) return
        doSetSource(uri, headers)
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
                exoPlayer?.playWhenReady = true
                _state = PlayerState.PLAYING
                startProgressTracking()
            }
            PlayerState.PAUSED -> {
                exoPlayer?.playWhenReady = true
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
            exoPlayer?.playWhenReady = false
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
            exoPlayer?.stop()
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
            isSeeking = true  // 标记正在 seek，防止误停进度追踪
            exoPlayer?.seekTo(positionMs)
            // 延迟重置标志并确保进度追踪正常运行（seek 是异步操作）
            App.mainHandler.postDelayed({
                isSeeking = false
                // 确保 seek 完成后进度追踪仍在运行
                if (_state == PlayerState.PLAYING && (progressJob == null || !progressJob!!.isActive)) {
                    log("Media3Player", "restart progress tracking after seek")
                    startProgressTracking()
                }
            }, 300)
        } catch (e: Exception) {
            log("Media3Player", "seekTo error: ${e.message}")
            isSeeking = false
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
        releaseExoPlayer()
        currentUri = null
        currentHeaders = null
        _state = PlayerState.RELEASED
    }

    // ==================== 内部：ExoPlayer 初始化 ====================

    private fun initExoPlayer() {
        releaseExoPlayer()

        val context: Context = App.context.applicationContext
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            // 绑定到 PlayerView
            playerView?.player = this@apply

            // 添加监听器
            addListener(exoPlayerListener)

            // 应用初始配置
            applyLoopMode()
            applySpeed()
            applyVideoScaleMode()
            this@AndroidMedia3Player.volume.let { vol ->
                if (vol != 1.0f) this@apply.volume = vol
            }
        }
    }

    private fun releaseExoPlayer() {
        try {
            exoPlayer?.removeListener(exoPlayerListener)
            exoPlayer?.release()
        } catch (_: Exception) {}
        exoPlayer = null
    }

    // ==================== 内部：ExoPlayer 监听器 ====================

    private val exoPlayerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    // IDLE 状态：可能是停止或错误后的状态
                    if (_state != PlayerState.STOPPED && _state != PlayerState.ERROR && _state != PlayerState.IDLE) {
                        _state = PlayerState.IDLE
                    }
                }
                Player.STATE_BUFFERING -> {
                    when (_state) {
                        PlayerState.IDLE, PlayerState.STOPPED -> {
                            // 首次加载中 → PREPARING
                            _state = PlayerState.PREPARING
                        }
                        else -> {
                            // 播放过程中的缓冲，保持当前状态不变
                            listener?.onBufferingUpdate(getBufferedPercent())
                        }
                    }
                }
                Player.STATE_READY -> {
                    if (exoPlayer?.isPlaying == true) {
                        if (_state != PlayerState.PLAYING) {
                            _state = PlayerState.PLAYING
                            startProgressTracking()
                        }
                    } else {
                        // READY 但未播放：首次 prepared 或暂停状态
                        if (_state == PlayerState.PREPARING || _state == PlayerState.IDLE) {
                            // 首次准备好
                            val dur = duration
                            _state = PlayerState.PREPARED
                            listener?.onPrepared(dur)
                        } else if (_state == PlayerState.PLAYING) {
                            // 从 playing 变为 ready（缓冲结束），保持 playing
                        } else if (_state != PlayerState.PAUSED && _state != PlayerState.PREPARED) {
                            _state = PlayerState.PREPARED
                        }
                    }
                }
                Player.STATE_ENDED -> {
                    stopProgressTracking()
                    when (loopMode) {
                        LoopMode.SINGLE -> {
                            // SINGLE 模式由 repeatMode=ONE 自动处理，不应到达这里
                            // 兜底处理
                            _state = PlayerState.COMPLETED
                            listener?.onComplete()
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
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // 注意：此回调在 ExoPlayer 的播放线程，需切换到主线程
            App.mainHandler.post {
                // 如果正在 seek 操作中，忽略临时的 isPlaying 变化（seek 过程中会短暂暂停）
                if (isSeeking) {
                    log("Media3Player", "onIsPlayingChanged ignored during seeking: isPlaying=$isPlaying")
                    return@post
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

        override fun onPlayerError(error: PlaybackException) {
            stopProgressTracking()
            _state = PlayerState.ERROR

            val handled = listener?.onError(error.errorCode, 0) ?: false
            if (!handled && retryLeft > 0) {
                retryLeft--
                log("Media3Player", "auto retry, left=$retryLeft")
                postDelayed({ doPrepareInternal(currentUri, currentHeaders) }, 500)
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            if (videoSize.width > 0 && videoSize.height > 0) {
                listener?.onVideoSizeChanged(videoSize.width, videoSize.height)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            // 缓冲进度更新
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                val percent = getBufferedPercent()
                if (percent > 0) {
                    listener?.onBufferingUpdate(percent)
                }
            }
        }
    }

    // ==================== 内部：数据源设置 & 准备流程 ====================

    private fun doSetSource(uri: Uri, headers: Map<String, String>?) {
        if (_state == PlayerState.RELEASED) return

        // 停止当前的进度追踪（重要：切换数据源前必须清理）
        stopProgressTracking()

        currentUri = uri
        currentHeaders = headers
        retryLeft = retryCount

        doPrepareInternal(uri, headers)
    }

    private fun doPrepareInternal(uri: Uri?, headers: Map<String, String>?) {
        val srcUri = uri ?: return

        // 确保 ExoPlayer 存在
        if (exoPlayer == null) {
            initExoPlayer()
        }

        try {
            val mediaItem = MediaItem.Builder().setUri(srcUri).build()

            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
            }

            _state = PlayerState.PREPARING
        } catch (e: Exception) {
            log("Media3Player", "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    private fun handlePrepareError(e: Exception) {
        _state = PlayerState.ERROR
        val handled = listener?.onError(-1, 0) ?: false
        if (!handled && retryLeft > 0) {
            retryLeft--
            log("Media3Player", "prepare error auto retry, left=$retryLeft")
            postDelayed({ doPrepareInternal(currentUri, currentHeaders) }, 500)
        }
    }

    // ==================== 内部：进度追踪 ====================

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _state == PlayerState.PLAYING) {
                val pos = try { exoPlayer?.currentPosition ?: 0L } catch (_: Exception) { 0L }
                val dur = try {
                    val d = exoPlayer?.duration?:0
                    if (d == C.TIME_UNSET || d < 0) 0L else d
                } catch (_: Exception) { 0L }
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
     * 应用循环模式
     */
    private fun applyLoopMode() {
        exoPlayer?.repeatMode = when (loopMode) {
            LoopMode.NONE -> Player.REPEAT_MODE_OFF
            LoopMode.SINGLE -> Player.REPEAT_MODE_ONE
            LoopMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    /**
     * 应用变速
     */
    private fun applySpeed() {
        try {
            exoPlayer?.setPlaybackParameters(
                androidx.media3.common.PlaybackParameters(speed)
            )
        } catch (e: Exception) {
            log("Media3Player", "setSpeed error: ${e.message}")
        }
    }

    /**
     * 应用视频缩放模式
     *
     * 通过 PlayerView 的 resizeMode 属性控制：
     * - FIT_CENTER → RESIZE_MODE_FIT（保持比例，完整显示）
     * - CROP_CENTER → RESIZE_MODE_ZOOM（保持比例，填满容器）
     * - STRETCH → RESIZE_MODE_FILL（拉伸填满）
     */
    @OptIn(UnstableApi::class)
    private fun applyVideoScaleMode() {
        playerView?.resizeMode = when (videoScaleMode) {
            VideoScaleMode.FIT_CENTER -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            VideoScaleMode.CROP_CENTER -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            VideoScaleMode.STRETCH -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }

    /**
     * 计算缓冲百分比
     */
    private fun getBufferedPercent(): Int {
        val dur = try {
            val d = exoPlayer?.duration?:0
            if (d == C.TIME_UNSET || d <= 0) return 0 else d
        } catch (_: Exception) { return 0 }

        val buffered = try { exoPlayer?.totalBufferedDuration ?: 0L } catch (_: Exception) { 0L }
        return ((buffered.toFloat() / dur.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * 主线程延迟执行
     */
    private fun postDelayed(action: () -> Unit, delayMs: Long) {
        App.mainHandler.postDelayed(action, delayMs)
    }
}
