package app.allever.android.sample.audiovideo.android.base

import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.TimeHelper.formatTime
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class IjkPlayerKernal: BasePlayerKernal<IjkMediaPlayer>() {

    private val mOnSeekCompleteListener = IMediaPlayer.OnSeekCompleteListener {
        log(TAG, "onSeekComplete")
        mMainHandler.postDelayed({
//            mListener?.onSeekComplete()
        }, 300)
    }

    private val mOnPreparedListener = IMediaPlayer.OnPreparedListener {
        log(TAG, "onPrepared")
        mMainHandler.post {
            mListener?.onPrepared()
        }
    }

    private val mOnCompletionListener = IMediaPlayer.OnCompletionListener {
        log(TAG, "onCompletion")
        mMainHandler.post {
            mListener?.onCompletion()
        }
    }

    private val mOnErrorListener = IMediaPlayer.OnErrorListener { _, what, extra ->
        log(TAG, "onError")
        mMainHandler.post {
            val errorCode = when (what) {
                IjkMediaPlayer.MEDIA_ERROR_UNKNOWN -> PlayerErrorCode.IJK_MEDIA_PLAYER_INTERNAL_ERROR
                IjkMediaPlayer.MEDIA_ERROR_SERVER_DIED -> PlayerErrorCode.SERVER_ERROR
                else -> PlayerErrorCode.UNKNOWN
            }
            val msg = PlayerErrorCode.formatError(errorCode, "IjkMediaPlayer error: what=$what, extra=$extra")
            mListener?.onError(errorCode, msg)
        }
        true  // 返回 true 表示已处理错误
    }

    private val mOnBufferingUpdateListener = IMediaPlayer.OnBufferingUpdateListener { _, percent ->
//        log(TAG, "onBufferingUpdate: $percent")
        mMainHandler.post {
            mListener?.onBufferingUpdate(percent)
        }
    }

    private val mOnInfoListener = IMediaPlayer.OnInfoListener { _, what, extra ->
        log(TAG, "onInfo: what=$what, extra=$extra")
        mMainHandler.post {
            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    // 视频开始渲染（首帧显示）
                    log(TAG, "视频渲染开始（首帧显示）")
//                    mListener?.onInfo()
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    // 开始缓冲
                    log(TAG, "开始缓冲")
                    mListener?.onBufferingUpdate(0)
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    // 缓冲结束
                    log(TAG, "缓冲结束")
                    mListener?.onBufferingUpdate(100)
                }
                IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> {
                    // 网络带宽信息（单位：bps）
                    log(TAG, "网络带宽: $extra bps")
//                    listener?.onNetworkBandwidth(extra.toLong())
                }
            }
        }
        true
    }

    private val mOnVideoSizeChangedListener = IMediaPlayer.OnVideoSizeChangedListener { _, width, height, _, _ ->
        log(TAG, "onVideoSizeChanged: $width x $height")
        mMainHandler.post {
            mListener?.onVideoSizeChanged(width, height)
        }
    }


    init {
        init()
    }

    override fun init() {
        try {
            mPlayer = IjkMediaPlayer()

            // 1. 配置 IjkMediaPlayer
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO)

            // 2. 配置播放选项（启用硬解码、优化缓冲等）
            setDefaultOptions()

            // 3. 注册监听器
            setupListeners()

            mPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

        } catch (e: Exception) {
            log(TAG, "init fail")
            e.printStackTrace()
            mListener?.onError(-1, "init fail")
        }

    }

    override fun setSurface(surface: Surface?) {
        mPlayer?.setSurface(surface)
        if (surface == null) {
            mPlayer?.setDisplay(null)
        }
    }

    override fun setSource(
        source: Uri,
        headers: Map<String, String>?
    ) {
        // 设置数据源
        when (source.scheme) {
            "http", "https" -> {
                // 在线视频（带请求头）
                mPlayer?.setDataSource(App.context, source, headers)
            }
            "content" -> {
                // Content Provider
                mPlayer?.setDataSource(App.context, source)
            }
            else -> {
                // 本地文件（file:// 或纯路径）
                mPlayer?.dataSource = source.toString()
            }
        }
    }

    override fun setAssetSource(source: String) {
    }

    override fun prepareAsync() {
        try {
            mPlayer?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun start() {
        try {
            mPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pause() {
        try {
            mPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        try {
            mPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun reset() {
        try {
            mPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        clearListeners()

        // 释放 IjkMediaPlayer（异步释放，防止阻塞主线程）
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mPlayer?.release()
                log(TAG, "IjkMediaPlayer released")
            } catch (e: Exception) {
                e.printStackTrace()
                log(TAG, "release error: ${e.message}")
            } finally {
                mPlayer = null
            }
        }
    }

    override fun seekTo(position: Long) {
        try {
            mPlayer?.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun volume(volume: Float) {
        try {
            mPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loopMode(mode: LoopMode) {
        try {
            mPlayer?.isLooping = (mode == LoopMode.SINGLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun speed(speed: Float) {
        try {
            mPlayer?.setSpeed(speed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCurrentPosition(): Long {
        return try {
            mPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getDuration(): Long {
        return try {
            val dur = mPlayer?.duration ?: 0
            if (dur < 0) 0L else dur
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getTcpSpeed(): Long {
        return try {
            mPlayer?.tcpSpeed ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getVideoWidth(): Int {
        return try {
            return mPlayer?.videoWidth ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getVideoHeight(): Int {
        return try {
            return mPlayer?.videoHeight ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun isPlaying(): Boolean {
        return try {
            mPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun registerListener(listener: IPlayerKernal.IListener) {
        mListener = listener
    }

    override fun unregisterListener(listener: IPlayerKernal.IListener) {
        mListener = null
    }

    /**
     * 设置 IjkMediaPlayer 默认选项
     *
     * 启用的优化项：
     * - mediacodec-all：全格式硬件解码（提升性能、降低功耗）
     * - opensles：OpenSL ES 音频输出（降低延迟）
     * - framebuffer-frames：帧缓冲优化
     * - packet-buffer-size：网络包缓冲大小
     * - max-buffer-size：最大缓冲大小
     * - min-frames：最小缓冲帧数
     * - start-on-prepared：准备完成后自动开始
     * - http-dns-cache：HTTP DNS 缓存
     * - dns-cache-timeout：DNS 缓存超时时间
     * - skip-loop-filter：跳过环路滤波（提升性能）
     * - framedrop：允许丢帧（卡顿时保持流畅）
     * - infbuf：无限缓冲（防止缓冲不足导致暂停）
     *
     * 子类可覆盖此方法自定义选项。
     */
    private fun setDefaultOptions() {
        try {
            mPlayer?.apply {
                // 启用硬解码
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)

                // 音频优化
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1)  // 变速变调支持

                // 渲染优化
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32.toLong())
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)  // 允许丢帧

                // 缓冲策略
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-dns-cache-timeout", 60000000L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns-cache-timeout", 60000000L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_for_live_streaming", 1024 * 1024)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffer-size", 512 * 1024)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 0)  // 0 表示无限缓冲
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 25)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)  // 手动控制播放
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1)  // 无限缓冲

                // 性能优化
                setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)  // 跳过环路滤波
            }
        } catch (e: Exception) {
            log("IjkVideoPlayer", "setDefaultOptions error: ${e.message}")
        }
    }

    /**
     * 注册 IjkMediaPlayer 所有监听器
     */
    private fun setupListeners() {
        mPlayer?.apply {
            setOnPreparedListener(mOnPreparedListener)
            setOnCompletionListener(mOnCompletionListener)
            setOnErrorListener(mOnErrorListener)
            setOnBufferingUpdateListener(mOnBufferingUpdateListener)
            setOnInfoListener(mOnInfoListener)
            setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
            setOnSeekCompleteListener(mOnSeekCompleteListener)
            log(TAG, "setupListeners success")
        }
    }

    /**
     * 清除所有监听器引用（释放时调用，避免内存泄漏）
     */
    private fun clearListeners() {
        mPlayer?.apply {
            setOnPreparedListener(null)
            setOnCompletionListener(null)
            setOnErrorListener(null)
            setOnBufferingUpdateListener(null)
            setOnInfoListener(null)
            setOnVideoSizeChangedListener(null)
            setOnSeekCompleteListener(null)
        }
    }
}