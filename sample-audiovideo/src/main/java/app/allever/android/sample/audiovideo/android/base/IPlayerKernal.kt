package app.allever.android.sample.audiovideo.android.base

import android.net.Uri
import android.view.Surface
import app.allever.android.sample.audiovideo.lib.LoopMode

interface IPlayerKernal {
    /**
     * 初始化播放器
     */
    fun init()
    fun setSurface(surface: Surface)

    fun setSource(source: Uri, headers: Map<String, String>?)
    fun setAssetSource(source: String)

    fun prepareAsync()

    /**
     * 播放控制
     */
    fun start()
    fun pause()
    fun stop()
    fun reset()
    fun release()
    fun seekTo(position: Long)
    fun volume(volume: Float)
    fun loopMode(mode: LoopMode)
    fun speed(speed: Float)

    fun getCurrentPosition(): Long
    fun getDuration(): Long

    fun isPlaying(): Boolean

    fun registerListener(listener: IListener)
    fun unregisterListener(listener: IListener)

    interface IListener {
        /**
         *              MediaPlayer 的监听器
         *             setOnPreparedListener(mOnPreparedListener)
         *             setOnCompletionListener(mOnCompletionListener)
         *             setOnErrorListener(mOnErrorListener)
         *             setOnBufferingUpdateListener(mOnBufferingUpdateListener)
         *             setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
         *             setOnInfoListener(mOnInfoListener)
         *
         *             ExoPlayer 的监听器
         */
        fun onPrepared()
        fun onCompletion()
        fun onError(code: Int, msg: String)
        fun onBufferingUpdate(percent: Int)
        fun onVideoSizeChanged(width: Int, height: Int)
        fun onInfo()
    }
}