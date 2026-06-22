package app.allever.android.sample.audiovideo.core.engine

import android.net.Uri
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.render.ExoPlayerViewRender
import app.allever.android.sample.audiovideo.core.render.IVideoRender
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode

/**
 * Media3 (ExoPlayer) 引擎实现（基于 Google ExoPlayer/Media3）
 *
 * ## 职责
 * - 封装 ExoPlayer 的完整生命周期管理
 * - 将 ExoPlayer 的事件转换为 [IPlayerEngineListener] 回调
 * - 提供 ExoPlayer 特有的高级功能支持
 *
 * ## ExoPlayer 特点（与 MediaPlayer/IJK 的区别）
 * **优势：**
 * ✅ **功能强大**：支持 DASH、HLS、SmoothStreaming 等流媒体协议
 * ✅ **性能优秀**：针对 Android 优化，低延迟、高效率
 * ✅ **可定制性极强**：可自定义渲染器、解码器、加载器等每个组件
 * ✅ **官方维护**：Google 官方推荐，持续更新，文档完善
 * ✅ **适配性好**：自动处理各种设备和 Android 版本的兼容性问题
 * ✅ **高级特性**：
 *    - 自适应码率（ABR）：根据网络状况自动切换清晰度
 *    - 缓冲策略优化：智能缓冲，减少卡顿
 *    - 后台播放：Service 集成简单
 *    - 画中画：原生支持 Picture-in-Picture 模式
 *    - 字幕支持：WebVTT、TTML 等多种字幕格式
 *
 * **劣势：**
 * ❌ **依赖体积大**：库体积约 5-10MB（相比 MediaPlayer 的 0MB）
 * ❌ **学习成本高**：API 复杂，概念多（Renderer、TrackSelector、LoadControl 等）
 * ❌ **初始化较慢**：首次创建需要初始化解码器池等资源
 * ❌ **内存占用较高**：相比 MediaPlayer 多占用几 MB 内存
 *
 * ## 适用场景
 * - **在线视频应用**（YouTube、Netflix 风格）- DASH/HLS 流媒体
 * - **需要高级功能的播放器**（自适应码率、多音轨切换等）
 * - **企业级/商业项目**（稳定性要求高，需长期维护）
 * - **直播应用**（低延迟、高并发场景）
 * - **需要 DRM 保护的内容**（Widevine、PlayReady 等）
 *
 * ## 不适用场景
 * - 简单的本地视频播放（MediaPlayer 足够，更轻量）
 * - 对 APK 体积有严格限制的应用
 * - 快速原型开发 / MVP 验证
 *
 * ## 使用示例
 * ```kotlin
 * val engine = Media3PlayerEngine()
 * engine.setListener(object : IPlayerEngineListener {
 *     override fun onPrepared() { engine.start() }
 *     override fun onCompletion() { log("播放完成") }
 *     override fun onError(code: Int, msg: String) { log("错误: $msg") }
 * })
 * engine.init()
 * engine.setSource(uri, headers)
 * engine.prepareAsync()
 * ```
 *
 * @see IPlayerEngine 引擎接口定义
 */
class Media3PlayerEngine : IPlayerEngine {

    companion object {
        val NAME = Media3PlayerEngine::class.java.simpleName
    }

    private var uri: Uri? = null

    /** ExoPlayer 实例 */
    private var exoPlayer: ExoPlayer? = null

    /** 引擎事件监听器 */
    private var listener: IPlayerEngineListener? = null

    // ==================== ExoPlayer 监听器 ====================

    /** ExoPlayer 事件监听器（统一处理所有回调）*/
    private val exoPlayerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            log(TAG, "onPlaybackStateChanged: $playbackState")
            App.mainHandler.post {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        // IDLE 状态，通常不需要处理
                    }
                    Player.STATE_BUFFERING -> {
                        // 缓冲中
                        val percent = getBufferedPercent()
                        listener?.onBufferingUpdate(percent)
                    }
                    Player.STATE_READY -> {
                        // 准备就绪（对应 onPrepared）
                        listener?.onPrepared()
                    }
                    Player.STATE_ENDED -> {
                        // 播放结束
                        listener?.onCompletion()
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            log(TAG, "onIsPlayingChanged: $isPlaying")
            App.mainHandler.post {
                listener?.onIsPlayingChanged(isPlaying)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            log(TAG, "onPlayerError: ${error.errorCodeName} -> ${error.message}")
            App.mainHandler.post {
                val errorCode = mapExoPlayerError(error)
                val errorMsg = PlayerErrorCode.formatError(errorCode, error.message)
                listener?.onError(errorCode, errorMsg)
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            log(TAG, "onVideoSizeChanged: ${videoSize.width} x ${videoSize.height}")
            App.mainHandler.post {
                listener?.onVideoSizeChanged(videoSize.width, videoSize.height)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            // 处理位置不连续或时间线变化时的缓冲更新
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                val percent = getBufferedPercent()
                if (percent > 0) {
                    App.mainHandler.post {
                        listener?.onBufferingUpdate(percent)
                    }
                }
            }
        }
    }

    // ==================== 生命周期管理 ====================

    /**
     * 初始化引擎
     *
     * 创建 ExoPlayer 实例并设置监听器。
     */
    override fun init() {
        if (exoPlayer != null) return

        val context = App.context.applicationContext
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(exoPlayerListener)
        }

        log(TAG, "initialized")
    }

    /**
     * 异步准备数据源
     */
    override fun prepareAsync() {
        exoPlayer?.prepare()
    }

    /**
     * 开始播放 或 从暂停恢复播放
     */
    override fun start() {
        try {
            exoPlayer?.playWhenReady = true
        } catch (e: Exception) {
            log(TAG, "start error: ${e.message}")
        }
    }

    /**
     * 暂停播放
     */
    override fun pause() {
        try {
            exoPlayer?.playWhenReady = false
        } catch (e: Exception) {
            log(TAG, "pause error: ${e.message}")
        }
    }

    /**
     * 停止播放并重置
     */
    override fun stop() {
        try {
            exoPlayer?.stop()
        } catch (e: Exception) {
            log(TAG, "stop error: ${e.message}")
        }
    }

    /**
     * 重置引擎状态
     *
     * 注意：ExoPlayer 没有 reset 方法，使用 stop 替代。
     */
    override fun reset() {
        // ExoPlayer 不需要显式 reset，stop 后可以重新 setMediaItem + prepare
    }

    /**
     * 释放所有资源
     */
    override fun release() {
        try {
            exoPlayer?.apply {
                removeListener(exoPlayerListener)
                release()
            }
        } catch (_: Exception) {}
        exoPlayer = null
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
        this.uri = uri
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .build()

        exoPlayer?.setMediaItem(mediaItem)
    }

    /**
     * 设置 assets 目录下的视频文件
     *
     * 注意：ExoPlayer 不直接支持 assets，
     * 此方法仅做记录，实际由协调器处理文件复制。
     */
    override fun setAssetSource(path: String) {
        log(TAG, "setAssetSource: $path (will be handled by coordinator)")
    }

    // ==================== 播放控制 ====================

    /**
     * 跳转到指定位置
     */
    override fun seekTo(positionMs: Long) {
        try {
            exoPlayer?.seekTo(positionMs)
        } catch (e: Exception) {
            log(TAG, "seekTo error: ${e.message}")
        }
    }

    /**
     * 设置播放速度
     */
    override fun setSpeed(speed: Float) {
        try {
            exoPlayer?.playbackParameters = PlaybackParameters(speed)
        } catch (e: Exception) {
            log(TAG, "setSpeed error: ${e.message}")
        }
    }

    /**
     * 设置音量
     */
    override fun setVolume(volume: Float) {
        try {
            exoPlayer?.volume = volume
        } catch (e: Exception) {
            log(TAG, "setVolume error: ${e.message}")
        }
    }

    /**
     * 设置循环模式
     */
    override fun setLoopMode(mode: LoopMode) {
        try {
            val repeatMode = when (mode) {
                LoopMode.SINGLE -> Player.REPEAT_MODE_ONE
                LoopMode.ALL -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }
            exoPlayer?.repeatMode = repeatMode
        } catch (e: Exception) {
            log(TAG, "setLoopMode error: ${e.message}")
        }
    }

    /**
     * 设置渲染 Surface
     */
    override fun setSurface(surface: Surface?, render: IVideoRender) {
        if (!render.needSetSurface()) {
            log(TAG, "setSurface: ${render.renderName} (not needed)")
            return
        }
        try {
            exoPlayer?.setVideoSurface(surface)
        } catch (e: Exception) {
            log(TAG, "setSurface error: ${e.message}")
        }
    }

    // ==================== 状态查询 ====================

    /**
     * 获取当前播放位置（毫秒）
     */
    override fun getCurrentPosition(): Long {
        return try {
            exoPlayer?.currentPosition ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 获取视频总时长（毫秒）
     */
    override fun getDuration(): Long {
        return try {
            val dur = exoPlayer?.duration ?: 0L
            if (dur == C.TIME_UNSET || dur < 0) 0L else dur
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 是否正在播放
     */
    override fun isPlaying(): Boolean {
        return try {
            exoPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 获取视频宽度（像素）
     */
    override fun getVideoWidth(): Int {
        return try {
            exoPlayer?.videoSize?.width ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取视频高度（像素）
     */
    override fun getVideoHeight(): Int {
        return try {
            exoPlayer?.videoSize?.height ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取网络下载速度（ExoPlayer 不直接支持此功能，返回 0）
     */
    override fun getTcpSpeed(): Long {
        return 0L
    }

    override fun getCurrentUri(): Uri? {
        return uri
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

    // ==================== ExoPlayer 访问 ====================

    /**
     * 获取内部 ExoPlayer 实例（用于与 PlayerView 绑定）
     *
     * 此方法主要用于 [ExoPlayerViewRender] 绑定 PlayerView。
     * 一般情况下不需要直接调用此方法。
     *
     * @return ExoPlayer 实例，如果未初始化则返回 null
     */
    fun getExoPlayer(): ExoPlayer? = exoPlayer

    // ==================== 内部方法 ====================

    /**
     * 计算缓冲百分比
     */
    private fun getBufferedPercent(): Int {
        val dur = try {
            val d = getDuration()
            if (d <= 0) return 0 else d
        } catch (_: Exception) { return 0 }

        val buffered = try { exoPlayer?.totalBufferedDuration ?: 0L } catch (_: Exception) { 0L }
        return ((buffered.toFloat() / dur.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * 将 ExoPlayer 错误映射到 PlayerErrorCode
     */
    private fun mapExoPlayerError(error: PlaybackException): Int {
        return when (error.cause) {
            is java.io.FileNotFoundException -> PlayerErrorCode.FILE_NOT_FOUND
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> PlayerErrorCode.NETWORK_CONNECTION_FAILED
            is javax.net.ssl.SSLException -> PlayerErrorCode.SSL_ERROR
            is java.io.IOException -> PlayerErrorCode.FILE_READ_ERROR
            else -> PlayerErrorCode.EXO_PLAYER_INTERNAL_ERROR
        }
    }
}
