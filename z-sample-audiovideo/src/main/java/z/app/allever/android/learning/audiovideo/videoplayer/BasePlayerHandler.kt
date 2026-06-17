package z.app.allever.android.learning.audiovideo.videoplayer

import android.media.MediaPlayer
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.function.work.TimerTask2
import app.allever.android.lib.media.core.model.MediaItem

abstract class BasePlayerHandler : MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

    protected var mMediaPlayer: MediaPlayer? = null
    protected lateinit var mMediaBean: MediaItem
    protected var mStatusListener: StatusListener? = null

    protected val TAG = this::class.java.simpleName

    private val timerTask = TimerTask2(null, 1000L, true) {
        mStatusListener?.onVideoPlaying(mMediaPlayer?.currentPosition ?: 0)
    }

    fun isPlaying(): Boolean = mMediaPlayer?.isPlaying ?: false

    fun getMediaPlayer() = mMediaPlayer

    open fun play() {
        mStatusListener?.onVideoPlay()
        timerTask.start()
    }

    open fun pause() {
        timerTask.cancel()
        mStatusListener?.onVideoPause()
    }

    open fun stop() {
        timerTask.cancel()
        mStatusListener?.onVideoPause()
    }

    open fun seekTo(value: Int) {
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mStatusListener?.onVideoError()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        log(TAG, "onPrepared")
        if (mMediaPlayer == null) {
            mMediaPlayer = mp
        }
        //适应屏幕显示
        mMediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        //显示第一帧
        seekTo(1)
        val duration = (mMediaBean as? MediaItem.Video)?.duration ?: 0.toLong()
        mStatusListener?.onPrepare(duration)
        log("duration = ${mMediaPlayer?.duration}")
    }
}