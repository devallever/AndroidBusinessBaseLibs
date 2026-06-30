package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.android.absbase.utils.DebugUtil
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.bean.EffectBean

class BitmapContentView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BitmapContentView::class.java.simpleName

    }
    private var mBitmapMatrix = Matrix()
    private var mBitmapWidth = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_bitmap_width).toInt()
    private var mBitmapHeight = mBitmapWidth
    private var mPadding =   ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_padding).toInt()

    private var mBitmapPaint: Paint = Paint()
    private var mEffectBean: EffectBean =
        EffectBean()
    private var mBitmapList = mutableListOf<Bitmap?>()

    private var defaultBitmap: Bitmap = BitmapFactory.decodeResource(ResourcesUtils.resources, R.drawable.icon_album_default)

    /**
     * 是否需要通过时间进行裁剪
     */
    var needToCutThroughTime = false

    init {
    }

    fun setCellSize(width: Int, height: Int) {
        mBitmapWidth = width
        mBitmapHeight = height

        requestLayout()
    }

    fun setPadding(padding: Int) {
        mPadding = padding
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width =  if(needToCutThroughTime){
            mEffectBean.getCropTotalWidth(mBitmapWidth, mPadding)
        }else{
            mEffectBean.getOriginalTotalWidth(mBitmapWidth, mPadding)
        }
        val height = mBitmapHeight
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val duration = mEffectBean.dstDuration
        val srcStartTime = mEffectBean.videoTime.srcStartTime
        val srcEndTime = srcStartTime + duration +  EffectBean.SPILT_TIME_BITMAP
        var realPosition = 0
        for (position in 0 until mEffectBean.getBitmapCountForFrame()) {
            if (needToCutThroughTime
                    && position !in srcStartTime / EffectBean.SPILT_TIME_BITMAP until (srcEndTime / EffectBean.SPILT_TIME_BITMAP)) {
                continue
            }
            val left = mBitmapWidth * realPosition.toFloat() + mPadding * realPosition
            val top = 0f + paddingTop

            val bitmap = mEffectBean.getThumbBitmapForFrameByIndex(position) ?: defaultBitmap
//            val bitmap = mBitmapList[position] ?: defaultBitmap
            mBitmapMatrix.setScale(mBitmapWidth.toFloat()/bitmap.width, mBitmapHeight.toFloat()/bitmap.height)
            mBitmapMatrix.postTranslate(left, top)
            canvas?.drawBitmap(bitmap, mBitmapMatrix, mBitmapPaint)
            realPosition++
            if (DebugUtil.isDebuggable()) {
                val oldColor = mBitmapPaint.color
                mBitmapPaint.color = Color.RED
                // 用来测试位置
                canvas?.drawText("${position + 1}", left, height.toFloat() / 2, mBitmapPaint)
                val last = width - left
                if (last < mBitmapWidth) {
                    val scale = last / mBitmapWidth
                    val text = "$scale"
                    val measureText = mBitmapPaint.measureText(text)
                    canvas?.drawText(text, left - measureText, height.toFloat() / 2 + 20, mBitmapPaint)
                }
                mBitmapPaint.color = oldColor
            }
        }

    }

    private val listener = object : EffectBean.EffectListener {

        private fun checkOneBitmap(index: Int, bitmap: Bitmap?) {
            if (bitmap != null) {
                post {
                    if (index in 0 until mBitmapList.size) {
                        mBitmapList[index] = bitmap
                        invalidate()
                    }
                }
            }
        }

        override fun callBack(index: Int, bitmap: Bitmap) {
            checkOneBitmap(index, bitmap)
        }

        override fun callBack(bitmaps: MutableList<Bitmap?>) {
            mBitmapList = mutableListOf(*bitmaps.toTypedArray())
            requestLayout()
        }
    }

    fun setData(bean: EffectBean) {
        mEffectBean = bean
        mEffectBean.addEffectListener(listener)
        val bitmapList = mEffectBean.getThumbBitmapForFrame()
        mBitmapList = mutableListOf(*bitmapList.toTypedArray())
        requestLayout()
    }

    fun clear() {
        mEffectBean.removeEffectListener(listener)
        mBitmapList.clear()
    }

    fun setData(bitmapList: MutableList<Bitmap?>?) {
        mBitmapList = bitmapList ?: return
        for (i in 0 until mBitmapList.size) {
            if (mBitmapList[i] == null) {
                mBitmapList[i] = defaultBitmap
            }
        }
        requestLayout()
    }

}