package app.allever.android.lib.camera.core

import android.view.View

interface ICameraEngine {
    fun bindPreview(view: View)
    fun openCamera(cameraFacing: CameraFacing)
    fun closeCamera()
    fun switchCamera()
    fun takePicture(resultCallback: ResultCallback?)
    fun startRecordVideo(resultCallback: ResultCallback?)
    fun stopRecordVideo()
    fun release()
    fun setConfig(config: CameraConfig)
    fun getState(): CameraState
}