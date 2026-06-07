package app.allever.android.lib.imageloader.core.transformation

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * 高斯模糊变换
 *
 * @param radius 模糊半径 (1 <= radius <= 25)，值越大越模糊
 */
class BlurTransformation(val radius: Int) : Transformation {

    init {
        require(radius in 1..25) { "模糊半径必须在 1~25 之间，当前值: $radius" }
    }

    /**
     * 使用 RenderScript 执行高斯模糊
     * @param context Android Context（用于创建 RenderScript）
     * @param source 原始 Bitmap
     * @return 模糊后的新 Bitmap
     */
    fun transform(context: Context, source: Bitmap): Bitmap {
        return try {
            blurWithRenderScript(context, source)
        } catch (e: Exception) {
            // RenderScript 不可用时降级为简单模糊
            fallbackBlur(source)
        }
    }

    override fun transform(source: Bitmap): Bitmap {
        // 无 Context 时使用降级方案
        return fallbackBlur(source)
    }

    private fun blurWithRenderScript(context: Context, source: Bitmap): Bitmap {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, source)
        val output = Allocation.createTyped(rs, input.type)

        ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
            setInput(input)
            setRadius(radius.toFloat().coerceIn(1f, 25f))
            forEach(output)
        }

        val blurred = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        output.copyTo(blurred)

        input.destroy()
        output.destroy()
        rs.destroy()

        if (source != blurred && !source.isRecycled) {
            source.recycle()
        }
        return blurred
    }

    /** 降级模糊：使用 Stack Blur 算法（纯 Java 实现） */
    private fun fallbackBlur(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        stackBlur(pixels, width, height, radius)

        output.setPixels(pixels, 0, width, 0, 0, width, height)

        if (source != output && !source.isRecycled) {
            source.recycle()
        }
        return output
    }

    /** Stack Blur 算法实现 */
    private fun stackBlur(pixels: IntArray, w: Int, h: Int, r: Int) {
        if (r < 1) return
        val wm = w - 1
        val hm = h - 1
        val div = r + r + 1
        val rSum = IntArray(w * h)
        val gSum = IntArray(w * h)
        val bSum = IntArray(w * h)
        val aSum = IntArray(w * h)
        val vmin = IntArray(maxOf(w, h))
        val vmax = IntArray(maxOf(w, h))
        val divSum = IntArray((div shl 3) + 3)
        for (i in divSum.indices) {
            divSum[i] = i / div
        }

        // 函数级变量，两个循环共享
        var sum = 0
        var rsum = 0
        var gsum = 0
        var bsum = 0
        var asum = 0

        // 水平方向模糊
        var y = 0
        while (y < h) {
            sum = 0; rsum = 0; gsum = 0; bsum = 0; asum = 0
            for (i in -r..r) {
                val p = pixels[clamp(y, 0, hm) * w + clamp(i, 0, wm)]
                rsum += (p shr 16) and 0xff
                gsum += (p shr 8) and 0xff
                bsum += p and 0xff
                asum += p ushr 24
            }
            var x = 0
            while (x < w) {
                aSum[y * w + x] = asum / div
                rSum[y * w + x] = rsum / div
                gSum[y * w + x] = gsum / div
                bSum[y * w + x] = bsum / div

                val yi = clamp(x - r, 0, wm).also { vmax[x] = clamp(x + r + 1, 0, wm) }
                val yw = yi + y * w
                sum -= pixels[yw].ushr(24)
                rsum -= (pixels[yw] shr 16) and 0xff
                gsum -= (pixels[yw] shr 8) and 0xff
                bsum -= pixels[yw] and 0xff
                val px = clamp(x + r + 1, 0, wm)
                val py = px + y * w
                sum += pixels[py].ushr(24)
                rsum += (pixels[py] shr 16) and 0xff
                gsum += (pixels[py] shr 8) and 0xff
                bsum += pixels[py] and 0xff
                asum += sum
                x++
            }
            y++
        }

        // 垂直方向模糊
        var x2 = 0
        while (x2 < w) {
            sum = 0; rsum = 0; gsum = 0; bsum = 0; asum = 0
            for (i in -r..r) {
                val p = pixels[clamp(i, 0, hm) * w + clamp(x2, 0, wm)]
                rsum += (p shr 16) and 0xff
                gsum += (p shr 8) and 0xff
                bsum += p and 0xff
                asum += p ushr 24
            }
            y = 0
            while (y < h) {
                pixels[y * w + x2] = ((aSum[y * w + x2] shl 24) or
                    (rSum[y * w + x2] shl 16) or
                    (gSum[y * w + x2] shl 8) or
                    bSum[y * w + x2])
                val xi = clamp(y - r, 0, hm).also { vmax[x2] = clamp(y + r + 1, 0, hm) }
                val xw = x2 + xi * w
                sum -= pixels[xw].ushr(24)
                rsum -= (pixels[xw] shr 16) and 0xff
                gsum -= (pixels[xw] shr 8) and 0xff
                bsum -= pixels[xw] and 0xff
                val py = clamp(y + r + 1, 0, hm)
                val pw = x2 + py * w
                sum += pixels[pw].ushr(24)
                rsum += (pixels[pw] shr 16) and 0xff
                gsum += (pixels[pw] shr 8) and 0xff
                bsum += pixels[pw] and 0xff
                asum += sum
                y++
            }
            x2++
        }
    }

    private fun clamp(value: Int, min: Int, max: Int): Int =
        value.coerceIn(min, max)

    override fun key(): String = "BlurTransformation(r=$radius)"
}
