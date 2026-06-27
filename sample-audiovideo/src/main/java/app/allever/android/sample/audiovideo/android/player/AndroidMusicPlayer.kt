package app.allever.android.sample.audiovideo.android.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.player.core.IPlayerListener
import app.allever.android.lib.player.core.LoopMode
import app.allever.android.lib.player.core.PlayerErrorCode
import app.allever.android.lib.player.core.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android MediaPlayer 音频播放封装
 *
 * 职责：
 * - 封装 MediaPlayer 完整生命周期
 * - 管理状态机转换
 * - 提供进度追踪、变速、音量、循环等能力
 * - 通过 [app.allever.android.lib.player.core.IPlayerListener] 回调所有事件
 *
 * 使用示例：
 * ```kotlin
 * val player = AndroidMusicPlayer()
 * player.setListener(object : IPlayerListener { ... })
 * player.setSource("https://example.com/audio.mp3")  // 设置数据源并准备
 * player.play()  // 开始播放（或从暂停恢复）
 * player.pause()
 * player.play()  // 继续播放
 * player.release()
 * ```
 */
class AndroidMusicPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var listener: IPlayerListener? = null

    // ==================== 状态 ====================

    private var _state: PlayerState = PlayerState.IDLE
        set(value) {
            val old = field
            if (old != value) {
                log("MusicPlayer", "state: $old -> $value")
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

    // ==================== 内部状态 ====================

    private var progressJob: Job? = null
    private var currentUri: Uri? = null
    private var currentHeaders: Map<String, String>? = null
    private var retryLeft: Int = 0
    private var isAssetSource: Boolean = false

    // ==================== 数据源 & 播放控制 ====================

    /**
     * 设置音频数据源并准备（不自动播放）
     *
     * 支持 http/https/content/file/android_asset 协议
     * 准备完成后回调 [IPlayerListener.onPrepared]，此时需调用 [play] 开始播放
     *
     * @param url 支持：
     * - http/https URL
     * - content:// URI
     * - file:// 路径
     * - file:///android_asset/filename.mp3 (assets 目录)
     */
    fun setSource(url: String) {
        val uri = Uri.parse(url)
        isAssetSource = uri.scheme == "file" && uri.path?.startsWith("/android_asset/") == true
        setSource(uri)
    }

    /**
     * 设置 assets 目录下的音频文件并准备（不自动播放）
     *
     * @param assetPath assets 目录下的相对路径，如 "audio/test.mp3"
     */
    fun setAssetSource(assetPath: String) {
        isAssetSource = true
        currentUri = Uri.parse("file:///android_asset/$assetPath")
        currentHeaders = null
        retryLeft = retryCount
        doPrepare()
    }

    /**
     * 设置音频数据源并准备（不自动播放）
     *
     * @param uri 音频 URI
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
            mediaPlayer?.stop()
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
            log("MusicPlayer", "seekTo error: ${e.message}")
        }
    }

    // ==================== 监听器 ====================

    /**
     * 设置事件监听器
     */
    fun setListener(listener: IPlayerListener?) {
        this.listener = listener
    }

    // ==================== 生命周期 ====================

    /**
     * 释放所有资源，调用后不可再使用此实例
     */
    fun release() {
        stopProgressTracking()
        try {
            mediaPlayer?.setOnPreparedListener(null)
            mediaPlayer?.setOnCompletionListener(null)
            mediaPlayer?.setOnErrorListener(null)
            mediaPlayer?.setOnBufferingUpdateListener(null)
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        currentUri = null
        currentHeaders = null
        _state = PlayerState.RELEASED
    }

    // ==================== 内部：准备流程 ====================

    private fun doPrepare() {
        // 如果已有实例且处于可复用状态，先 reset
        if (mediaPlayer != null && _state != PlayerState.RELEASED && _state != PlayerState.IDLE) {
            try { mediaPlayer?.reset() } catch (_: Exception) {}
        }

        initMediaPlayer()

        try {
            val context = App.Companion.context
            if (isAssetSource) {
                // assets 文件
                val assetPath = currentUri?.path?.removePrefix("/android_asset/") ?: return
                log("MusicPlayer", "prepare asset: $assetPath")
                val afd = context.assets.openFd(assetPath)
                mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                val uri = currentUri ?: return
                if (!uri.scheme.isNullOrEmpty() && uri.scheme!!.startsWith("http") && !currentHeaders.isNullOrEmpty()) {
                    mediaPlayer?.setDataSource(context, uri, HashMap(currentHeaders!!))
                } else {
                    mediaPlayer?.setDataSource(context, uri)
                }
            }
            mediaPlayer?.prepareAsync()
            _state = PlayerState.PREPARING
        } catch (e: Exception) {
            log("MusicPlayer", "prepare error: ${e.message}")
            handlePrepareError(e)
        }
    }

    private fun initMediaPlayer() {
        if (mediaPlayer == null || _state == PlayerState.IDLE || _state == PlayerState.RELEASED) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                setOnPreparedListener { mp ->
                    _state = PlayerState.PREPARED
                    applySpeed()
                    listener?.onPrepared(mp.duration.toLong())
                    // 注意：此处不再自动开始播放，由外部调用 play() 触发
                }

                setOnCompletionListener {
                    stopProgressTracking()
                    when (loopMode) {
                        LoopMode.SINGLE -> {
                            // 单曲循环：seek 到开头重新播放
                            it.seekTo(0)
                            it.start()
                            _state = PlayerState.PLAYING
                            startProgressTracking()
                        }
                        LoopMode.ALL -> {
                            // 列表循环：通知上层切歌
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
                        log("MusicPlayer", "auto retry, left=$retryLeft")
                        postDelayed({ doPrepare() }, 500)
                        true  // 已通过重试处理
                    } else {
                        handled
                    }
                }

                setOnBufferingUpdateListener { _, percent ->
                    listener?.onBufferingUpdate(percent)
                }
            }
        }
    }

    private fun handlePrepareError(e: Exception) {
        _state = PlayerState.ERROR
        val handled = listener?.onError(
            PlayerErrorCode.PREPARE_FAILED, PlayerErrorCode.formatError(
                PlayerErrorCode.PREPARE_FAILED, e.message)) ?: false
        if (!handled && retryLeft > 0) {
            retryLeft--
            log("MusicPlayer", "prepare error auto retry, left=$retryLeft")
            postDelayed({ doPrepare() }, 500)
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
            mediaPlayer?.playbackParams = PlaybackParams().apply { speed = this@AndroidMusicPlayer.speed }
        } catch (e: Exception) {
            log("MusicPlayer", "setSpeed error: ${e.message}")
        }
    }

    /**
     * 主线程延迟执行
     */
    private fun postDelayed(action: () -> Unit, delayMs: Long) {
        App.Companion.mainHandler.postDelayed(action, delayMs)
    }
}