package com.allever.video.editor.function.music

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.Log

class SoundVisualizer {
    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null

    fun init(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer

        val visualizer = Visualizer(mediaPlayer.audioSessionId)
        this.visualizer = visualizer
        visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {
                var v: Long = 0
                for (i in waveform.indices) {
                    v += Math.pow(waveform[i].toDouble(), 2.0).toLong()
                }

                val volume = 10 * Math.log10(v / waveform.size.toDouble())

//                currentVolume = volume.toInt()
//                Log.i("xiaozhu", "waveform: ${waveform.size} volume: ${volume.toInt()}")

            }

            override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true)


    }

    fun start() {
        visualizer?.enabled = true
    }

    fun stop() {
        visualizer?.enabled = false
    }

    fun release() {
        visualizer?.enabled = false
        visualizer?.release()
    }
}