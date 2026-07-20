package app.allever.android.lib.camera.core

import java.io.File

interface ResultCallback {
    fun onSuccess(file: File)
    fun onFailure(message: String)
    fun onVideoProgress(progress: Long) {}
}