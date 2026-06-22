package app.allever.android.sample.audiovideo.core.engine

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.Surface
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.render.IVideoRender
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode

/**
 * MediaPlayer 引擎实现（基于 Android 原生 MediaPlayer）
 *
 * ## 职责
 * - 封装 Android MediaPlayer 的完整生命周期管理
 * - 将 MediaPlayer 的回调转换为 [IPlayerEngineListener] 事件
 * - 提供线程安全的播放控制接口
 *
 * ## MediaPlayer 特点（与 ExoPlayer/IJKPlayer 的区别）
 * **优势：**
 * ✅ **零依赖**：Android 系统内置，无需引入第三方库
 * ✅ **体积小**：不增加 APK 体积
 * ✅ **启动快**：初始化速度快，适合短视频场景
 * ✅ **兼容性好**：所有 Android 设备都支持
 *
 * **劣势：**
 * ❌ **功能有限**：不支持 DASH/HLS/SmoothStreaming 等流媒体协议
 * ❌ **无自适应码率**：无法根据网络状况自动切换清晰度
 * ❌ **错误恢复弱**：网络异常时容易卡死
 * ❌ **定制性差**：无法自定义解码器、渲染器等组件
 *
 * ## 适用场景
 * - 本地视频播放
 * - 简单的 HTTP 视频流（MP4 格式）
 * - 对 APK 体积敏感的应用
 * - 短视频播放（启动速度要求高）
 *
 * ## 不适用场景
 * - 需要播放 HLS/DASH 流媒体
 * - 需要自适应码率（ABR）
 * - 需要高级功能（字幕、多音轨、画中画等）
 * - 对播放稳定性要求极高的场景
 *
 * ## 使用示例
 * ```kotlin
 * val engine = MediaPlayerEngine()
 * engine.setListener(object : IPlayerEngineListener {
 *     override fun onPrepared() { engine.start() }
 *     override fun onCompletion() { log("播放完成") }
 *     override fun onError(code: Int, msg: String) { log("错误: $msg") }
 * })
 * engine.init()
 * engine.setSource(uri, headers)
 * engine.prepareAsync()
 * ```
 */
class MediaPlayerEngine : IPlayerEngine {

    companion object {
        private const val TAG = "MediaPlayerEngine"
    }

    /** 底层 MediaPlayer 实例 */
    private var mediaPlayer: MediaPlayer? = null

    /** 引擎事件监听器 */
    private var listener: IPlayerEngineListener? = null

    // ==================== MediaPlayer 监听器 ====================

    /** 准备完成监听器 */
    private val preparedListener = MediaPlayer.OnPreparedListener {
        log(TAG, "onPrepared")
        App.mainHandler.post { listener?.onPrepared() }
    }

    /** 播放完成监听器 */
    private val completionListener = MediaPlayer.OnCompletionListener {
        log(TAG, "onCompletion")
        App.mainHandler.post { listener?.onCompletion() }
    }

    /** 错误监听器 */
    private val errorListener = MediaPlayer.OnErrorListener { _, what, extra ->
        log(TAG, "onError: what=$what, extra=$extra")
        App.mainHandler.post {
            val errorCode = mapMediaPlayerError(what, extra)
            val errorMsg = PlayerErrorCode.formatError(errorCode, "MediaPlayer error: what=$what, extra=$extra")
            listener?.onError(errorCode, errorMsg)
        }
        true // 返回 true 表示已处理此错误
    }

    /** 缓冲进度监听器 */
    private val bufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { _, percent ->
        App.mainHandler.post { listener?.onBufferingUpdate(percent) }
    }

    /** 视频尺寸变化监听器 */
    private val videoSizeChangedListener = MediaPlayer.OnVideoSizeChangedListener { _, width, height ->
        log(TAG, "onVideoSizeChanged: ${width}x${height}")
        App.mainHandler.post { listener?.onVideoSizeChanged(width, height) }
    }

    /** 信息监听器 */
    private val infoListener = MediaPlayer.OnInfoListener { mp, what, _ ->
        log(TAG, "onInfo: $what")
        App.mainHandler.post {
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    log(TAG, "BUFFERING_START")
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    log(TAG, "BUFFERING_END")
                }
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    log(TAG, "VIDEO_RENDERING_START")
                }
                MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> {
                    log(TAG, "VIDEO_TRACK_LAGGING")
                }
            }
            listener?.onInfo()
        }
        true
    }

    // ==================== 生命周期管理 ====================

    /**
     * 初始化引擎
     *
     * 创建 MediaPlayer 实例并设置所有监听器。
     * 必须在调用其他方法之前调用。
     */
    override fun init() {
        if (mediaPlayer != null) return

        mediaPlayer = MediaPlayer().apply {
            // 设置音频属性
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )

            // 设置所有监听器
            setOnPreparedListener(preparedListener)
            setOnCompletionListener(completionListener)
            setOnErrorListener(errorListener)
            setOnBufferingUpdateListener(bufferingUpdateListener)
            setOnVideoSizeChangedListener(videoSizeChangedListener)
            setOnInfoListener(infoListener)
        }

        log(TAG, "initialized")
    }

    /**
     * 异步准备数据源
     *
     * 调用前必须先调用 [setSource] 或 [setAssetSource]。
     */
    override fun prepareAsync() {
        mediaPlayer?.prepareAsync()
    }

    /**
     * 开始播放 或 从暂停恢复播放
     */
    override fun start() {
        mediaPlayer?.start()
        // 通知播放状态变化
        App.mainHandler.post { listener?.onIsPlayingChanged(true) }
    }

    /**
     * 暂停播放
     */
    override fun pause() {
        mediaPlayer?.pause()
        // 通知播放状态变化
        App.mainHandler.post { listener?.onIsPlayingChanged(false) }
    }

    /**
     * 停止播放并重置
     */
    override fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (e: Exception) {
            log(TAG, "stop error: ${e.message}")
        }
    }

    /**
     * 重置引擎状态
     */
    override fun reset() {
        try {
            mediaPlayer?.reset()
        } catch (e: Exception) {
            log(TAG, "reset error: ${e.message}")
        }
    }

    /**
     * 释放所有资源
     */
    override fun release() {
        try {
            mediaPlayer?.apply {
                setOnPreparedListener(null)
                setOnCompletionListener(null)
                setOnErrorListener(null)
                setOnBufferingUpdateListener(null)
                setOnVideoSizeChangedListener(null)
                setOnInfoListener(null)
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        listener = null
        log(TAG, "released")
    }

    // ==================== 数据源设置 ====================

    /**
     * 设置视频数据源
     *
     * 支持 http/https/file/content 协议。
     */
    override fun setSource(uri: Uri, headers: Map<String, String>?) {
        val context = App.context.applicationContext
        
        when (uri.scheme) {
            "http", "https" -> {
                if (!headers.isNullOrEmpty()) {
                    mediaPlayer?.setDataSource(context, uri, HashMap(headers))
                } else {
                    mediaPlayer?.setDataSource(uri.toString())
                }
            }
            "content" -> {
                mediaPlayer?.setDataSource(context, uri)
            }
            "file" -> {
                mediaPlayer?.setDataSource(uri.toString())
            }
            else -> {
                mediaPlayer?.setDataSource(uri.toString())
            }
        }
    }

    /**
     * 设置 assets 目录下的视频文件
     *
     * 注意：MediaPlayer 不直接支持 assets，
     * 此方法仅做记录，实际由协调器处理文件复制。
     */
    override fun setAssetSource(path: String) {
        // MediaPlayer 不直接支持 assets
        // 实际处理由 VideoPlayer 协调器负责（复制到缓存后使用 file:// URI）
        log(TAG, "setAssetSource: $path (will be handled by coordinator)")
    }

    // ==================== 播放控制 ====================

    /**
     * 跳转到指定位置
     */
    override fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    /**
     * 设置播放速度（需要 API 23+）
     */
    override fun setSpeed(speed: Float) {
        try {
            mediaPlayer?.playbackParams = PlaybackParams().setSpeed(speed)
        } catch (e: Exception) {
            log(TAG, "setSpeed error: ${e.message}")
        }
    }

    /**
     * 设置音量
     */
    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    /**
     * 设置循环模式
     */
    override fun setLoopMode(mode: LoopMode) {
        val looping = when (mode) {
            LoopMode.SINGLE, LoopMode.ALL -> true
            else -> false
        }
        try {
            mediaPlayer?.isLooping = looping
        } catch (_: Exception) {}
    }

    /**
     * 设置渲染 Surface
     */
    override fun setSurface(surface: Surface?, render: IVideoRender) {
        mediaPlayer?.setSurface(surface)
    }

    // ==================== 状态查询 ====================

    /**
     * 获取当前播放位置（毫秒）
     */
    override fun getCurrentPosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 获取视频总时长（毫秒）
     */
    override fun getDuration(): Long {
        return try {
            mediaPlayer?.duration?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 是否正在播放
     */
    override fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 获取视频宽度（像素）
     */
    override fun getVideoWidth(): Int {
        return try {
            mediaPlayer?.videoWidth ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取视频高度（像素）
     */
    override fun getVideoHeight(): Int {
        return try {
            mediaPlayer?.videoHeight ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取网络下载速度（MediaPlayer 不支持，返回 0）
     */
    override fun getTcpSpeed(): Long {
        return 0L
    }

    // ==================== 监听器管理 ====================

    /**
     * 设置引擎事件监听器
     */
    override fun setListener(listener: IPlayerEngineListener?) {
        this.listener = listener
    }

    /**
     * 移除引擎事件监听器
     */
    override fun removeListener(listener: IPlayerEngineListener) {
        if (this.listener === listener) {
            this.listener = null
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 将 MediaPlayer 错误代码映射到 PlayerErrorCode
     */
    private fun mapMediaPlayerError(what: Int, extra: Int): Int {
        return when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> PlayerErrorCode.MEDIA_PLAYER_INTERNAL_ERROR
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> PlayerErrorCode.SERVER_ERROR
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_IO -> PlayerErrorCode.FILE_READ_ERROR
            MediaPlayer.MEDIA_ERROR_MALFORMED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlayerErrorCode.NETWORK_TIMEOUT
            else -> PlayerErrorCode.UNKNOWN
        }
    }
}
