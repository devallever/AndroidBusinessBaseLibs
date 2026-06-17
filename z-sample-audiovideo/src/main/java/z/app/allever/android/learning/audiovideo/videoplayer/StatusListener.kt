package z.app.allever.android.learning.audiovideo.videoplayer

interface StatusListener {
    fun onPrepare(duration: Long)
    fun onVideoPlay()
    fun onVideoPause()
    fun onVideoError()
    fun onVideoPlaying(currentPosition: Int)
}