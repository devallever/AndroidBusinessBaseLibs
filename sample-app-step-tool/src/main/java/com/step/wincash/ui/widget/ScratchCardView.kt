package com.step.wincash.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.step.wincash.R

class ScratchCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** 触发自动解锁的阈值（0~1），默认 0.5 即 50% */
    var revealThreshold = 0.5f

    /** 进度回调（0~1） */
    var onScratchProgress: ((Float) -> Unit)? = null

    /** 达到阈值且已自动清除遮罩时回调（只调一次） */
    var onRevealed: (() -> Unit)? = null

    private var maskResId: Int = R.drawable.ic_scratch_mask
    private var overlayBitmap: Bitmap? = null
    private var overlayCanvas: Canvas? = null
    private val drawPath = Path()
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val erasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = dp(24f) // 橡皮大小
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) // 擦除
    }

    var isRevealed: Boolean = false
        private set

    fun setMask(@DrawableRes resId: Int) {
        maskResId = resId
        if (width > 0 && height > 0) {
            buildMaskBitmap()
            invalidate()
        }
    }

    /** 可在外部调橡皮大小（dp） */
    fun setBrushSizeDp(sizeDp: Float) { erasePaint.strokeWidth = dp(sizeDp) }

    fun reset() {
        // 防止复用残留
        animate().cancel()
        alpha = 1f
        isRevealed = false
        buildMaskBitmap()
        invalidate()
    }

    /** 立即或带动画清除整个遮罩 */
    fun revealAll(animation: Boolean = true) {
        if (isRevealed) return
        // 防止复用残留
        animate().cancel()
        alpha = 1f

        isRevealed = true
        if (animation) {
            animate().alpha(0f).setDuration(220).withEndAction {
                clearOverlay()
                alpha = 1f
                invalidate()
            }.start()
        } else {
            clearOverlay()
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) buildMaskBitmap()
    }

    private fun buildMaskBitmap() {
        if (maskResId == 0 || width == 0 || height == 0) return
        overlayBitmap?.recycle()
        overlayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        overlayCanvas = Canvas(overlayBitmap!!)

        val d = AppCompatResources.getDrawable(context, maskResId)!!
        d.setBounds(0, 0, width, height)
        d.draw(overlayCanvas!!)

        if (isRevealed) {
            clearOverlay()
        }
    }

    private fun clearOverlay() {
        overlayCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        overlayBitmap?.let { canvas.drawBitmap(it, 0f, 0f, bitmapPaint) }
    }

    /**
     * ========= 新增：供外部（如 RecyclerView 的 OnItemTouchListener）直接调用 =========
     */
    fun beginScratch(x: Float, y: Float) {
        if (isRevealed) return
        drawPath.reset()
        drawPath.moveTo(x, y)
        overlayCanvas?.drawCircle(x, y, erasePaint.strokeWidth / 2f, erasePaint)
        invalidate()
    }

    fun scratchTo(x: Float, y: Float) {
        if (isRevealed) return
        drawPath.lineTo(x, y)
        overlayCanvas?.drawPath(drawPath, erasePaint)
        invalidate()
    }

    fun endScratch() {
        if (isRevealed) return
        drawPath.reset()
        checkPercent()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 保留原生单 View 内使用方式；当接入统一分发时，事件会在 RecyclerView 层被拦截，不会走到这里
        if (isRevealed) return false
        parent.requestDisallowInterceptTouchEvent(true)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                beginScratch(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                scratchTo(event.x, event.y)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                endScratch()
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun checkPercent() {
        val percent = scratchedPercent(sampleStep = 8) // 步长越小越精确
        onScratchProgress?.invoke(percent)
        if (!isRevealed && percent >= revealThreshold) {
            revealAll(animation = true)
            onRevealed?.invoke()
        }
    }

    /** 估算已刮开的比例（alpha==0 认为已清除） */
    fun scratchedPercent(sampleStep: Int = 8): Float {
        val bmp = overlayBitmap ?: return 0f
        val w = bmp.width
        val h = bmp.height
        var clear = 0
        var total = 0
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val a = (bmp.getPixel(x, y) ushr 24) and 0xff
                if (a == 0) clear++
                total++
                x += sampleStep
            }
            y += sampleStep
        }
        return if (total == 0) 0f else clear.toFloat() / total
    }

    private fun dp(v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)
}
