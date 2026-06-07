package app.allever.android.lib.imageloader.core.transformation

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

/**
 * 灰度变换
 * 将图片转换为灰度图（黑白效果）
 */
object GrayscaleTransformation : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }

        // 饱和度设为 0 即为灰度
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        canvas.drawBitmap(source, 0f, 0f, paint)

        if (source != output && !source.isRecycled) {
            source.recycle()
        }
        return output
    }

    override fun key(): String = "GrayscaleTransformation"
}
