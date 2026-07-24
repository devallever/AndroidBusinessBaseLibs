package app.allever.android.lib.camera.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseCameraEngine : ICameraEngine {
    protected var previewRef: WeakReference<View>? = null
    protected var mConfig: CameraConfig = CameraConfig.Builder().build()
    protected var currentState: CameraState = CameraState.IDLE
    protected var currentFacing: CameraFacing = CameraFacing.FACE_BACK
    protected var isPreviewing = false
    protected var isCapturing = false
    protected var isRecording = false

    protected var cameraScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** 录像回调和文件 */
    protected var videoCallback: ResultCallback? = null
    protected var currentVideoFile: File? = null

    /** 拍照回调 */
    protected var photoCallback: ResultCallback? = null
    protected var currentPhotoFile: File? = null

    // ==================== 协程 ====================

    protected fun launchCameraTask(block: suspend () -> Unit) {
        if (!cameraScope.coroutineContext.isActive) {
            cameraScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        }
        cameraScope.launch {
            block()
        }
    }

    protected suspend fun runOnCameraThread(block: () -> Unit) {
        withContext(Dispatchers.IO) {
            block()
        }
    }

    protected fun cancelCameraScope() {
        cameraScope.cancel()
    }

    // ==================== 接口默认实现 ====================

    override fun bindPreview(view: View) {
        previewRef = WeakReference(view)
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

    // ==================== 权限检查 ====================

    protected fun checkCameraPermission(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    protected fun checkAudioPermission(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    // ==================== View 辅助 ====================

    protected fun getPreviewView(): View? {
        return previewRef?.get()
    }

    protected fun isPreviewViewValid(): Boolean {
        val view = previewRef?.get()
        return view != null && view.isAttachedToWindow
    }

    protected fun getContext(): Context? {
        return previewRef?.get()?.context
    }

    // ==================== 文件创建 ====================

    protected fun createPhotoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(mConfig.photoSavePath, "IMG_$timestamp.jpg")
    }

    protected fun createVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(mConfig.videoSavePath, "VID_$timestamp.mp4")
    }

    // ==================== 图片处理 ====================

    protected fun rotatePhotoFile(file: File, degree: Int) {
        if (degree % 360 == 0) return

        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        FileOutputStream(file).use { fos ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        rotated.recycle()
    }

    protected fun rotateImageBytes(data: ByteArray, degree: Int): ByteArray {
        if (degree % 360 == 0) return data

        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()

        val outputStream = java.io.ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        rotated.recycle()

        return outputStream.toByteArray()
    }

    // ==================== 媒体库扫描 ====================

    protected fun scanPhotoToGallery(file: File) {
        val context = getContext() ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    protected fun scanVideoToGallery(file: File) {
        val context = getContext() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CameraCore")
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    @Throws(IOException::class)
    protected fun savePhotoToGallery(data: ByteArray): File {
        val context = getContext() ?: throw IOException("Context is null")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timestamp.jpg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraCore")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(data)
                }
                val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
                cursor?.moveToFirst()?.let {
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                    cursor.close()
                    return File(path)
                }
                cursor?.close()
            }
        }

        val photoFile = File(mConfig.photoSavePath, fileName)
        FileOutputStream(photoFile).use { fos ->
            fos.write(data)
        }
        scanPhotoToGallery(photoFile)
        return photoFile
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
