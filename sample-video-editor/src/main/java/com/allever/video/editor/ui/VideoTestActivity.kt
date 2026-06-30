//package com.videoeditor.ui
//
//import android.graphics.SurfaceTexture
//import android.media.AudioManager
//import android.media.MediaPlayer
//import android.os.Bundle
//import android.view.Surface
//import android.view.TextureView
//import android.widget.VideoView
//import com.photoeditor.R
//import Base2Activity
//import java.io.File
//
//class VideoTestActivity: Base2Activity(),TextureView.SurfaceTextureListener {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_video_test)
//
////        val vv = findViewById<VideoView>(R.id.video_view)
////        vv.setVideoPath("/storage/emulated/0/DCIM/Camera/VID_20181212_171919.mp4")
////        vv.start()
//
//        val textureView = findViewById<TextureView>(R.id.texture_view)
//        textureView.surfaceTextureListener = this
//    }
//
//
//    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
//
//    }
//
//    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//    }
//
//    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
//        return true
//    }
//
//    private var mMediaPlayer: MediaPlayer? = null
//    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
//        mMediaPlayer = MediaPlayer()
//        mMediaPlayer?.setSurface(Surface(surface))
//        mMediaPlayer?.setDataSource("/storage/emulated/0/DCIM/Camera/VID_20181212_171919.mp4")
//        mMediaPlayer?.prepareAsync()
//        mMediaPlayer?.setOnPreparedListener {
////            it.start()
//        }
//
//    }
//
//}