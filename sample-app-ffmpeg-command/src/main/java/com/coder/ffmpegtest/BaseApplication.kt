package com.coder.ffmpegtest

import android.app.Application
import app.allever.android.lib.core.app.App
import com.coder.ffmpeg.jni.FFmpegCommand

/**
 * @author: AnJoiner
 * @datetime: 19-12-20
 */
object BaseApplication {
    var instance: Application? = null
        private set

     fun onCreate() {
        instance = App.app
        FFmpegCommand.setDebug(true)
    }
}