package app.allever.android.lib.camera.core

import android.view.View


object CameraCore: ICameraEngine {
    private var engine: ICameraEngine = CameraEngine()
    fun setupEngine(engine: ICameraEngine) {
        this.engine = engine
    }

    override fun bindPreview(view: View) {
        engine.bindPreview(view)
    }

    override fun openCamera(cameraFacing: CameraFacing) {
        engine.openCamera(cameraFacing)
    }

    override fun closeCamera() {
        engine.closeCamera()
    }

    override fun switchCamera() {
        engine.switchCamera()
    }

    override fun takePicture(resultCallback: ResultCallback?) {
        engine.takePicture(resultCallback)
    }

    override fun startRecordVideo(resultCallback: ResultCallback?) {
        engine.startRecordVideo(resultCallback)
    }

    override fun stopRecordVideo() {
        engine.stopRecordVideo()
    }

    override fun release() {
        engine.release()
    }

}