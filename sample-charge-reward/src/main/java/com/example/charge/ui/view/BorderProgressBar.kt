package com.example.charge.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.charge.R

class BorderProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private var maxProgress = 100f
    private var backgroundColor = Color.parseColor("#CCCCCC")
    private var progressColor = Color.parseColor("#FF3B26")
    private var cornerRadius = 135f
    private var borderWidth = 6f
    private var shadowColor = Color.parseColor("#FFFF13")
    private var shadowRadius = 10f

    private val progressPath = Path()

    init {
        // 初始化属性
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BorderProgressBar)
            try {
                // 读取自定义属性值，使用默认值作为备选
                progress = typedArray.getInt(R.styleable.BorderProgressBar_bp_progress, 50).toFloat()
                maxProgress = typedArray.getInt(R.styleable.BorderProgressBar_bp_max, 100).toFloat()
                backgroundColor = typedArray.getColor(R.styleable.BorderProgressBar_bp_backgroundColor, Color.parseColor("#CCCCCC"))
                progressColor = typedArray.getColor(R.styleable.BorderProgressBar_bp_progressColor, Color.parseColor("#FF6B35"))
                cornerRadius = typedArray.getDimension(R.styleable.BorderProgressBar_bp_cornerRadius, 135f)
                borderWidth = typedArray.getDimension(R.styleable.BorderProgressBar_bp_borderWidth, 6f)
                shadowColor = typedArray.getColor(R.styleable.BorderProgressBar_bp_shadowColor, progressColor)
                shadowRadius = typedArray.getDimension(R.styleable.BorderProgressBar_bp_shadowRadius, 10f)
            } finally {
                typedArray.recycle()
            }
        }
    }

    private val backgroundPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.STROKE
        strokeWidth = borderWidth
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        color = progressColor
        style = Paint.Style.STROKE
        strokeWidth = borderWidth
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        // 添加发光效果
        setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        // 设置画笔质量以获得更好的发光效果
        setFilterBitmap(true)
        isDither = true
    }

    private val path = Path()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // 确保有足够的空间显示发光效果
        val desiredWidth = suggestedMinimumWidth + (shadowRadius * 2).toInt()
        val desiredHeight = suggestedMinimumHeight + (shadowRadius * 2).toInt()
        
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        
        // 考虑padding，确保发光效果不被裁剪
        val paddingLeft = shadowRadius
        val paddingTop = shadowRadius
        val paddingRight = shadowRadius
        val paddingBottom = shadowRadius

        // 绘制背景边框（完整圆角矩形）
        val rect = RectF(
            paddingLeft + borderWidth/2,
            paddingTop + borderWidth/2,
            width - paddingRight - borderWidth/2,
            height - paddingBottom - borderWidth/2
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)
        
        // 保存画布状态以支持发光效果
        canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)

        // 绘制进度边框
        if (progress > 0) {
            val progressRatio = progress / maxProgress

            path.reset()
            // 创建圆角矩形路径
            path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)

            // 创建PathMeasure来精确控制路径
            val pathMeasure = PathMeasure(path, true)

            // 使用PathMeasure获取准确的路径长度
            val pathLength = pathMeasure.length
            val progressLength = pathLength * progressRatio

            // 右边框中点作为起始点：
            // 完整路径长度的3/4处 //底部中点
            // 完整路径长度的0处 //左边中店
            // 完整路径长度的0.5处 //右边中店
            val startPosition = pathLength * 0.5f

            // 创建用于绘制进度的新路径
            progressPath.reset()

            // 确保第一个点正确移动
            val pos = FloatArray(2)
            val tan = FloatArray(2)
            pathMeasure.getPosTan(startPosition, pos, tan)
            progressPath.moveTo(pos[0], pos[1])

            // 如果进度长度超过剩余路径长度，分段处理
            if (startPosition + progressLength > pathLength) {
                // 第一段：从起始点到路径末尾
                val firstSegmentLength = pathLength - startPosition
                pathMeasure.getSegment(startPosition, pathLength, progressPath, false)

                // 重置PathMeasure以重新开始
                pathMeasure.setPath(path, true)

                // 第二段：从路径开头继续绘制剩余部分
                val secondSegmentLength = progressLength - firstSegmentLength
                pathMeasure.getSegment(0f, secondSegmentLength, progressPath, false)
            } else {
                // 直接从起始点绘制到指定长度
                pathMeasure.getSegment(startPosition, startPosition + progressLength, progressPath, false)
            }

            // 绘制进度路径
            canvas.drawPath(progressPath, progressPaint)
        }
        
        // 恢复画布状态
        canvas.restore()
    }

    fun setProgress(progress: Int) {
        this.progress = progress.coerceIn(0, maxProgress.toInt()).toFloat()
        invalidate()
    }

    fun setMax(max: Int) {
        this.maxProgress = max.toFloat()
        invalidate()
    }

    // 添加设置颜色和尺寸的方法，用于动态修改属性
    fun setBackgroundColors(color: Int) {
        this.backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    fun setProgressColor(color: Int) {
        this.progressColor = color
        progressPaint.color = color
        invalidate()
    }
    
    // 设置发光颜色
    fun setShadowColor(color: Int) {
        this.shadowColor = color
        progressPaint.setShadowLayer(shadowRadius, 0f, 0f, color)
        invalidate()
    }
    
    // 设置发光半径
    fun setShadowRadius(radius: Float) {
        this.shadowRadius = radius
        progressPaint.setShadowLayer(radius, 0f, 0f, shadowColor)
        invalidate()
        // 重新测量以适应新的发光半径
        requestLayout()
    }

    fun setCornerRadius(radius: Float) {
        this.cornerRadius = radius
        invalidate()
    }

    fun setBorderWidth(width: Float) {
        this.borderWidth = width
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        invalidate()
    }

    fun getProgress(): Int = progress.toInt()
    fun getMax(): Int = maxProgress.toInt()
}