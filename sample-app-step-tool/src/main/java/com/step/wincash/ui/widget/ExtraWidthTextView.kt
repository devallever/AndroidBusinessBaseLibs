package com.step.wincash.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.step.wincash.R
import com.step.wincash.utils.dp2px

/**
 * 自定义TextView，可在XML中设置增加的宽度
 * 场景：TextView使用wrap_content时，某些国家字体，边缘会切割，因此增加一点点宽度，以解决该问题
 */
class ExtraWidthTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    // 增加的宽度（默认5dp）
    private val DEFAULT_DP = dp2px(5f).toFloat()
    private var extraWidth: Int = 0

    init {
        // 从XML属性中读取自定义属性
        attrs?.let {attributeSet ->
            val typedArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.ExtraWidthTextView,
                defStyleAttr,
                0
            )
            try {
                extraWidth = typedArray.getDimension(R.styleable.ExtraWidthTextView_extraWidth, DEFAULT_DP).toInt()
            } finally {
                typedArray.recycle()
            }
        }

    }

    /**
     * 将dp值转换为像素值
     */
    private fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    /**
     * 设置额外增加的宽度（dp）
     */
    fun setExtraWidth(extraWidthDp: Float) {
        this.extraWidth = dpToPx(context, extraWidthDp)
        requestLayout() // 重新布局
    }

    /**
     * 获取额外增加的宽度（像素）
     */
    fun getExtraWidth(): Int {
        return extraWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 先让父类进行正常测量
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // 获取测量后的宽度
        val measuredWidth = measuredWidth
        
        // 在测量宽度基础上增加指定的额外宽度
        val newWidth = measuredWidth + extraWidth
        
        // 设置新的测量尺寸
        setMeasuredDimension(newWidth, measuredHeight)
    }
}