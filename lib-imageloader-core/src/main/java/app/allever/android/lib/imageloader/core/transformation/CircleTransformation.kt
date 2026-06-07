package app.allever.android.lib.imageloader.core.transformation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF

/**
 * 圆形裁切变换
 * 将图片裁切为正圆形（以短边为直径居中裁切）
 */
object CircleTransformation : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }

        // 计算居中裁切区域
        val left = (source.width - size) / 2f
        val top = (source.height - size) / 2f

        // 绘制圆形
        val path = Path().apply {
            addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW)
        }
        canvas.drawPath(path, paint)

        // 裁切原图到圆形区域
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, -left, -top, paint)

        if (source != output && !source.isRecycled) {
            source.recycle()
        }
        return output
    }

    override fun key(): String = "CircleTransformation"
}
