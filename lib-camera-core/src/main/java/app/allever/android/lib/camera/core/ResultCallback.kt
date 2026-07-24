package app.allever.android.lib.camera.core

import java.io.File

interface PhotoResultCallback {
    fun onSuccess(file: File)
    fun onFailure(message: String)
}

interface VideoResultCallback {
    fun onSuccess(file: File)
    fun onFailure(message: String)
    fun onProgress(durationMillis: Long) {}
}
