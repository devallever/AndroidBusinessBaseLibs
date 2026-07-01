package com.step.wincash.utils

import android.media.MediaPlayer
import com.step.wincash.R
import com.step.wincash.base.BaseApplication

object MusicUtil {
    private var _mediaPlayer: MediaPlayer? = null
    private var hasInit = false

    var isOpenMusic: Boolean
        get() = SpUtil.get(SpKey.IS_MUSIC_OPEN, true)
        set(value) = SpUtil.put(SpKey.IS_MUSIC_OPEN, value)

    fun init() {
        if (!hasInit){
            hasInit = true
            _mediaPlayer = MediaPlayer.create(BaseApplication.instance, R.raw.bgm)
            _mediaPlayer!!.isLooping = true
            _mediaPlayer!!.setOnErrorListener { _, _, _ ->
                _mediaPlayer!!.reset()
                false
            }
            play()
        }

    }

    fun pause() {
        runCatching {
            if (_mediaPlayer?.isPlaying==true) {
                _mediaPlayer!!.pause()
            }
        }
    }


    fun play() {
        runCatching {
            if (isOpenMusic) {
                _mediaPlayer?.start()
            }
        }
    }

    fun release() {
        runCatching {
            if (_mediaPlayer?.isPlaying == true) {
                _mediaPlayer!!.stop()
            }
        }
        if (_mediaPlayer != null) {
            _mediaPlayer!!.release()
            _mediaPlayer = null
        }
    }
}