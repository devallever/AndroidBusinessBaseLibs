package com.allever.video.editor.function.music

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build

class SongPlayer {
    interface OnPlayerListener {
        fun loadComplete(status: Int)
    }

    private val soundPool: SoundPool
    private var songId: Int = 0
    private var streamID: Int = 0

    var onPlayerListener: OnPlayerListener? = null

    init {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val abs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(100)   //设置允许同时播放的流的最大值
                .setAudioAttributes(abs)   //完全可以设置为null
                .build()
        } else {
            SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            /**
             * Called when a sound has completed loading.
             *
             * @param soundPool SoundPool object from the load() method
             * @param sampleId the sample ID of the sound loaded.
             * @param status the status of the load operation (0 = success)
             */
            onPlayerListener?.loadComplete(status)
        }
    }

    fun load(path: String) {
        songId = soundPool.load(path, 1)
    }

    fun unload() {
        soundPool.unload(songId)
    }

    fun play() {
        /**
         * @param soundID a soundID returned by the load() function
         * @param leftVolume left volume value (range = 0.0 to 1.0)
         * @param rightVolume right volume value (range = 0.0 to 1.0)
         * @param priority stream priority (0 = lowest priority)
         * @param loop loop mode (0 = no loop, -1 = loop forever)
         * @param rate playback rate (1.0 = normal playback, range 0.5 to 2.0)
         * @return non-zero streamID if successful, zero if failed
         */
        streamID = soundPool.play(songId, 1f, 1f, 1, 0, 1.0f)
    }

    fun stop() {
        soundPool.stop(streamID)
    }

    fun pause() {
        soundPool.pause(streamID)
    }

    fun resume() {
        soundPool.resume(streamID)
    }

    fun release() {
        soundPool.release()
    }

    fun setLoop(loop: Int) {
        // 设置指定id的音频循环播放次数
        soundPool.setLoop(streamID, loop)
    }

    fun abc() {
//        void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener)

    }
}