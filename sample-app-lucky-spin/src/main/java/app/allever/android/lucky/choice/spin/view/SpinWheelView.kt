package app.allever.android.lucky.choice.spin.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withSave
import kotlin.random.Random

class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var onSpinEndListener: ((position: Int, value: String) -> Unit)? = null

    var isSpinning = false
        private set

    private var radius = 0f
    private var isDarkMode = false

    private val trianglePath = Path()
    private val sectorPath = Path()
    private val textBounds = Rect()


    var data: List<String> = listOf("Kotlin", "Java", "Python")
        set(value) {
            if (value.isEmpty()) return
            if (isSpinning) {
                isSpinning = false
                valueAnimator?.end()
            }
            currentAngle = 0f
            field = value
            generateColors()
            invalidate()
        }

    private val colorPalette: MutableList<Int> =
        mutableListOf(0xFFE57373.toInt(), 0xFF81C784.toInt(), 0xFF64B5F6.toInt())

    init {
        paint.style = Paint.Style.FILL
        paint.strokeJoin = Paint.Join.MITER // set miter style
        textPaint.color = Color.WHITE

        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER
        isDarkMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        textPaint.color = if (isDarkMode) Color.WHITE else Color.BLACK

        dividerPaint.color = if (isDarkMode) Color.BLACK else Color.WHITE
        dividerPaint.style = Paint.Style.STROKE
        dividerPaint.strokeWidth = 4f
        dividerPaint.strokeJoin = Paint.Join.ROUND

        borderPaint.color = Color.rgb(240, 240, 240)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 14f
    }

    private var currentAngle = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var valueAnimator: ValueAnimator? = null
    var spinDuration = 3000L

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        val sweepAngle = 360f / data.size // sector angle

        canvas.withSave {
            rotate(currentAngle - 90, width / 2f, height / 2f)

            for (i in data.indices) {
                // draw sector
                paint.color = colorPalette[i] // Set paint color
                sectorPath.apply {
                    reset()
                    moveTo(width / 2f, height / 2f)
                    arcTo(
                        0f,
                        0f,
                        width.toFloat(),
                        height.toFloat(),
                        i * sweepAngle,
                        sweepAngle,
                        false
                    )
                    close()
                }
                canvas.drawPath(sectorPath, paint)

                // Draw text (clip to the sector so it doesn't overflow)
                canvas.withSave {
                    clipPath(sectorPath)
                    val text = data[i]
                    textPaint.getTextBounds(text, 0, text.length, textBounds)
                    val textAngle = i * sweepAngle + sweepAngle / 2
                    withSave {
                        rotate(textAngle, width / 2f, height / 2f)
                        // from  height / 2 - bottom + (bottom - top) / 2
                        val textY = height / 2f - (textBounds.top + textBounds.bottom) / 2f
                        val textX = width / 2f + radius / 2
                        canvas.drawText(text, textX, textY, textPaint)
                    }
                }

            }
        }

        // Draw the divider lines (separately to prevent them from being obscured by the sectors)
        if (data.size > 1) {
            canvas.withSave {
                rotate(currentAngle - 90, width / 2f, height / 2f)
                for (i in data.indices) {
                    val lineAngle = i * sweepAngle
                    withSave {
                        rotate(-lineAngle, width / 2f, height / 2f)
                        val startX = width / 2f
                        val startY = height / 2f
                        val endX = width
                        val endY = height / 2f
                        canvas.drawLine(startX, startY, endX.toFloat(), endY, dividerPaint)
                    }
                }
            }
        }


        val bigCircleRadius = radius / 6.5f
        val smallCircleRadius = radius / 12

        // Draw the center indicator
        // Draw triangle
        trianglePath.apply {
            reset()
            moveTo(width / 2f + bigCircleRadius, height / 2f)
            lineTo(width / 2f - bigCircleRadius, height / 2f)
            lineTo(width / 2f, height / 2f - bigCircleRadius - 20)
            close()
        }
        paint.color = if (isDarkMode) Color.DKGRAY else Color.WHITE
        canvas.drawPath(trianglePath, paint)
        // Draw large circle
        canvas.drawCircle(width / 2f, height / 2f, bigCircleRadius, paint)
        // Draw small circle
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 16f
        canvas.drawCircle(width / 2f, height / 2f, smallCircleRadius, paint)
        // Reset the paint style
        paint.style = Paint.Style.FILL

        // draw border
        canvas.drawCircle(
            width / 2f, height / 2f, radius - 7f, borderPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = measuredWidth.coerceAtMost(measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = w / 2f
    }

    private fun generateColors() {
        colorPalette.clear()
        val hsv = FloatArray(3)
        val hueStep = 360f / data.size

        val initialHue = Random.nextFloat() * hueStep

        for (i in data.indices) {
            hsv[0] = initialHue + i * hueStep // hue
            hsv[1] = (if (isDarkMode) 0.1f else 0.4f) + (Random.nextFloat() / 5) // saturation
            hsv[2] = (if (isDarkMode) 0.2f else 0.6f) + (Random.nextFloat() / 2) // value
            val color = Color.HSVToColor(hsv)
            colorPalette.add(color)
        }
    }

    fun spin() {
        if (isSpinning) return
        isSpinning = true
        val targetAngle = (5..10).random() * 360f + (0..360).random()

        valueAnimator = ValueAnimator.ofFloat(currentAngle, currentAngle + targetAngle).apply {
            duration = spinDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                currentAngle = it.animatedValue as Float
            }
            doOnEnd {
                if (!isSpinning) return@doOnEnd
                isSpinning = false
                currentAngle %= 360f
                val itemAngle = 360f / data.size
                val passCount = (currentAngle / itemAngle).toInt()
                val position = data.lastIndex - passCount
                onSpinEndListener?.invoke(position, data[position])
            }
        }
        valueAnimator?.start()
    }
}