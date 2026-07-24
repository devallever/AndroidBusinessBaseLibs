package app.allever.android.lib.camera.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 图片处理：旋转文件和字节数组。
 */
object ImageProcessor {

    fun rotateFile(file: File, degree: Int) {
        if (degree % 360 == 0) return

        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
        val rotated = rotateBitmap(bitmap, degree)
        bitmap.recycle()
        FileOutputStream(file).use { fos ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        rotated.recycle()
    }

    fun rotateBytes(data: ByteArray, degree: Int): ByteArray {
        if (degree % 360 == 0) return data

        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size) ?: return data
        val rotated = rotateBitmap(bitmap, degree)
        bitmap.recycle()

        val outputStream = ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        rotated.recycle()

        return outputStream.toByteArray()
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
