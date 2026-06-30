package com.step.wincash.ui.widget.scratchcards

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

/**
 * 九宫格刮刮乐视图组件
 * 支持9个格子的刮刮乐效果，每个格子可以独立刮开显示内容
 */
class ScratchCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // 九宫格参数配置
    private val GRID_ROWS = 3 // 行数
    private val GRID_COLS = 3 // 列数
    private var cellWidth = 0 // 每个格子的宽度
    private var cellHeight = 0 // 每个格子的高度
    private val cellPadding = 4 // 格子间距

    /**
     * 格子状态数据类
     * @param text 格子显示的文本内容
     * @param maskBitmap 遮罩层位图，用于实现刮擦效果
     * @param maskCanvas 遮罩层画布，用于绘制刮擦路径
     * @param scratchPath 刮擦路径，记录用户的刮擦轨迹
     * @param isRevealed 是否已完全揭示（刮开）
     * @param isRevealListenerCalled 是否已调用过揭示监听器
     */
    private data class CellState(
        val text: String,
        var maskBitmap: Bitmap,
        var maskCanvas: Canvas,
        var scratchPath: Path = Path(),
        var isRevealed: Boolean = false,
        var isRevealListenerCalled: Boolean = false
    )

    private val cells = mutableListOf<CellState>() // 所有格子的状态列表
    private val paint = Paint() // 通用画笔，用于绘制背景、网格线等
    private val scratchPaint = Paint() // 刮擦画笔，设置为透明模式实现刮开效果
    private var revealThreshold = 0.5f // 揭示阈值，超过50%时自动完全显示
    private var onCellRevealListener: ((Int) -> Unit)? = null // 格子揭示监听器（传入格子索引）
    private var onAllRevealedListener: (() -> Unit)? = null // 全部揭示监听器
    private var revealedCount = 0 // 已揭示的格子数量

    init {
        // 初始化刮擦画笔 - 关键设置：使用CLEAR模式实现透明刮开效果
        scratchPaint.isAntiAlias = true // 抗锯齿
        scratchPaint.style = Paint.Style.STROKE // 描边样式
        scratchPaint.strokeWidth = 40f // 画笔宽度，控制刮擦粗细
        scratchPaint.strokeCap = Paint.Cap.ROUND // 圆角笔触
        scratchPaint.strokeJoin = Paint.Join.ROUND // 圆角连接处
        scratchPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) // 核心：透明擦除模式

        // 初始化通用画笔 - 用于绘制遮罩层和网格
        paint.isAntiAlias = true
        paint.color = Color.GRAY // 灰色遮罩
        paint.style = Paint.Style.FILL
        
        // 初始化9个格子，默认为1-9的数字
        for (i in 0 until GRID_ROWS * GRID_COLS) {
            // 创建临时遮罩位图（后续会在onSizeChanged中重新调整大小）
            val maskBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(maskBitmap)
            
            // 绘制灰色遮罩层
            maskCanvas.drawRect(0f, 0f, 100f, 100f, paint)
            
            cells.add(CellState(
                text = "${i + 1}",
                maskBitmap = maskBitmap,
                maskCanvas = maskCanvas
            ))
        }
    }

    /**
     * 重写onMeasure方法，确保视图始终为正方形
     * 以宽度为基准，强制将高度设置为与宽度相等
     * 
     * @param widthMeasureSpec 宽度测量规格，包含模式和大小
     * @param heightMeasureSpec 高度测量规格，包含模式和大小
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取宽度值和测量模式
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        
        // 创建新的高度测量规格，使高度等于宽度
        // 保持相同的测量模式确保布局行为一致
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode)
        
        // 调用父类的onMeasure方法进行实际测量
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
    
    /**
     * 当视图大小改变时重新计算格子尺寸并创建遮罩层
     * 这是布局过程中的关键方法，确保在视图尺寸确定后正确初始化绘制资源
     * 
     * @param w 新的宽度
     * @param h 新的高度
     * @param oldw 旧的宽度
     * @param oldh 旧的高度
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // 计算有效网格区域（减去边距）
        val gridWidth = w - cellPadding * (GRID_COLS + 1) // 总宽度减去所有间距
        val gridHeight = h - cellPadding * (GRID_ROWS + 1) // 总高度减去所有间距
        
        // 计算每个格子的大小，取宽高比例中的较小值确保格子为正方形
        val cellSize = Math.min(gridWidth / GRID_COLS, gridHeight / GRID_ROWS)
        cellWidth = cellSize
        cellHeight = cellSize

        // 重新创建每个格子的遮罩层 - 关键步骤：确保遮罩层与实际格子尺寸匹配
        for (cell in cells) {
            // 回收旧的bitmap避免内存泄漏
            if (!cell.maskBitmap.isRecycled) {
                cell.maskBitmap.recycle()
            }
            // 创建新的遮罩位图，大小与实际格子尺寸一致
            cell.maskBitmap = Bitmap.createBitmap(cellWidth, cellHeight, Bitmap.Config.ARGB_8888)
            cell.maskCanvas = Canvas(cell.maskBitmap)
            
            // 绘制灰色遮罩层
            paint.color = Color.GRAY
            paint.style = Paint.Style.FILL
            cell.maskCanvas.drawRect(0f, 0f, cellWidth.toFloat(), cellHeight.toFloat(), paint)
        }
    }

    /*
     * 重写onDraw方法，绘制九宫格、数字和遮罩层
     * 绘制顺序：网格线 -> 背景 -> 文字 -> 遮罩层（如果未揭示）
     * 
     * @param canvas 画布对象，用于绘制所有内容
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 保存原始paint状态 - 重要：避免影响后续绘制或被其他绘制操作影响
        val originalColor = paint.color
        val originalStyle = paint.style
        val originalStrokeWidth = paint.strokeWidth
        
        // 绘制九宫格背景线
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        
        // 绘制水平线
        for (row in 0..GRID_ROWS) {
            val y = cellPadding + row * (cellHeight + cellPadding)
            canvas.drawLine(cellPadding.toFloat(), y.toFloat(), 
                (cellPadding + GRID_COLS * (cellWidth + cellPadding)).toFloat(), y.toFloat(), paint)
        }
        // 绘制垂直线
        for (col in 0..GRID_COLS) {
            val x = cellPadding + col * (cellWidth + cellPadding)
            canvas.drawLine(x.toFloat(), cellPadding.toFloat(), 
                x.toFloat(), (cellPadding + GRID_ROWS * (cellHeight + cellPadding)).toFloat(), paint)
        }

        // 绘制每个格子 - 核心绘制逻辑
        for (row in 0 until GRID_ROWS) {
            for (col in 0 until GRID_COLS) {
                val index = row * GRID_COLS + col
                val cell = cells[index]
                
                // 计算当前格子的位置坐标
                val left = cellPadding + col * (cellWidth + cellPadding)
                val top = cellPadding + row * (cellHeight + cellPadding)
                
                // 先绘制背景矩形，增强显示效果
                paint.color = Color.WHITE
                paint.style = Paint.Style.FILL
                canvas.drawRect(left.toFloat(), top.toFloat(), 
                    (left + cellWidth).toFloat(), (top + cellHeight).toFloat(), paint)
                
                // 绘制数字 - 使用独立的textPaint以避免影响主paint状态
                val textPaint = Paint().apply {
                    color = currentTextColor
                    textSize = cellHeight * 0.4f // 文字大小为格子高度的40%，确保合适的显示比例
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                
                // 计算文字居中位置 - 使用getTextBounds和ascent/descent确保文字在格子中完全居中
                val textBounds = Rect()
                textPaint.getTextBounds(cell.text, 0, cell.text.length, textBounds)
                val textX = left + cellWidth / 2f
                val textY = top + cellHeight / 2f + (textPaint.descent() + textPaint.ascent()) / -2
                
                canvas.drawText(cell.text, textX, textY, textPaint)
                
                // 绘制遮罩层（如果未揭示）
                if (!cell.isRevealed) {
                    // 使用null画笔绘制遮罩，以确保遮罩效果正确应用
                    // 遮罩层包含用户刮擦的路径信息，通过透明像素显示底层内容
                    canvas.drawBitmap(cell.maskBitmap, left.toFloat(), top.toFloat(), null)
                }
            }
        }
        
        // 恢复原始paint状态 - 重要：确保不影响其他组件的绘制
        paint.color = originalColor
        paint.style = originalStyle
        paint.strokeWidth = originalStrokeWidth
    }

    /** 当前正在刮擦的格子索引，用于处理跨格子边界的情况 */
    private var currentCellIndex = -1
    
    /**
     * 重写onTouchEvent方法，处理用户的刮擦操作
     * 实现了跨格子边界的平滑处理，确保每个格子的刮擦路径独立且正确
     * 
     * @param event 触摸事件对象，包含触摸坐标和动作类型
     * @return 是否消费了触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        // 计算触摸位置对应的格子行列坐标
        val col = ((x - cellPadding) / (cellWidth + cellPadding)).toInt()
        val row = ((y - cellPadding) / (cellHeight + cellPadding)).toInt()
        
        // 检查是否在有效格子范围内
        if (col in 0 until GRID_COLS && row in 0 until GRID_ROWS) {
            val index = row * GRID_COLS + col
            val cell = cells[index]
            
            // 只有未揭示的格子才响应刮擦操作
            if (!cell.isRevealed) {
                // 计算在格子内的相对坐标（相对于格子左上角）
                val relativeX = x - (cellPadding + col * (cellWidth + cellPadding))
                val relativeY = y - (cellPadding + row * (cellHeight + cellPadding))
                
                // 根据触摸动作类型处理
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 触摸开始，初始化路径
                        cell.scratchPath.reset()
                        cell.scratchPath.moveTo(relativeX, relativeY)
                        // 记录当前操作的格子索引
                        currentCellIndex = index
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 处理格子切换的情况 - 关键优化：避免跨格子时出现错误刮痕
                        if (index != currentCellIndex) {
                            // 先完成之前格子的绘制
                            if (currentCellIndex != -1 && currentCellIndex < cells.size) {
                                // 确保之前的路径已经绘制到画布上
                                invalidate()
                            }
                            // 为新格子重置路径并开始新的刮痕
                            cell.scratchPath.reset()
                            cell.scratchPath.moveTo(relativeX, relativeY)
                            // 更新当前格子索引
                            currentCellIndex = index
                        } else {
                            // 在同一个格子内移动，继续添加到路径
                            cell.scratchPath.lineTo(relativeX, relativeY)
                        }
                        
                        // 核心操作：将路径绘制到遮罩层，使用CLEAR模式擦除遮罩
                        cell.maskCanvas.drawPath(cell.scratchPath, scratchPaint)
                        // 触发重绘
                        invalidate()
                        
                        // 检查是否达到揭示阈值
                        checkCellRevealPercentage(cell, index)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // 触摸结束，完成绘制
                        if (index == currentCellIndex) {
                            // 确保最后一段路径也被绘制
                            cell.maskCanvas.drawPath(cell.scratchPath, scratchPaint)
                            invalidate()
                            // 重置路径，准备下一次刮擦
                            cell.scratchPath.reset()
                        }
                        // 重置当前格子索引
                        currentCellIndex = -1
                    }
                }
                return true // 消费触摸事件
            }
        } else {
            // 触摸点超出所有格子范围，在抬起或取消时重置当前格子索引
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                currentCellIndex = -1
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 检查格子的揭示百分比，当达到阈值时自动完全揭示
     * 通过计算透明像素数量来确定揭示程度
     * 
     * @param cell 当前要检查的格子状态
     * @param index 格子索引
     */
    private fun checkCellRevealPercentage(cell: CellState, index: Int) {
        // 如果监听器已经被调用过，则不再检查
        if (cell.isRevealListenerCalled) return

        // 计算总像素数和透明像素数
        val totalPixels = cell.maskBitmap.width * cell.maskBitmap.height
        var transparentPixels = 0

        // 提取位图中的所有像素数据
        val pixels = IntArray(totalPixels)
        cell.maskBitmap.getPixels(pixels, 0, cell.maskBitmap.width, 0, 0, cell.maskBitmap.width, cell.maskBitmap.height)
        
        // 遍历计算透明像素数量
        for (pixel in pixels) {
            if (pixel == Color.TRANSPARENT) {
                transparentPixels++
            }
        }

        // 计算揭示百分比并与阈值比较
        val revealPercentage = transparentPixels.toFloat() / totalPixels
        if (revealPercentage >= revealThreshold) {
            // 达到阈值，完全揭示该格子
            revealCellCompletely(cell, index)
        }
    }

    /**
     * 完全揭示一个格子
     * 设置格子状态、更新揭示计数并触发相应的监听器
     * 
     * @param cell 要揭示的格子状态
     * @param index 格子索引
     */
    private fun revealCellCompletely(cell: CellState, index: Int) {
        // 设置格子为已揭示状态
        cell.isRevealed = true
        // 更新已揭示的格子计数
        revealedCount++
        // 触发重绘，使格子完全显示
        invalidate()
        
        // 调用格子揭示监听器（仅调用一次）
        if (!cell.isRevealListenerCalled) {
            onCellRevealListener?.invoke(index)
            cell.isRevealListenerCalled = true
        }
        
        // 检查是否所有格子都已揭示，如果是则触发全部揭示监听器
        if (revealedCount == GRID_ROWS * GRID_COLS) {
            onAllRevealedListener?.invoke()
        }
    }

    /**
     * 设置指定格子的文本内容
     * 
     * @param index 格子索引
     * @param text 要设置的文本
     */
    fun setCellText(index: Int, text: String) {
        if (index in cells.indices) {
            // 使用copy创建新的CellState实例，保持不可变性
            cells[index] = cells[index].copy(text = text)
            // 触发重绘以显示新文本
            invalidate()
        }
    }

    /**
     * 为所有格子设置随机数字（1-9）
     * 使用shuffled()方法确保数字随机排列
     */
    fun setRandomNumbers() {
        // 生成1-9的随机序列
        val numbers = (1..9).shuffled()
        // 为每个格子设置对应的随机数字
        for (i in cells.indices) {
            cells[i] = cells[i].copy(text = numbers[i].toString())
        }
        // 触发重绘以显示新数字
        invalidate()
    }

    /**
     * 重置所有格子，恢复初始状态
     * 清空揭示记录，重建所有遮罩层
     */
    fun resetAll() {
        // 重置揭示计数
        revealedCount = 0
        
        // 重置每个格子的状态
        for (cell in cells) {
            cell.isRevealed = false
            cell.isRevealListenerCalled = false
            cell.scratchPath.reset()
            
            // 关键步骤：重新创建遮罩位图，确保完全重置刮痕
            if (!cell.maskBitmap.isRecycled) {
                cell.maskBitmap.recycle() // 回收旧bitmap避免内存泄漏
            }
            // 创建新的遮罩位图
            cell.maskBitmap = Bitmap.createBitmap(cellWidth, cellHeight, Bitmap.Config.ARGB_8888)
            cell.maskCanvas = Canvas(cell.maskBitmap)
            
            // 重新绘制灰色遮罩
            paint.color = Color.GRAY
            paint.style = Paint.Style.FILL
            cell.maskCanvas.drawRect(0f, 0f, cellWidth.toFloat(), cellHeight.toFloat(), paint)
        }
        // 触发重绘以显示重置后的状态
        invalidate()
    }

    /**
     * 完全揭示所有格子
     * 立即显示所有格子内容，无需用户刮擦
     */
    fun revealAll() {
        for (i in cells.indices) {
            if (!cells[i].isRevealed) {
                revealCellCompletely(cells[i], i)
            }
        }
    }

    /**
     * 设置格子揭示监听器
     * 当单个格子被揭示时触发
     * 
     * @param listener 监听器函数，接收格子索引参数
     */
    fun setOnCellRevealListener(listener: (Int) -> Unit) {
        this.onCellRevealListener = listener
    }

    /**
     * 设置全部揭示监听器
     * 当所有格子都被揭示时触发
     * 
     * @param listener 监听器函数
     */
    fun setOnAllRevealedListener(listener: () -> Unit) {
        this.onAllRevealedListener = listener
    }

    /**
     * 设置揭示阈值
     * 当刮开面积达到此比例时自动完全揭示
     * 
     * @param threshold 阈值，范围0.0-1.0
     */
    fun setRevealThreshold(threshold: Float) {
        this.revealThreshold = threshold
    }

    /**
     * 检查是否所有格子都已揭示
     * 
     * @return 是否所有格子都已揭示
     */
    fun isAllRevealed(): Boolean {
        return revealedCount == GRID_ROWS * GRID_COLS
    }
}