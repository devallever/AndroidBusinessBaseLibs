package com.plinkopro.wincash.business.withdraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.plinkopro.wincash.R
import kotlin.random.Random

class CaptchaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_CAPTCHA_LENGTH = 4
        private const val DEFAULT_WIDTH = 160
        private const val DEFAULT_HEIGHT = 58
        private const val DEFAULT_FONT_SIZE = 24f
        private const val DEFAULT_CORNER_RADIUS = 8f
        private const val DEFAULT_BORDER_WIDTH = 1f
        private val DEFAULT_BORDER_COLOR = Color.GRAY
        private val GRAY = Color.GRAY
    }

    private var captchaCode: String = ""
    private val textPaint = Paint()
    private val linePaint = Paint()
    private val backgroundPaint = Paint()
    private val borderPaint = Paint()
    private val random = Random.Default
    private val rectF = RectF()

    // 自定义属性
    var captchaLength: Int = DEFAULT_CAPTCHA_LENGTH
        set(value) {
            field = value
            generateNewCaptcha()
        }
    var captchaFontSize: Float = DEFAULT_FONT_SIZE
        set(value) {
            field = value
            textPaint.textSize = value
            invalidate()
        }
    var cornerRadius: Float = DEFAULT_CORNER_RADIUS
        set(value) {
            field = value
            invalidate()
        }
    var borderWidth: Float = DEFAULT_BORDER_WIDTH
        set(value) {
            field = value
            borderPaint.strokeWidth = value
            invalidate()
        }
    var borderColor: Int = DEFAULT_BORDER_COLOR
        set(value) {
            field = value
            borderPaint.color = value
            invalidate()
        }

    init {
        // 从XML属性读取设置
        context.withStyledAttributes(
            attrs,
            R.styleable.CaptchaView,
            defStyleAttr,
            0
        ) {

            captchaLength = getInt(
                R.styleable.CaptchaView_captchaLength,
                DEFAULT_CAPTCHA_LENGTH
            )

            captchaFontSize = getDimension(
                R.styleable.CaptchaView_captchaFontSize,
                DEFAULT_FONT_SIZE
            )

            cornerRadius = getDimension(
                R.styleable.CaptchaView_captchaCornerRadius,
                DEFAULT_CORNER_RADIUS
            )

            borderWidth = getDimension(
                R.styleable.CaptchaView_captchaBorderWidth,
                DEFAULT_BORDER_WIDTH
            )

            borderColor = getColor(
                R.styleable.CaptchaView_captchaBorderColor,
                DEFAULT_BORDER_COLOR
            )

        }

        // Initialize paints
        textPaint.apply {
            textSize = captchaFontSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        linePaint.apply {
            color = GRAY
            strokeWidth = 1f
            isAntiAlias = true
        }

        backgroundPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        borderPaint.apply {
            color = DEFAULT_BORDER_COLOR
            strokeWidth = DEFAULT_BORDER_WIDTH
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        generateNewCaptcha()
    }

    fun generateNewCaptcha() {
        captchaCode = generateCaptchaCode(captchaLength)
        invalidate() // Redraw the view
    }

    fun getCaptchaCode(): String = captchaCode

    private fun generateCaptchaCode(length: Int): String {
        return (1..length).joinToString("") { Random.nextInt(10).toString() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取XML中指定的宽高或使用默认值
        val width = resolveSize(DEFAULT_WIDTH, widthMeasureSpec)
        val height = resolveSize(DEFAULT_HEIGHT, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw rounded background
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)

        // Draw border if border width is greater than 0
        if (borderWidth > 0) {
            // Adjust rect for border to be inside the view
            val halfBorder = borderWidth / 2f
            rectF.set(
                halfBorder,
                halfBorder,
                width.toFloat() - halfBorder,
                height.toFloat() - halfBorder
            )
            canvas.drawRoundRect(rectF, cornerRadius - halfBorder, cornerRadius - halfBorder, borderPaint)
        }

        // Calculate the base vertical center of the canvas for the text
        val baseTextYPos = (height / 2 - (textPaint.descent() + textPaint.ascent()) / 2)
        val maxVerticalOffset = (captchaFontSize * 0.5f).toInt()

        // Draw each character
        val charWidth = width / captchaCode.length.toFloat()
        captchaCode.forEachIndexed { index, char ->
            val xPos = (index + 0.5f) * charWidth
            val yOffset = random.nextInt(-maxVerticalOffset, maxVerticalOffset + 1)
            val currentYPos = baseTextYPos + yOffset

            textPaint.color = Color.rgb(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )

            canvas.drawText(char.toString(), xPos, currentYPos, textPaint)
        }

        // Draw some random lines
        for (i in 0..3) {
            val startX = random.nextInt(width)
            val startY = random.nextInt(height)
            val endX = random.nextInt(width)
            val endY = random.nextInt(height)
            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                endX.toFloat(),
                endY.toFloat(),
                linePaint
            )
        }
    }
}