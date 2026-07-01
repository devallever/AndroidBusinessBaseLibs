package com.allever.video.editor.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import com.airbnb.lottie.L
import com.android.absbase.helper.log.DLog

class TextureViewHolder
    : View.OnClickListener, View.OnTouchListener, TextureView.SurfaceTextureListener {

    companion object {
        private val TAG = TextureViewHolder::class.java.simpleName
    }

    private var mIvPlay: ImageView? = null
    private var mTextureView: TextureView? = null
    private var mPath: String? = null
    private var mMediaPlayer: MediaPlayer? = null


    init {
//        initVideoView()
    }

    private fun initTextureView() {
        mIvPlay?.setOnClickListener(this)
        mTextureView?.setOnTouchListener(this)
        mIvPlay?.visibility = View.VISIBLE
        mTextureView?.visibility = View.VISIBLE
        mTextureView?.surfaceTextureListener = this
    }

    fun initVideo(textureView: TextureView?, path: String?, ivPlay: ImageView?) {
        mIvPlay = ivPlay
        mTextureView = textureView
        mPath = path
        initTextureView()
    }

    fun play() {
        mMediaPlayer?.start()
//        mMediaPlayer?.prepare()
        mTextureView?.visibility = View.VISIBLE
        mIvPlay?.visibility = View.GONE
    }

    fun pause() {
        mMediaPlayer?.pause()
        mIvPlay?.visibility = View.VISIBLE
    }

    fun stop() {
        mMediaPlayer?.stop()
        mTextureView?.visibility = View.GONE
        mTextureView = null
    }

//    override fun onPrepared(it: MediaPlayer?) {
//        //显示第一帧
//        mMediaPlayer?.seekTo(100)
//        it?.setOnInfoListener { mp, what, extra ->
//            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
//                mTextureView?.setBackgroundColor(Color.TRANSPARENT)
//            }
//            return@setOnInfoListener true
//        }
//    }
//
//    override fun onCompletion(mp: MediaPlayer?) {
//        mMediaPlayer?.seekTo(100)
//        mIvPlay?.visibility = View.VISIBLE
//    }

    override fun onClick(v: View?) {
        when (v) {
            mIvPlay -> {
                play()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v) {
            mTextureView -> {
                pause()
                return true
            }
        }
        return false
    }


    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        DLog.d(TAG, "onSurfaceTextureSizeChanged()")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        DLog.d(TAG, "onSurfaceTextureUpdated()")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        DLog.d(TAG, "onSurfaceTextureDestroyed()")
//        mMediaPlayer?.stop()
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        DLog.d(TAG, "onSurfaceTextureAvailable()")
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setSurface(Surface(surface))
        mMediaPlayer?.setDataSource(mPath)
        mMediaPlayer?.prepareAsync()
        mMediaPlayer?.setOnPreparedListener {
            it?.seekTo(100)
//            it.start()
        }

        mMediaPlayer?.setOnCompletionListener {
            //                mMediaPlayer?.prepare()
            it?.seekTo(100)
            mIvPlay?.visibility = View.VISIBLE
        }
    }
}