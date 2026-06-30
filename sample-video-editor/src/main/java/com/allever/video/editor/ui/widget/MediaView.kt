package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.DeviceUtils

import com.allever.video.editor.R

class MediaView : FrameLayout {
    private val TAG = MediaView::class.java.name
    private var surfaceView: SurfaceView? = null
    private var imageView: ImageView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var dataSource: String? = null
    private var needStart = false
    private var surfaceCreated = false
    private var prepared = false
    private var startDelay = 0L

    var isLooping: Boolean = false

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        initView()
        initPlayer()
    }

    fun setDefaultImage(id: Int) {
        imageView?.setImageResource(id)
    }

    fun setDefaultImage(bitmap: Bitmap) {
        imageView?.setImageBitmap(bitmap)
    }

    fun setDataSource(path: String?) {
        if (path == null) {
            return
        }
        prepared = false
        try {
            mediaPlayer?.setDataSource(path)
            if (surfaceCreated) {
                mediaPlayer?.prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun _start() {
        if (mediaPlayer == null) {
            initPlayer()
        }
        val runnable = Runnable {
            imageView?.visibility = View.GONE
            surfaceView?.bringToFront()
            try {
                val videoWidth = mediaPlayer?.videoWidth?:-1
                val videoHeight = mediaPlayer?.videoHeight ?: -1

                val lp = layoutParams
                lp.width = DeviceUtils.SCREEN_WIDTH_PX
                lp.height = videoHeight * lp.width / videoWidth

                mediaPlayer?.start()
            } catch (e: Exception) {

            }
        }
        if (startDelay > 0) {
            _end()
            handler.postDelayed(runnable, startDelay)
        } else {
            runnable.run()
        }
    }

    private fun _end() {
        imageView?.visibility = View.VISIBLE
        imageView?.bringToFront()
    }

    private fun _prepare() {
        try {
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {

        }
    }

    fun start(delay: Long = 0) {
        startDelay = delay
        needStart = true
        if (prepared && mediaPlayer?.isPlaying != true) {
            _start()
        }
    }

    fun pause() {
        val mediaPlayer = mediaPlayer ?: return
        if (mediaPlayer.isPlaying) {
            try {
                mediaPlayer.pause()
            } catch (e: Exception) {

            }
        }
    }

    fun release() {
        val mediaPlayer = mediaPlayer ?: return
        try {
            mediaPlayer.reset()
            mediaPlayer.release()
        } catch (e: Exception) {

        }
        this.mediaPlayer = null
    }

    fun seekTo(msec: Int) {
        try {
            mediaPlayer?.seekTo(msec)
        } catch (e: Exception) {

        }
    }

    private fun initView() {
        imageView = findViewById(R.id.image_view) as? ImageView
        val surfaceView = findViewById(R.id.surfaceView) as? SurfaceView
        this.surfaceView = surfaceView
        if (surfaceView != null) {
            surfaceView.setZOrderOnTop(false)
//            surfaceView.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceView.holder.addCallback(playerListener)
        }
        imageView?.visibility = View.VISIBLE
        imageView?.bringToFront()
    }

    private fun initPlayer() {
        val player = MediaPlayer()
        if (surfaceCreated) {
            player.setDisplay(surfaceView?.holder)
        }

        player.setOnCompletionListener(playerListener)
        player.setOnErrorListener(playerListener)
        player.setOnInfoListener(playerListener)
        player.setOnPreparedListener(playerListener)
        player.setOnSeekCompleteListener(playerListener)
        player.setOnVideoSizeChangedListener(playerListener)
        setDataSource(dataSource)
        mediaPlayer = player
    }

    private val playerListener = object :
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnInfoListener,
            MediaPlayer.OnPreparedListener,
            MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnVideoSizeChangedListener,
            SurfaceHolder.Callback {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param mp the MediaPlayer that reached the end of the file
         */
        override fun onCompletion(mp: MediaPlayer) {
            DLog.d(TAG, "onCompletion")
            if (isLooping) {
                _start()
            } else {
                _end()
            }
        }

        /**
         * Called to indicate an error.
         *
         * @param mp      the MediaPlayer the error pertains to
         * @param what    the type of error that has occurred:
         *
         *  * [.MEDIA_ERROR_UNKNOWN]
         *  * [.MEDIA_ERROR_SERVER_DIED]
         *
         * @param extra an extra code, specific to the error. Typically
         * implementation dependent.
         *
         *  * [.MEDIA_ERROR_IO]
         *  * [.MEDIA_ERROR_MALFORMED]
         *  * [.MEDIA_ERROR_UNSUPPORTED]
         *  * [.MEDIA_ERROR_TIMED_OUT]
         *  * `MEDIA_ERROR_SYSTEM (-2147483648)` - low-level system error.
         *
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            DLog.d(TAG, "onError: what=$what, extra=$extra")
            _end()
            return true
        }

        /**
         * Called to indicate an info or a warning.
         *
         * @param mp      the MediaPlayer the info pertains to.
         * @param what    the type of info or warning.
         *
         *  * [.MEDIA_INFO_UNKNOWN]
         *  * [.MEDIA_INFO_VIDEO_TRACK_LAGGING]
         *  * [.MEDIA_INFO_VIDEO_RENDERING_START]
         *  * [.MEDIA_INFO_BUFFERING_START]
         *  * [.MEDIA_INFO_BUFFERING_END]
         *  * `MEDIA_INFO_NETWORK_BANDWIDTH (703)` -
         * bandwidth information is available (as `extra` kbps)
         *  * [.MEDIA_INFO_BAD_INTERLEAVING]
         *  * [.MEDIA_INFO_NOT_SEEKABLE]
         *  * [.MEDIA_INFO_METADATA_UPDATE]
         *  * [.MEDIA_INFO_UNSUPPORTED_SUBTITLE]
         *  * [.MEDIA_INFO_SUBTITLE_TIMED_OUT]
         *
         * @param extra an extra code, specific to the info. Typically
         * implementation dependent.
         * @return True if the method handled the info, false if it didn't.
         * Returning false, or not having an OnInfoListener at all, will
         * cause the info to be discarded.
         */
        override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            DLog.d(TAG, "onInfo: what=$what, extra=$extra")
            return false
        }

        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the MediaPlayer that is ready for playback
         */
        override fun onPrepared(mp: MediaPlayer?) {
            DLog.d(TAG, "onPrepared")
            prepared = true
            imageView?.visibility = View.GONE
            surfaceView?.bringToFront()
            if (needStart) {
                _start()
            }
        }

        /**
         * Called to indicate the completion of a seek operation.
         *
         * @param mp the MediaPlayer that issued the seek operation
         */
        override fun onSeekComplete(mp: MediaPlayer?) {
            DLog.d(TAG, "onSeekComplete: ${mp?.currentPosition ?: "null"}")
        }

        /**
         * Called to indicate the video size
         *
         * The video size (width and height) could be 0 if there was no video,
         * no display surface was set, or the value was not determined yet.
         *
         * @param mp        the MediaPlayer associated with this callback
         * @param width     the width of the video
         * @param height    the height of the video
         */
        override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
            DLog.d(TAG, "onVideoSizeChanged: width=$width, height=$height")
        }

        /**
         * This is called immediately after any structural changes (format or
         * size) have been made to the surface.  You should at this point update
         * the imagery in the surface.  This method is always called at least
         * once, after [.surfaceCreated].
         *
         * @param holder The SurfaceHolder whose surface has changed.
         * @param format The new PixelFormat of the surface.
         * @param width The new width of the surface.
         * @param height The new height of the surface.
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            DLog.d(TAG, "surfaceChanged: format=$format, width=$width, height=$height")
        }

        /**
         * This is called immediately before a surface is being destroyed. After
         * returning from this call, you should no longer try to access this
         * surface.  If you have a rendering thread that directly accesses
         * the surface, you must ensure that thread is no longer touching the
         * Surface before returning from this function.
         *
         * @param holder The SurfaceHolder whose surface is being destroyed.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            DLog.d(TAG, "surfaceDestroyed")
            surfaceCreated = false
            pause()
        }

        /**
         * This is called immediately after the surface is first created.
         * Implementations of this should start up whatever rendering code
         * they desire.  Note that only one thread can ever draw into
         * a [Surface], so you should not draw into the Surface here
         * if your normal rendering will be in another thread.
         *
         * @param holder The SurfaceHolder whose surface is being created.
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            DLog.d(TAG, "surfaceCreated")
            surfaceCreated = true
            mediaPlayer?.setDisplay(holder)
            if (!prepared) {
                _prepare()
            } else {
                start(startDelay)
            }
        }
    }

}
