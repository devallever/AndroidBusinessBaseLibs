package app.allever.android.lib.camera.core

import android.content.Context
import android.media.CamcorderProfile
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

abstract class BaseCameraEngine : ICameraEngine {
    protected var previewRef: WeakReference<View>? = null
    protected var mConfig: CameraConfig = CameraConfig.Builder().build()

    @Volatile
    protected var currentState: CameraState = CameraState.IDLE
    @Volatile
    protected var currentFacing: CameraFacing = CameraFacing.FACE_BACK
    @Volatile
    protected var isPreviewing = false
    @Volatile
    protected var isCapturing = false
    @Volatile
    protected var isRecording = false

    @Volatile
    protected var cameraScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** 录像回调和文件 */
    @Volatile
    protected var videoCallback: VideoResultCallback? = null
    @Volatile
    protected var currentVideoFile: File? = null

    /** 拍照回调 */
    @Volatile
    protected var photoCallback: PhotoResultCallback? = null
    @Volatile
    protected var currentPhotoFile: File? = null

    /** 媒体文件管理（组合） */
    protected var mediaSaver: MediaSaver? = null

    /** 无预览场景下存储的 Context */
    protected var appContext: Context? = null

    // ==================== 协程 ====================

    protected fun launchCameraTask(block: suspend () -> Unit) {
        if (!cameraScope.coroutineContext.isActive) {
            cameraScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        }
        cameraScope.launch {
            block()
        }
    }

    protected fun cancelCameraScope() {
        cameraScope.cancel()
    }

    // ==================== 接口默认实现 ====================

    override fun bindPreview(view: View) {
        previewRef = WeakReference(view)
        appContext = view.context
        mediaSaver = MediaSaver(view.context)
    }

    override fun setContext(context: Context) {
        appContext = context
        if (mediaSaver == null) {
            mediaSaver = MediaSaver(context)
        }
    }

    override fun setConfig(config: CameraConfig) {
        this.mConfig = config
        this.currentFacing = config.cameraFacing
    }

    override fun getState(): CameraState {
        return currentState
    }

    override fun switchCamera() {
        val newFacing = if (currentFacing == CameraFacing.FACE_BACK) {
            CameraFacing.FACE_FRONT
        } else {
            CameraFacing.FACE_BACK
        }
        openCamera(newFacing)
    }

    // ==================== 状态管理 ====================

    protected fun updateState(state: CameraState) {
        currentState = state
    }

    protected fun resetState() {
        isPreviewing = false
        isCapturing = false
        isRecording = false
        currentState = CameraState.IDLE
    }

    // ==================== View 辅助 ====================

    protected fun getPreviewView(): View? {
        return previewRef?.get()
    }

    protected fun getContext(): Context? {
        return previewRef?.get()?.context ?: appContext
    }

    // ==================== 文件创建（委托 MediaSaver）====================

    protected fun createPhotoFile(): File {
        return mediaSaver!!.createPhotoFile(mConfig.photoSavePath)
    }

    protected fun createVideoFile(): File {
        return mediaSaver!!.createVideoFile(mConfig.videoSavePath)
    }

    // ==================== 图片处理（委托 ImageProcessor）====================

    protected fun rotatePhotoFile(file: File, degree: Int) {
        ImageProcessor.rotateFile(file, degree)
    }

    protected fun rotateImageBytes(data: ByteArray, degree: Int): ByteArray {
        return ImageProcessor.rotateBytes(data, degree)
    }

    // ==================== 媒体库扫描（委托 MediaSaver）====================

    protected fun scanPhotoToGallery(file: File) {
        mediaSaver?.scanPhotoToGallery(file)
    }

    protected fun scanVideoToGallery(file: File) {
        mediaSaver?.scanVideoToGallery(file)
    }

    // ==================== 视频质量配置 ====================

    protected fun getCamcorderProfile(quality: VideoQuality): CamcorderProfile {
        val qualityLevel = when (quality) {
            VideoQuality.SD_480P -> CamcorderProfile.QUALITY_480P
            VideoQuality.HD_720P -> CamcorderProfile.QUALITY_720P
            VideoQuality.FHD_1080P -> CamcorderProfile.QUALITY_1080P
            VideoQuality.UHD_4K -> CamcorderProfile.QUALITY_2160P
        }
        return if (CamcorderProfile.hasProfile(qualityLevel)) {
            CamcorderProfile.get(qualityLevel)
        } else {
            CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        }
    }
}
