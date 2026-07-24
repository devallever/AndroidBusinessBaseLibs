package app.allever.android.lib.camera.core

import android.content.Context
import android.view.View

object CameraCore : ICameraEngine {
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

    override fun takePicture(callback: PhotoResultCallback?) {
        engine.takePicture(callback)
    }

    override fun startRecordVideo(callback: VideoResultCallback?) {
        engine.startRecordVideo(callback)
    }

    override fun stopRecordVideo() {
        engine.stopRecordVideo()
    }

    override fun release() {
        engine.release()
    }

    override fun setConfig(config: CameraConfig) {
        engine.setConfig(config)
    }

    override fun getState(): CameraState {
        return engine.getState()
    }

    override fun setFlashMode(mode: FlashMode) {
        engine.setFlashMode(mode)
    }

    override fun setZoom(zoom: Float) {
        engine.setZoom(zoom)
    }

    override fun setContext(context: Context) {
        engine.setContext(context)
    }
}
