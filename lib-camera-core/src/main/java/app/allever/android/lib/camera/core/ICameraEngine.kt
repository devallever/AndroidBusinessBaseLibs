package app.allever.android.lib.camera.core

interface ICameraEngine {
    fun openCamera(cameraFacing: CameraFacing = CameraFacing.FACE_BACK)
    fun closeCamera()
    fun switchCamera()
    fun takePicture(resultCallback: ResultCallback?)
    fun startRecordVideo(resultCallback: ResultCallback?)
    fun stopRecordVideo()
    fun release()
}