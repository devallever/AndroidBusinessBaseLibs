package app.allever.android.sample.audiovideo.lib.kernal

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
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.player.core.LoopMode
import app.allever.android.lib.player.core.PlayerErrorCode
import app.allever.android.sample.audiovideo.lib.kernal.BasePlayerKernal
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

class Media3PlayerKernal: BasePlayerKernal<ExoPlayer>() {

    private val exoPlayerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            log(TAG, "onPlaybackStateChanged: $playbackState")
            mMainHandler.post {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_BUFFERING -> {
                        mListener?.onBufferingUpdate(getBufferedPercent())
                    }
                    Player.STATE_READY -> {
                        mListener?.onPrepared()
                    }
                    Player.STATE_ENDED -> {
                        mListener?.onCompletion()
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            log(TAG, "onIsPlayingChanged: $isPlaying")
            mMainHandler.post {
                mListener?.onIsPlayingChanged(isPlaying)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            log(TAG, "onPlayerError: ${error.errorCodeName} -> ${error.message}")
            mMainHandler.post {
                // 将 ExoPlayer 错误映射到 PlayerErrorCode
                val errorCode = mapExoPlayerError(error)
                val errorMsg = PlayerErrorCode.formatError(errorCode, error.message)
                mListener?.onError(errorCode, errorMsg)
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            log(TAG, "onVideoSizeChanged:  ${videoSize.width} x ${videoSize.height}")
            mMainHandler.post {
                mListener?.onVideoSizeChanged(videoSize.width, videoSize.height)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            //log
            log(TAG, "onEvents: ${events.toJson()}")
            mMainHandler.post {
                // 缓冲进度更新
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                    events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    val percent = getBufferedPercent()
                    mListener?.onBufferingUpdate(percent)
                }
            }

        }
    }

    init {
        init()
    }

    override fun init() {
        val context = App.Companion.context.applicationContext
        mPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(exoPlayerListener)
        }

        log(TAG, "ExoPlayer initialized")
    }

    override fun setSurface(surface: Surface?) {
        try {
            mPlayer?.setVideoSurface(surface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setSource(
        source: Uri,
        headers: Map<String, String>?
    ) {
        try {
            val mediaItem = MediaItem.Builder().setUri(source).build()

            mPlayer?.apply {
                setMediaItem(mediaItem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setAssetSource(source: String) {
    }

    override fun prepareAsync() {
        try {
            mPlayer?.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun start() {
        try {
//            mPlayer?.play()
            mPlayer?.playWhenReady = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pause() {
        try {
//            mPlayer?.pause()
            mPlayer?.playWhenReady = false
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
//            mPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        try {
            mPlayer?.apply {
                removeListener(exoPlayerListener)
                release()
            }
            mPlayer = null
            mListener = null
        } catch (e: Exception) {
            e.printStackTrace()
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
            mPlayer?.volume = volume
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loopMode(mode: LoopMode) {
        try {
            val repeatMode = when (mode) {
                LoopMode.SINGLE -> Player.REPEAT_MODE_ONE
                LoopMode.ALL -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }
            mPlayer?.repeatMode = repeatMode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun speed(speed: Float) {
        try {
//            mPlayer?.setPlaybackSpeed(speed)
            mPlayer?.playbackParameters = PlaybackParameters(speed)
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
            if (dur == C.TIME_UNSET || dur < 0) 0L else dur
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getTcpSpeed(): Long {
        return 0L
    }

    override fun getVideoWidth(): Int {
        return try {
            mPlayer?.videoSize?.width ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getVideoHeight(): Int {
        return try {
            mPlayer?.videoSize?.height ?: 0
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

    override fun getEnginePlayer(): ExoPlayer? {
        return mPlayer
    }

    /**
     * 计算缓冲百分比
     */
    private fun getBufferedPercent(): Int {
        val dur = try {
            val d = getDuration()
            if (d == C.TIME_UNSET || d <= 0) return 0 else d
        } catch (_: Exception) { return 0 }

        val buffered = try { mPlayer?.totalBufferedDuration ?: 0L } catch (_: Exception) { 0L }
        return ((buffered.toFloat() / dur.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * 将 ExoPlayer 的错误映射到 PlayerErrorCode
     *
     * @param error ExoPlayer PlaybackException
     * @return 对应的 PlayerErrorCode
     */
    private fun mapExoPlayerError(error: PlaybackException): Int {
        // 根据异常类型判断错误代码，避免使用可能不存在的常量
        return when (error.cause) {
            is FileNotFoundException -> PlayerErrorCode.FILE_NOT_FOUND
            is SocketTimeoutException, is ConnectException -> PlayerErrorCode.NETWORK_CONNECTION_FAILED
            is SSLException -> PlayerErrorCode.SSL_ERROR
            is IOException -> PlayerErrorCode.FILE_READ_ERROR
            else -> PlayerErrorCode.EXO_PLAYER_INTERNAL_ERROR
        }
    }
}