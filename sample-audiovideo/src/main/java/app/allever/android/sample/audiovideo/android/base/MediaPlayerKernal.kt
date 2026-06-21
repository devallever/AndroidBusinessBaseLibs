package app.allever.android.sample.audiovideo.android.base

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerErrorCode

class MediaPlayerKernal(val context: Context): BasePlayerKernal<MediaPlayer>() {
    private val mOnPreparedListener = MediaPlayer.OnPreparedListener {
        log(TAG, "onPrepared")
        mMainHandler.post {
            mListener?.onPrepared()
        }
    }
    private val mOnCompletionListener = MediaPlayer.OnCompletionListener {
        log(TAG, "onCompletion")
        mMainHandler.post {
            mListener?.onCompletion()
        }
    }
    private val mOnErrorListener = MediaPlayer.OnErrorListener { _, what, extra ->
        log(TAG, "onError")
        mMainHandler.post {
            // 将 MediaPlayer 错误代码映射到 PlayerErrorCode
            val errorCode = mapMediaPlayerError(what, extra)
            val errorMsg = PlayerErrorCode.formatError(errorCode, "MediaPlayer error: what=$what, extra=$extra")

            mListener?.onError(errorCode, errorMsg)
        }

        true
    }
    private val mOnBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { _, percent ->
        log(TAG, "onBufferingUpdate: $percent")
        mMainHandler.post {
            mListener?.onBufferingUpdate(percent)
        }
    }
    private val mOnVideoSizeChangedListener = MediaPlayer.OnVideoSizeChangedListener { _, width, height ->
        log(TAG, "onVideoSizeChanged: $width x $height")
        mMainHandler.post {
            mListener?.onVideoSizeChanged(width, height)
        }
    }
    private val mOnInfoListener = MediaPlayer.OnInfoListener { _, what, _ ->
        log(TAG, "onInfo: $what")
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                log(TAG, "MEDIA_INFO_BUFFERING_START")
                // 可以在这里显示缓冲指示器
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                log(TAG, "MEDIA_INFO_BUFFERING_END")
                // 可以在这里隐藏缓冲指示器
            }
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                log(TAG, "MEDIA_INFO_VIDEO_RENDERING_START")
                // 视频首帧渲染
            }
        }
        mMainHandler.post {
            mListener?.onInfo()
        }
        true
    }

    init {
        init()
    }

    override fun init() {
        if (mPlayer != null) return

        mPlayer = MediaPlayer().apply {
            // 设置各种监听器
            setOnPreparedListener(mOnPreparedListener)
            setOnCompletionListener(mOnCompletionListener)
            setOnErrorListener(mOnErrorListener)
            setOnBufferingUpdateListener(mOnBufferingUpdateListener)
            setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
            setOnInfoListener(mOnInfoListener)
        }

        log("AndroidMP", "MediaPlayer initialized")

    }

    override fun setSurface(surface: Surface) {
        mPlayer?.setSurface(surface)
    }

    override fun setSource(
        source: Uri,
        headers: Map<String, String>?
    ) {
        // 设置数据源
        when (source.scheme) {
            "http", "https" -> {
                // HTTP/HTTPS 数据源
                if (headers != null) {
                    // API 21+ 支持设置请求头
                    mPlayer?.setDataSource(App.context.applicationContext, source, headers)
                } else {
                    mPlayer?.setDataSource(source.toString())
                }
            }
            "content" -> {
                // Content Provider
                mPlayer?.setDataSource(App.context.applicationContext, source)
            }
            "file" -> {
                // 本地文件
                mPlayer?.setDataSource(source.toString())
            }
            else -> {
                // 其他情况尝试直接设置
                mPlayer?.setDataSource(source.toString())
            }
        }
    }

    override fun setAssetSource(source: String) {
    }

    override fun prepareAsync() {
        mPlayer?.prepareAsync()
    }

    override fun start() {
        mPlayer?.start()
    }

    override fun pause() {
        mPlayer?.pause()
    }

    override fun stop() {
        mPlayer?.stop()
        mPlayer?.reset()
    }

    override fun reset() {
        mPlayer?.reset()
    }

    override fun release() {
        try {
            mPlayer?.apply {
                setOnPreparedListener(null)
                setOnCompletionListener(null)
                setOnErrorListener(null)
                setOnBufferingUpdateListener(null)
                setOnVideoSizeChangedListener(null)
                setOnInfoListener(null)
                release()
            }
        } catch (_: Exception) {}
        mPlayer = null
        mListener = null
        log(TAG, "released")
    }

    override fun seekTo(position: Long) {
        mPlayer?.seekTo(position.toInt())
    }

    override fun volume(volume: Float) {
        mPlayer?.setVolume(volume, volume)
    }

    override fun loopMode(mode: LoopMode) {
        val looping = when (mode) {
            LoopMode.SINGLE -> true
            LoopMode.ALL -> true
            else -> false
        }
        try {
            mPlayer?.isLooping = looping
        } catch (_: Exception) {}
    }

    override fun speed(speed: Float) {
        try {
            mPlayer?.playbackParams = android.media.PlaybackParams().setSpeed(speed)
        } catch (e: Exception) {
            log(TAG, "setSpeed error: ${e.message}")
        }
    }

    override fun getCurrentPosition(): Long {
        return (mPlayer?.currentPosition ?: 0).toLong()
    }

    override fun getDuration(): Long {
        return (mPlayer?.duration ?: 0).toLong()
    }

    override fun isPlaying(): Boolean {
        return mPlayer?.isPlaying == true
    }

    override fun registerListener(listener: IPlayerKernal.IListener) {
        mListener = listener
    }

    override fun unregisterListener(listener: IPlayerKernal.IListener) {
        mListener = null
    }

    //
    /**
     * 将 MediaPlayer 的错误代码映射到 PlayerErrorCode
     *
     * @param what MediaPlayer 错误类型
     * @param extra 额外错误信息
     * @return 对应的 PlayerErrorCode
     */
    private fun mapMediaPlayerError(what: Int, extra: Int): Int {
        return when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> PlayerErrorCode.MEDIA_PLAYER_INTERNAL_ERROR
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> PlayerErrorCode.SERVER_ERROR
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_IO -> when (extra) {
                else -> PlayerErrorCode.FILE_READ_ERROR
            }
            MediaPlayer.MEDIA_ERROR_MALFORMED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlayerErrorCode.SOURCE_FORMAT_UNSUPPORTED
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlayerErrorCode.NETWORK_TIMEOUT
            else -> PlayerErrorCode.UNKNOWN
        }
    }
}