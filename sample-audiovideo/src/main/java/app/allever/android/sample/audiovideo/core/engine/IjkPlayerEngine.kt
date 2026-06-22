package app.allever.android.sample.audiovideo.core.engine

import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.core.render.IVideoRender
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * IJKPlayer 引擎实现（基于 Bilibili IJKPlayer / FFmpeg）
 *
 * ## 职责
 * - 封装 IJKPlayer (基于 FFmpeg) 的完整生命周期管理
 * - 将 IJKPlayer 的事件转换为 [IPlayerEngineListener] 回调
 * - 提供 IJKPlayer 特有的高级功能（TCP 速度、调试信息等）
 *
 * ## IJKPlayer 特点（与 MediaPlayer/ExoPlayer 的区别）
 * **优势：**
 * ✅ **格式支持最广**：基于 FFmpeg，支持几乎所有视频格式和编解码器
 * ✅ **协议支持丰富**：RTMP、RTSP、HLS、DASH 等流媒体协议
 * ✅ **跨平台一致性**：Android/iOS 行为一致
 * ✅ **高度可定制**：可修改源码实现特殊需求
 * ✅ **调试功能强大**：内置详细日志、VLC 风格调试命令
 * ✅ **网络速度监控**：可获取 TCP 下载速度
 *
 * **劣势：**
 * ❌ **编译复杂**：需要 NDK 环境，编译时间长
 * ❌ **体积较大**：完整编译约 15-20MB，精简后 5-10MB
 * ❌ **API 不够友好**：接口设计较底层（类似 C API）
 * ❌ **维护风险**：主要依赖 Bilibili 维护，更新频率不如 Google
 * ❌ **学习成本高**：需要了解 FFmpeg 基础概念
 *
 * ## 适用场景
 * 🎥 **播放老旧/特殊格式的视频**（AVI、RMVB、FLV 等）
 * 📡 **直播应用**（RTMP/RTSP 协议）
 * 🎯 **需要极致格式兼容性**
 * 🔄 **跨平台项目**（Android + iOS 需保持一致行为）
 *
 * ## 不适用场景
 * - 简单的本地视频播放（MediaPlayer 足够）
 * - 对 APK 体积有严格限制的应用
 * - 快速原型开发
 *
 * ## 使用示例
 * ```kotlin
 * val engine = IjkPlayerEngine()
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
class IjkPlayerEngine : IPlayerEngine {

    companion object {
        private const val TAG = "IjkPlayerEngine"

        init {
            // 加载 IJKPlayer 原生库（如果尚未加载）
            try {
                System.loadLibrary("ijkplayer")
                log(TAG, "IJKPlayer native library loaded")
            } catch (_: UnsatisfiedLinkError) {
                // 可能已经加载或使用其他方式加载
                log(TAG, "IJKPlayer library already loaded or using different loading method")
            }
        }
    }

    /** IjkMediaPlayer 实例 */
    private var ijkPlayer: IjkMediaPlayer? = null

    /** 引擎事件监听器 */
    private var listener: IPlayerEngineListener? = null

    /** TCP 下载速度（字节/秒）*/
    @Volatile
    private var tcpSpeedValue: Long = 0L

    // ==================== IJKPlayer 监听器 ====================

    /** 准备完成监听器 */
    private val preparedListener = IMediaPlayer.OnPreparedListener {
        log(TAG, "onPrepared")
        App.mainHandler.post {
            listener?.onPrepared()
        }
    }

    /** 播放完成监听器 */
    private val completionListener = IMediaPlayer.OnCompletionListener {
        log(TAG, "onCompletion")
        App.mainHandler.post {
            listener?.onCompletion()
        }
    }

    /** 错误监听器 */
    private val errorListener = IMediaPlayer.OnErrorListener { _, what, extra ->
        log(TAG, "onError: what=$what, extra=$extra")
        App.mainHandler.post {
            val errorCode = mapIjkPlayerError(what)
            val errorMsg = PlayerErrorCode.formatError(errorCode, "IjkMediaPlayer error: what=$what, extra=$extra")
            listener?.onError(errorCode, errorMsg)
        }
        true // 返回 true 表示已处理此错误
    }

    /** 缓冲进度监听器 */
    private val bufferingUpdateListener = IMediaPlayer.OnBufferingUpdateListener { _, percent ->
        App.mainHandler.post {
            if (percent > 0) {
                listener?.onBufferingUpdate(percent)
            }
        }
    }

    /** 视频尺寸变化监听器 */
    private val videoSizeChangedListener = IMediaPlayer.OnVideoSizeChangedListener { _, width, height, _, _ ->
        log(TAG, "onVideoSizeChanged: ${width}x${height}")
        App.mainHandler.post {
            listener?.onVideoSizeChanged(width, height)
        }
    }

    /** 信息监听器（包含缓冲状态、网络带宽等）*/
    private val infoListener = IMediaPlayer.OnInfoListener { _, what, extra ->
        log(TAG, "onInfo: what=$what, extra=$extra")
        App.mainHandler.post {
            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    // 视频开始渲染（首帧显示）
                    log(TAG, "视频渲染开始（首帧显示）")
                    listener?.onInfo()
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    // 开始缓冲
                    log(TAG, "开始缓冲")
                    listener?.onInfo()
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    // 缓冲结束
                    log(TAG, "缓冲结束")
                    listener?.onInfo()
                }
                IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> {
                    // 网络带宽信息（单位：bps）
                    log(TAG, "网络带宽: $extra bps")
                    tcpSpeedValue = extra.toLong()
                }
            }
            true
        }
    }

    /** Seek 完成监听器 */
    private val seekCompleteListener = IMediaPlayer.OnSeekCompleteListener {
        log(TAG, "onSeekComplete")
    }

    // ==================== 生命周期管理 ====================

    /**
     * 初始化引擎
     *
     * 创建 IjkMediaPlayer 实例并配置默认参数。
     */
    override fun init() {
        if (ijkPlayer != null) return

        try {
            ijkPlayer = IjkMediaPlayer().apply {
                // 设置日志级别
                IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO)

                // 配置默认选项
                setDefaultOptions()

                // 注册所有监听器
                setOnPreparedListener(preparedListener)
                setOnCompletionListener(completionListener)
                setOnErrorListener(errorListener)
                setOnBufferingUpdateListener(bufferingUpdateListener)
                setOnVideoSizeChangedListener(videoSizeChangedListener)
                setOnInfoListener(infoListener)
                setOnSeekCompleteListener(seekCompleteListener)

                // 设置音频类型
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            log(TAG, "initialized")
        } catch (e: Exception) {
            log(TAG, "init fail: ${e.message}")
            e.printStackTrace()
            listener?.onError(-1, "init fail")
        }
    }

    /**
     * 异步准备数据源
     */
    override fun prepareAsync() {
        ijkPlayer?.prepareAsync()
    }

    /**
     * 开始播放 或 从暂停恢复播放
     */
    override fun start() {
        try {
            ijkPlayer?.start()
        } catch (e: Exception) {
            log(TAG, "start error: ${e.message}")
        }
    }

    /**
     * 暂停播放
     */
    override fun pause() {
        try {
            ijkPlayer?.pause()
        } catch (e: Exception) {
            log(TAG, "pause error: ${e.message}")
        }
    }

    /**
     * 停止播放并重置
     */
    override fun stop() {
        try {
            ijkPlayer?.stop()
        } catch (e: Exception) {
            log(TAG, "stop error: ${e.message}")
        }
    }

    /**
     * 重置引擎状态
     */
    override fun reset() {
        try {
            ijkPlayer?.reset()
        } catch (e: Exception) {
            log(TAG, "reset error: ${e.message}")
        }
    }

    /**
     * 释放所有资源
     *
     * 注意：IJKPlayer 的 release 是同步操作且可能耗时，
     * 因此在 IO 线程异步执行以避免阻塞主线程。
     */
    override fun release() {
        clearListeners()

        // 异步释放 IJKPlayer（防止阻塞主线程）
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ijkPlayer?.release()
                log(TAG, "IjkMediaPlayer released")
            } catch (e: Exception) {
                log(TAG, "release error: ${e.message}")
            } finally {
                ijkPlayer = null
            }
        }
        listener = null
        log(TAG, "release called")
    }

    // ==================== 数据源设置 ====================

    /**
     * 设置视频数据源
     *
     * 支持 http/https/file/content 协议。
     */
    override fun setSource(uri: Uri, headers: Map<String, String>?) {
        when (uri.scheme) {
            "http", "https" -> {
                // 在线视频（带请求头）
                ijkPlayer?.setDataSource(App.context, uri, headers)
            }
            "content" -> {
                // Content Provider
                ijkPlayer?.setDataSource(App.context, uri)
            }
            else -> {
                // 本地文件（file:// 或纯路径）
                ijkPlayer?.dataSource = uri.toString()
            }
        }
    }

    /**
     * 设置 assets 目录下的视频文件
     *
     * 注意：IJKPlayer 不直接支持 assets，
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
            ijkPlayer?.seekTo(positionMs)
        } catch (e: Exception) {
            log(TAG, "seekTo error: ${e.message}")
        }
    }

    /**
     * 设置播放速度
     */
    override fun setSpeed(speed: Float) {
        try {
            ijkPlayer?.setSpeed(speed)
        } catch (e: Exception) {
            log(TAG, "setSpeed error: ${e.message}")
        }
    }

    /**
     * 设置音量
     */
    override fun setVolume(volume: Float) {
        try {
            ijkPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            log(TAG, "setVolume error: ${e.message}")
        }
    }

    /**
     * 设置循环模式
     */
    override fun setLoopMode(mode: LoopMode) {
        try {
            ijkPlayer?.isLooping = (mode == LoopMode.SINGLE || mode == LoopMode.ALL)
        } catch (e: Exception) {
            log(TAG, "setLoopMode error: ${e.message}")
        }
    }

    /**
     * 设置渲染 Surface
     */
    override fun setSurface(surface: Surface?, render: IVideoRender) {
        try {
            ijkPlayer?.setSurface(surface)
            if (surface == null) {
                ijkPlayer?.setDisplay(null)
            }
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
            ijkPlayer?.currentPosition ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 获取视频总时长（毫秒）
     */
    override fun getDuration(): Long {
        return try {
            ijkPlayer?.duration ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 是否正在播放
     */
    override fun isPlaying(): Boolean {
        return try {
            ijkPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 获取视频宽度（像素）
     */
    override fun getVideoWidth(): Int {
        return try {
            ijkPlayer?.videoWidth ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取视频高度（像素）
     */
    override fun getVideoHeight(): Int {
        return try {
            ijkPlayer?.videoHeight ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * 获取网络下载速度（字节/秒）
     *
     * 这是 IJKPlayer 特有的功能，通过 MEDIA_INFO_NETWORK_BANDWIDTH 回调获取。
     */
    override fun getTcpSpeed(): Long {
        return tcpSpeedValue
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
     * 清除所有监听器
     */
    private fun clearListeners() {
        try {
            ijkPlayer?.setOnPreparedListener(null)
            ijkPlayer?.setOnCompletionListener(null)
            ijkPlayer?.setOnErrorListener(null)
            ijkPlayer?.setOnBufferingUpdateListener(null)
            ijkPlayer?.setOnVideoSizeChangedListener(null)
            ijkPlayer?.setOnInfoListener(null)
            ijkPlayer?.setOnSeekCompleteListener(null)
        } catch (_: Exception) {}
    }

    /**
     * 设置 IJKPlayer 默认配置选项
     *
     * 这些选项优化了播放性能和兼容性：
     * - 启用硬解码（降低 CPU 占用）
     * - 优化缓冲策略（减少卡顿）
     * - 启用 RTMP 支持（直播场景）
     */
    private fun IjkMediaPlayer.setDefaultOptions() {
        try {
            // 播放器类别：1=视频 2=音频 3=音视频
            setOption(4, "mediacodec-all-videos", 1)  // 启用 MediaCodec 硬解码
            setOption(4, "mediacodec-audio", 1)         // 音频也尝试使用硬解码
            setOption(4, "opensles", 1)                  // 使用 OpenSL ES 音频输出
            setOption(4, "overlay-format", 842225444)   // Surface 格式
            setOption(4, "framedrop", 1)                 // 允许丢帧（减少卡顿）
            setOption(4, "max-fps", 30)                  // 最大帧率限制
            setOption(4, "packet-buffering", 0)          // 关闭包缓冲模式
            
            // 缓冲策略优化
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024)  // 1MB 缓冲区
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 25)               // 最小缓冲帧数
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)          // 不自动播放
            
            // 网络相关
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)                 // 自动重连
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 30000000)             // 30秒超时
            
            // RTMP 相关（直播场景）
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtmp-tcp", 1)                  // RTMP over TCP
            
            log(TAG, "default options applied")
        } catch (e: Exception) {
            log(TAG, "setDefaultOptions error: ${e.message}")
        }
    }

    /**
     * 将 IJKPlayer 错误代码映射到 PlayerErrorCode
     */
    private fun mapIjkPlayerError(what: Int): Int {
        return when (what) {
            IjkMediaPlayer.MEDIA_ERROR_UNKNOWN -> PlayerErrorCode.IJK_MEDIA_PLAYER_INTERNAL_ERROR
            IjkMediaPlayer.MEDIA_ERROR_SERVER_DIED -> PlayerErrorCode.SERVER_ERROR
            IjkMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            IjkMediaPlayer.MEDIA_ERROR_IO -> PlayerErrorCode.FILE_READ_ERROR
            IjkMediaPlayer.MEDIA_ERROR_MALFORMED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            IjkMediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            IjkMediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlayerErrorCode.NETWORK_TIMEOUT
            else -> PlayerErrorCode.UNKNOWN
        }
    }
}
