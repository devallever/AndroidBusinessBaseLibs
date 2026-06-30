package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.bean.EffectBean


class TimeLineView
@JvmOverloads constructor
(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener, View.OnTouchListener, View.OnLongClickListener, Handler.Callback {

    companion object {
        private val TAG = TimeLineView::class.java.simpleName
        private val LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()
        private val TOUCH_SLOP = ViewConfiguration.getTouchSlop()

        private val LONG_PRESS = 1
    }

    private lateinit var mContentContainer: LinearLayout
    private lateinit var mBtnStart: ImageView
    private lateinit var mBtnEnd: ImageView
    private var mBackground: Drawable = resources.getDrawable(R.drawable.time_line_bg)
    private var mMovingListener: OnFunMovingListener? = null
    private var mOptionListener: OnOptionListener? = null
    private var mScreenWidth = DeviceUtils.getScreenWidthPx(context)

    private var mFunBtnWidth = ResourcesUtils.getDimension(R.dimen.time_line_view_item_fun_width)

    private var mLastRawX = 0f
    private var mOriginRawX = 0f

    private var mContentViewWidth = 0f
    private var mContentViewHeight = ResourcesUtils.getDimension(R.dimen.time_line_view_item_height)
    private var mContentViewPadding = ResourcesUtils.getDimension(R.dimen.time_line_view_item_content_padding)

    private var mArrowColor = ResourcesUtils.getColor(R.color.black)

    private var mHandler = Handler(this)
    private var mIsLongClickContentView = false

    //视频和音乐只能在宽度内移动,文本图片可以无限左右移动，会修改时间轴
    //左箭头移动最大距离，默认为容器的宽度
    var startMaxTranslationX = -1
    //右箭头移动最大距离，默认为容器的宽度, 测量后赋值
    var endMaxTranslationX = -1

    var contentView: View? = null
        set(value) {
            field = value
            mContentContainer.removeAllViews()
            if (value != null) {
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                lp.gravity = Gravity.CENTER_VERTICAL
                mContentContainer.addView(value, lp)
            }
        }
    /**
     * 主副特效
     */
    var effectBean: EffectBean? = null

    var interceptContentContainerEvent = false

    private fun initView() {
        this.setOnClickListener(this)
        this.setOnLongClickListener(this)

        mContentContainer = findViewById(R.id.id_content_container)
        mContentContainer.setOnTouchListener(this)

        mBtnStart = findViewById(R.id.id_iv_start)
        mBtnEnd = findViewById(R.id.id_iv_end)
        mBtnStart.setOnTouchListener(this)
        mBtnEnd.setOnTouchListener(this)

        showFrame(false)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        initView()
    }

    fun setContentViewSize(width: Int? = null, height: Int? = null, padding: Int? = null) {
        if (width != null) {
            mContentViewWidth = width.toFloat()
        }
        if (height != null) {
            mContentViewHeight = height.toFloat()
        }
        if (padding != null) {
            mContentViewPadding = padding.toFloat()
        }
        requestLayout()
    }

    override fun onClick(v: View?) {
        when (v) {
            this -> {
                mOptionListener?.onClick(this)
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            this -> {
                hideAllFrame()
                mOptionListener?.onLongClick(this)
                return true
            }
        }
        return false
    }

    private fun hideAllFrame() {
        val parent = parent as? ViewGroup
        val childCount = parent?.childCount ?: 0
        for (i in 0 until childCount) {
            val child = parent?.getChildAt(i)
            if (child is TimeLineView) {
                child.showFrame(false)
            }
        }
    }
    fun getEndImageWidth(): Int {
        return mBtnEnd.width
    }
    fun getStartImageWidth(): Int {
        return mBtnStart.width
    }

    fun getStartImageLeft(): Int {
        return mBtnStart.left
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = mContentViewWidth + mFunBtnWidth * 2
        val height = mContentViewHeight + mContentViewPadding * 2

        val lp = contentView?.layoutParams
        if (lp != null) {
            lp.width = mContentViewWidth.toInt()
        }

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                width.toInt(), View.MeasureSpec.EXACTLY )
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                height.toInt(), View.MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)


        val mContentContainerWidth = mContentContainer.width
        if (startMaxTranslationX == -1 && mContentContainerWidth != 0) {
            startMaxTranslationX = mContentContainerWidth
        }

        if (endMaxTranslationX == -1 && mContentContainerWidth != 0) {
            endMaxTranslationX = mContentContainerWidth
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when(msg?.what) {
            LONG_PRESS -> {
                mIsLongClickContentView = true
                mMovingListener?.onScrollStart()
            }
        }

        return true
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v) {
            mContentContainer -> {
                if (!interceptContentContainerEvent) {
                    return false
                }
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mOriginRawX = event.rawX
                        mLastRawX = mOriginRawX

                        mIsLongClickContentView = false
                        mHandler.removeMessages(LONG_PRESS)
                        mHandler.sendEmptyMessageAtTime(LONG_PRESS, event.downTime + LONGPRESS_TIMEOUT)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val currentRawX = event.rawX
                        val offsetX = currentRawX - mLastRawX
                        if (offsetX > TOUCH_SLOP) {
                            mHandler.removeMessages(LONG_PRESS)
                        }
                        if (mIsLongClickContentView) {
                            mMovingListener?.onScrollOffset(offsetX)
                        }
                        mLastRawX = currentRawX
                    }
                    //当滑动超出控件范围，只会触发ACTION_CANCEL 不触发ACTION_UP
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> {
                        val currentRawX = event.rawX
                        val isRightTranslation = (mLastRawX - currentRawX) < 0


                        mHandler.removeMessages(LONG_PRESS)
                        mIsLongClickContentView = false
                        mMovingListener?.onScrollEnd()
                    }
                }
            }

            mBtnEnd -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mMovingListener?.onFunDown(this, false, true)
                        mOriginRawX = event.rawX
                        mLastRawX = mOriginRawX
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val currentRawX = event.rawX
                        val offsetX = currentRawX - mLastRawX

                        changeWidthByMove(false, true, offsetX)

                        mLastRawX = currentRawX
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> {
                        val currentRawX = event.rawX
                        val isRightTranslation = (mLastRawX - currentRawX) < 0
                        mMovingListener?.onFunUp(this, false, true)
                    }
                }
            }

            mBtnStart -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mMovingListener?.onFunDown(this, true, false)
                        mOriginRawX = event.rawX
                        mLastRawX = mOriginRawX
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val currentRawX = event.rawX
                        val offsetX = currentRawX - mLastRawX

                        changeWidthByMove(true, false, offsetX)

                        mLastRawX = currentRawX
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> {
                        val currentRawX = event.rawX
                        //判断， 并返回中间位置，动画效果
                        val isLeftTranslation = (mLastRawX - currentRawX) > 0
                        mMovingListener?.onFunUp(this, true, false)
                    }
                }
            }
        }
        //处理滑动冲突，屏蔽父控件拦截onTouch事件
        parent?.requestDisallowInterceptTouchEvent(true)
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    private fun changeWidthByMove(left: Boolean, right: Boolean, distance: Float) {
        if (distance == 0f
                || (left && distance < 0 || right && distance > 0)
                && mMovingListener?.checkFunMove(this, left, right, distance) == false) {
            return
        }
        if (left) {
            //-offsetX：与右边箭头相反
            mContentViewWidth -= distance
            //处理滑动到右边箭头后禁止继续滑动
            if (mContentViewWidth > 0) {
                val isLeftTranslation = (-distance) > 0
                val ivStartLocation = IntArray(2)
                mBtnStart.getLocationOnScreen(ivStartLocation)
                val ivStartX = ivStartLocation[0]
                //如果左箭头移动到左边屏幕边缘，停止移动，向反方向, 即修改you
                if (isLeftTranslation && ivStartX < 0) {
                    //自动滚动，即使手指触摸到屏幕但没移动，也能匀速滚动
                    mMovingListener?.onMoveToScreenEdge(this, true, false)
                } else {
                    requestLayout()
                    mMovingListener?.onFunMoving(this, true, false, distance, false)
//                    if (effectBean?.primary == true) {
//                        contentView?.translationX = width.toFloat() - startMaxTranslationX
//                    }
                }
            } else {
                mContentViewWidth = 0f
            }
        }
        if (right) {
            mContentViewWidth += distance
            //处理滑动到左边箭头后禁止继续滑动
            if (mContentViewWidth > 0) {
                val isRightTranslation = (-distance) < 0
                val ivEndLocation = IntArray(2)
                mBtnEnd.getLocationOnScreen(ivEndLocation)
                val ivEndX = ivEndLocation[0]
                //如果右箭头移动到右边屏幕边缘，停止移动，向反方向, 即修改左
                if (isRightTranslation && ivEndX > (mScreenWidth - 90)) {
                    //自动滚动，即使手指触摸到屏幕但没移动，也能匀速滚动
                    mMovingListener?.onMoveToScreenEdge(this, false, true)
                } else {
                    requestLayout()
                    mMovingListener?.onFunMoving(this, false, true, distance, !isRightTranslation)
                }
            } else {
                mContentViewWidth = 0f
            }
        }
    }

    fun showFrame(show: Boolean = false, arrowColor: Int = mArrowColor) {
        if (show) {
            //显示边框
            mBtnEnd.visibility = View.VISIBLE
            mBtnStart.visibility = View.VISIBLE
            mContentContainer.background = mBackground
            mBtnEnd.setColorFilter(arrowColor)
            mBtnStart.setColorFilter(arrowColor)
        } else {
            //隐藏边框
            mBtnEnd.visibility = View.INVISIBLE
            mBtnStart.visibility = View.INVISIBLE
            mContentContainer.background = null
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val result = super.onTouchEvent(event)
        return result
    }

    fun setMovingListener(movingListener: OnFunMovingListener?) {
        mMovingListener = movingListener
    }

    fun setOptionListener(opListener: OnOptionListener) {
        mOptionListener = opListener
    }

    interface OnFunMovingListener {
        fun onFunDown(timeLineView: TimeLineView, left: Boolean, right: Boolean)
        fun onFunUp(timeLineView: TimeLineView, left: Boolean, right: Boolean)
        //箭头在可移动区域内移动
        fun onFunMoving(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float, isMoveToLeft: Boolean)

        fun checkFunMove(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float): Boolean

        //箭头移动到屏幕边缘
        fun onMoveToScreenEdge(timeLineView: TimeLineView, left: Boolean, right: Boolean)

        fun onScrollStart()
        fun onScrollOffset(offsetX: Float)
        fun onScrollEnd()
    }

    interface OnOptionListener {
        fun onClick(timeLineView: TimeLineView)
        fun onLongClick(timeLineView: TimeLineView)
    }

}