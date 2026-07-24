package app.allever.android.lib.camera.core

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 媒体文件管理：创建、保存、扫描到相册。
 */
class MediaSaver(private val context: Context) {

    private val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun createPhotoFile(savePath: String): File {
        val fileName = "IMG_${timeFormat.format(Date())}.jpg"
        return File(savePath, fileName)
    }

    fun createVideoFile(savePath: String): File {
        val fileName = "VID_${timeFormat.format(Date())}.mp4"
        return File(savePath, fileName)
    }

    /**
     * 保存照片字节数组到相册（Camera API 使用）。
     * Android Q+ 通过 MediaStore 保存，低版本直接写文件。
     */
    @Throws(IOException::class)
    fun savePhotoBytes(data: ByteArray, savePath: String): File {
        val fileName = "IMG_${timeFormat.format(Date())}.jpg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraCore")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { os -> os.write(data) }
                return queryFileFromUri(uri) ?: File(savePath, fileName).also { f ->
                    FileOutputStream(f).use { fos -> fos.write(data) }
                }
            }
        }

        val photoFile = File(savePath, fileName)
        FileOutputStream(photoFile).use { fos -> fos.write(data) }
        scanPhotoToGallery(photoFile)
        return photoFile
    }

    fun scanPhotoToGallery(file: File) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file)
            context.sendBroadcast(intent)
        }
    }

    fun scanVideoToGallery(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CameraCore")
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file)
            context.sendBroadcast(intent)
        }
    }

    private fun queryFileFromUri(uri: Uri): File? {
        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                return File(path)
            }
        }
        return null
    }
}
