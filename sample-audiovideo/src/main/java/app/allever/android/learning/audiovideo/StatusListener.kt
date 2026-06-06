package app.allever.android.learning.audiovideo

interface StatusListener {
    fun onPrepare(duration: Long)
    fun onVideoPlay()
    fun onVideoPause()
    fun onVideoError()
    fun onVideoPlaying(currentPosition: Int)
}