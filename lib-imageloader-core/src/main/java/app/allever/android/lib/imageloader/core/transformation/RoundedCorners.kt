package app.allever.android.lib.imageloader.core.transformation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF

/**
 * 圆角变换
 *
 * @param radius 圆角半径（像素）
 * @param topLeft 左上角是否圆角，默认 true
 * @param topRight 右上角是否圆角，默认 true
 * @param bottomLeft 左下角是否圆角，默认 true
 * @param bottomRight 右下角是否圆角，默认 true
 */
class RoundedCorners(
    val radius: Float,
    val topLeft: Boolean = true,
    val topRight: Boolean = true,
    val bottomLeft: Boolean = true,
    val bottomRight: Boolean = true
) : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }

        // 绘制圆角矩形
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radii = floatArrayOf(
            if (topLeft) radius else 0f,
            if (topLeft) radius else 0f,
            if (topRight) radius else 0f,
            if (topRight) radius else 0f,
            if (bottomRight) radius else 0f,
            if (bottomRight) radius else 0f,
            if (bottomLeft) radius else 0f,
            if (bottomLeft) radius else 0f
        )
        canvas.drawRoundRect(rectF, radii[0], radii[2], paint)

        // 使用 DST_IN 模式裁切原图到圆角区域
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)

        if (source != output && !source.isRecycled) {
            source.recycle()
        }
        return output
    }

    override fun key(): String =
        "RoundedCorners(r=$radius,tL=$topLeft,tR=$topRight,bL=$bottomLeft,bR=$bottomRight)"
}
