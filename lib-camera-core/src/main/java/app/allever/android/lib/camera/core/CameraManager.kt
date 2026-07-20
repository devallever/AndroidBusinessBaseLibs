package app.allever.android.lib.camera.core


object CameraManager: ICameraEngine {
    private var engine: ICameraEngine = CameraEngine()
    fun setupEngine(engine: ICameraEngine) {
        this.engine = engine
    }

    override fun openCamera(cameraFacing: CameraFacing) {
        engine?.openCamera(cameraFacing)
    }

    override fun closeCamera() {
        engine?.closeCamera()
    }

    override fun switchCamera() {
        engine?.switchCamera()
    }

    override fun takePicture(resultCallback: ResultCallback?) {
        engine?.takePicture(resultCallback)
    }

    override fun startRecordVideo(resultCallback: ResultCallback?) {
        engine?.startRecordVideo(resultCallback)
    }

    override fun stopRecordVideo() {
        engine?.stopRecordVideo()
    }

    override fun release() {

    }

}