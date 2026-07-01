package com.allever.video.editor.ui.widget.gesture

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.alexvasilkov.gestures.GestureController
import com.alexvasilkov.gestures.Settings
import com.alexvasilkov.gestures.State
import com.alexvasilkov.gestures.views.interfaces.GestureView
import com.android.absbase.utils.DebugUtil
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.Ratio
import com.allever.video.editor.function.editor.OnEffectEditListener
import com.allever.video.editor.function.editor.OnEffectSelectListener
import com.allever.video.editor.function.editor.action.*
import com.allever.video.editor.function.editor.bean.*
import com.allever.video.editor.ui.widget.EffectTextView
import com.allever.video.editor.ui.widget.video.TextureVideoView
import com.allever.video.editor.utils.AssetsUtil
import com.allever.video.editor.utils.MediaTypeUtil
import com.allever.video.editor.utils.ViewUtil
import com.allever.video.editor.utils.rectCenterExpansion
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set
import androidx.core.content.res.ResourcesCompat
import com.allever.video.editor.function.editor.action.*
import com.allever.video.editor.function.editor.bean.EffectBean


/**
 * 手势缩放view
 *
 * @author dell
 */
class CustomGestureFrameLayout
@JvmOverloads
constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(mContext, attrs, defStyleAttr), GestureView, TextureVideoView.MediaPlayerCallback,
    IContentView,
    IApplyAction {
    private val TAG: String = CustomGestureFrameLayout::class.java.name
    private lateinit var mContainerLayout: FrameLayout
    private lateinit var mGestureController: GestureController
    private lateinit var mPaint: Paint

    // 用来测试
    private val mBrowseSelectBean = false
    private var mTmpImageView: ImageView? = null
    private var mNeedInitView = true

    var mCurrentClickView: View? = null
    private var mPrevClickView: View? = null
    private val mDefaultBorderPadding = DeviceUtils.dip2px(1f).toFloat()
    private val mStickerBorderPadding = DeviceUtils.dip2px(20f).toFloat()

    private var mFrameBackgroundColor =
        ResourcesUtils.getColor(R.color.video_edit_frame_default_background_color)

    /**
     * 媒体资源Bean的uuid 和 view 映射表
     */
    private val mBeanViewMap = HashMap<Int, View>()
    private val mDefaultViewActions = HashMap<Int, ViewAction>()

    /**
     * 媒体资源Bean
     */
    private var mPrimaryListBean: EffectListBean? = null
    private var mSecondaryListBean: EffectListBean? = null

    private var mPrimaryEffectIds = arrayListOf<Int>()
    private var mSecondaryEffectIds = arrayListOf<Int>()

    private var mBtnDragDrawable: Drawable? = null
    private var mBtnDeleteDrawable: Drawable? = null

    /**
     * 旋转拖动按钮坐标
     */
    private var mBtnDragRect: Rect = Rect()

    /**
     * 删除按钮坐标
     */
    private var mBtnDeleteRect: Rect = Rect()

    /**
     * 用于缓存绘制时的RectF
     */
    private var mCacheRect: RectF = RectF()

    /**
     * 真实界面绘制时的RectF
     */
    private var mCurrentClickViewRect: RectF = RectF()

    /**
     * 裁剪子view
     */
    private val mBoundRectOfView = Rect(0, 0, 100, 100)

    /**
     * 当前是否旋转
     */
    private var rotateState: Boolean = false

    /**
     * 当前是否编辑
     */
    private var isEdit: Boolean = false

    /**
     * 前一次点击的 X 坐标
     */
    private var preEventX = -1f

    /**
     * 前一次点击的 Y 坐标
     */
    private var preEventY = -1f

    /**
     * 按钮的大小
     */
    private var mDragDrawableWidth = DeviceUtils.dip2px(20f)
    private var mDragDrawableHeight = mDragDrawableWidth

    private var mDeleteDrawableWidth = mDragDrawableWidth
    private var mDeleteDrawableHeight = mDragDrawableHeight

    /**
     * 底脚缩放按钮边框大小
     */
    private val mDrawableBounds = DeviceUtils.dip2px(10f)

    /**
     * 画笔宽度
     */
    private val mStrokeWidth = DeviceUtils.dip2px(1f).toFloat()

    /**
     * 边框实线部分宽度
     */
    private val mSolidLineWidth = DeviceUtils.dip2px(6f).toFloat()

    /**
     * 边框虚线部分宽度
     */
    private val mDottedWidth = DeviceUtils.dip2px(2f).toFloat()


    /**
     * 时间轴偏移量
     */
    private var mOffsetTime: Int = 0

    private var mDefaultRatio = Ratio.newRatioOriginal()
    private var mRatio = mDefaultRatio
    private var mPrevRatio: Ratio? = null
    private var mNeedClipChildView = true
        set(value) {
            field = value
            clipChildren = value
        }

    private var mDownEventViewAction: ViewAction? = null

    /**
     * 主特效视频数量
     */
    private var mPrimaryVideoCount = 0
    private var mVideoPreparedCount = 0

    var onAddActionListener: ActionController.OnAddActionListener? = null
    var onVideoPreparedListener: OnVideoPreparedListener? = null
    var onEffectSelectListener: OnEffectSelectListener? = null
    var onEffectEditListener: OnEffectEditListener? = null

    /**
     * 禁止编辑, 只包括拖动和选择
     */
    var prohibitEditing = false

    init {
        setWillNotDraw(false)
        init()
        initDefaultAction()
        initGestureController()

    }

    private fun initDefaultAction() {
    }

    private fun init() {
        clipChildren = false
        val layout = FrameLayout(context)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layout.setBackgroundColor(mFrameBackgroundColor)
        lp.gravity = Gravity.CENTER
        mContainerLayout = layout
        addView(layout, lp)
        layout.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mContainerLayout.viewTreeObserver.removeOnPreDrawListener(this)

                updateFrame(true)
                requestLayout()
                return false
            }
        })


        if (mBrowseSelectBean) {
            mTmpImageView = ImageView(context).also {
                it.setBackgroundColor(Color.RED)
                it.contentDescription = "testimageview"
                it.scaleType = ImageView.ScaleType.FIT_XY
                val lp2 = FrameLayout.LayoutParams(100, 100)
                addView(it, lp2)
                it
            }
        }


        val dragDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_edit_sticker_size)
        mBtnDragDrawable = dragDrawable
        mDragDrawableWidth = dragDrawable?.intrinsicWidth ?: 0
        mDragDrawableHeight = dragDrawable?.intrinsicHeight ?: 0
        mBtnDragRect = Rect(0, 0, mDragDrawableWidth, mDragDrawableHeight)


        val deleteDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_edit_sticker_del)
        mBtnDeleteDrawable = deleteDrawable
        mDeleteDrawableWidth = deleteDrawable?.intrinsicWidth ?: 0
        mDeleteDrawableHeight = deleteDrawable?.intrinsicHeight ?: 0
        mBtnDeleteRect = Rect(0, 0, mDeleteDrawableWidth, mDeleteDrawableHeight)

        mPaint = Paint().let {
            it.isAntiAlias = true
            it.color = ResourcesCompat.getColor(resources, R.color.video_edit_frame_color, null)
            it.strokeWidth = mStrokeWidth
            it.style = Paint.Style.STROKE
            //floatArrayOf(30f, 10f) 实线宽度， 虚线宽度....  改为实线，虚线效率太低
//            val pathEffect = DashPathEffect(floatArrayOf(mSolidLineWidth, mDottedWidth), 0f)
//            it.pathEffect = pathEffect
            it
        }
        mCacheRect = RectF()
        mCurrentClickViewRect = RectF()
    }

    private fun initGestureController() {
        val gestureController = GestureController(mContainerLayout)
        mGestureController = gestureController
        // Gesture controller settings
        gestureController.settings
            .setRotationEnabled(true)
            .setDoubleTapEnabled(true)
            .setFitMethod(Settings.Fit.INSIDE)
            .setBoundsType(Settings.Bounds.INSIDE)
            .setMinZoom(0.5f)
            .setMaxZoom(0f)
            .setImage(1, 1)
            .disableBounds()

        gestureController.setOnGesturesListener(object :
            GestureController.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent) {
                super.onDown(event)
                initDown()
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                singleClick()
                return super.onSingleTapUp(event)
            }

            override fun onDoubleTap(event: MotionEvent): Boolean {
                doubleClick()
                return super.onDoubleTap(event)
            }

            override fun onUpOrCancel(event: MotionEvent) {
                super.onUpOrCancel(event)
                upOrCancel()
            }
        })
        gestureController.addOnStateChangeListener(object :
            GestureController.OnStateChangeListener {
            override fun onStateChanged(state: State) {
                invalidate()
            }

            override fun onStateReset(oldState: State, newState: State) {
                invalidate()
            }
        })
    }

    fun singleClick() {
        if (mCurrentClickView is EffectTextView) {
            if (mPrevClickView == mCurrentClickView) {
                mCurrentClickView?.isEnabled = true
            } else {
                (mCurrentClickView as EffectTextView).setDefaultTextTip()
            }
        }
    }

    fun doubleClick() {
        if (mCurrentClickView is EffectTextView) {
            mCurrentClickView?.isEnabled = true
        } else {
            if (mPrevClickView is EffectTextView) {
                mPrevClickView?.isEnabled = false
            }
        }
    }

    fun upOrCancel() {
        val currentClickView = mCurrentClickView
        if (currentClickView != null) {
            checkChangePosition(mDownEventViewAction, currentClickView)
            val effectBean = currentClickView.tag as? EffectBean
            if (effectBean != null) {
                val id = effectBean.id
                if (id in mPrimaryEffectIds) {
                    onEffectSelectListener?.onSelectPrimaryEffect(effectBean)
                } else if (id in mSecondaryEffectIds) {
                    onEffectSelectListener?.onSelectSecondaryEffect(effectBean)
                }
            }
        } else {
            onEffectSelectListener?.onNoSelect()
        }


        if (mBrowseSelectBean) {
            getCurrentEditorBean()?.also {
                val imageView = mTmpImageView ?: return@also
                val effectBitmap = it.getEffectBitmap(this@CustomGestureFrameLayout)
                if (effectBitmap != null) {
                    val lp = imageView.layoutParams
                    lp.width = mBoundRectOfView.width() / 3
                    lp.height = mBoundRectOfView.height() / 3

                    imageView.setImageBitmap(effectBitmap)
                }
            }
        }
    }

    /**
     * 点击初始化数据
     */
    private fun initDown() {
        mPrevClickView = mCurrentClickView
        mCurrentClickView = mGestureController.currentClickView
        invalidate()
        if (mPrevClickView is EffectTextView && mPrevClickView != mCurrentClickView) {
            mPrevClickView?.isEnabled = false
        }
        mDownEventViewAction = null
        mCurrentClickView?.let {
            if (it is EffectTextView) {
                if (it.isEnabled) {
                    it.isEnabled = true
                }
            }
            mDownEventViewAction = makeViewAction(it)
        }

    }

    private fun updateRatio(w: Int, h: Int) {
        val scaleFactor = mRatio.scaleFactor
        var left = 0
        var top = 0
        var width = w
        var height = h
//        if (mRatio !== Ratio.RATIO_CUSTOM) {
        if (w.toFloat() / h > scaleFactor) {
            width = (height * scaleFactor).toInt()
        } else {
            height = (width / scaleFactor).toInt()
        }
        left = (w - width) / 2
        top = (h - height) / 2
//        }
        mBoundRectOfView.set(left, top, left + width, top + height)

        mContainerLayout.top = top
        mContainerLayout.bottom = top + height
        mContainerLayout.left = left
        mContainerLayout.right = left + width

        val lp = mContainerLayout.layoutParams as FrameLayout.LayoutParams
        lp.width = width
        lp.height = height
        lp.gravity = Gravity.CENTER
    }

    fun setRatio(ratio: Ratio = mDefaultRatio, action: Boolean = false) {
        if (mRatio == ratio) return
        mNeedClipChildView = true
        val prevRatio = mRatio
        mPrevRatio = mRatio
        val ratio = if (ratio.id == Ratio.RatioId.RATIO_ORIGINAL) {
            mDefaultRatio
        } else ratio
        mRatio = ratio
        val prevWidth = mBoundRectOfView.width()
        val prevHeight = mBoundRectOfView.height()
        updateRatio(width, height)
        val width = mBoundRectOfView.width()
        val height = mBoundRectOfView.height()
        updateFrame(false, false, width.toFloat() / prevWidth, height.toFloat() / prevHeight)
        requestLayout()

        if (!action) {
            val singleEffectAction =
                SingleEffectAction()
            singleEffectAction.type = ActionType.TYPE_FORMAT
            singleEffectAction.prevObj = prevRatio
            singleEffectAction.obj = ratio
            singleEffectAction.currentObj = ratio
            onAddActionListener?.addAction(singleEffectAction)
        }
    }

    fun getRatio(): Ratio {
        return mRatio
    }

    fun setFrameBackgroundColor(color: Int, action: Boolean = false) {
        if (mFrameBackgroundColor == color) return
        val prevColor = mFrameBackgroundColor
        mFrameBackgroundColor = color

        if (!action) {
            val singleEffectAction =
                SingleEffectAction()
            singleEffectAction.type = ActionType.TYPE_LAYOUT_BACKGROUND
            singleEffectAction.prevObj = prevColor
            singleEffectAction.obj = color
            singleEffectAction.currentObj = color

            onAddActionListener?.addAction(singleEffectAction)
        }
    }

    /**
     * 获取某个View的正确的位置
     *
     * @param view
     * @return
     */
    fun getViewRect(view: View): RectF {
        val vLeft: Int
        val vTop: Int
        val location = IntArray(2)
        view.getLocationInWindow(location)
        vLeft = location[0]
        vTop = location[1]
        val mViewRect = RectF(
            vLeft.toFloat(),
            vTop.toFloat(),
            (vLeft + view.width).toFloat(),
            (vTop + view.height).toFloat()
        )
        return mViewRect
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (prohibitEditing) {
            return super.onTouchEvent(event)
        }
        val eventX = event.x
        val eventY = event.y
        val isConsume: Boolean
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isEdit) {
                    if (isContains(mBtnDragRect, mDrawableBounds, eventX.toInt(), eventY.toInt())) {
                        rotateState = true
                        preEventX = -1f
                        preEventY = -1f
                    } else if (isContains(
                            mBtnDeleteRect,
                            mDrawableBounds,
                            eventX.toInt(),
                            eventY.toInt()
                        )
                    ) {
                        handleDelete()
                    }
                }
                mNeedClipChildView = false
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rotateState = false
                mNeedClipChildView = true
                invalidate()
            }
            else -> {
            }
        }
        if (rotateState) {
            handleDrag(eventX, eventY, event.action == MotionEvent.ACTION_DOWN)
            isConsume = true
        } else {
            isConsume = false
        }
        return if (isConsume) {
            true
        } else mGestureController.onTouch(this, event)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed || mNeedInitView) {
            val lp = mContainerLayout.layoutParams as FrameLayout.LayoutParams
            val oldWidth = lp.width
            val oldHeight = lp.height
            updateRatio(right - left, bottom - top)
            val newWidth = lp.width
            val newHeight = lp.height

            if (oldWidth != -1 && !mNeedInitView) {
                updateFrame(
                    false,
                    true,
                    newWidth.toFloat() / oldWidth,
                    newHeight.toFloat() / oldHeight
                )
            } else {
                mNeedInitView = false
                updateFrame(true, true)
            }



            requestLayout()
        }
    }

    /**
     * 处理删除
     */
    private fun handleDelete() {
        val effectBean = mCurrentClickView?.tag as? EffectBean
            ?: return
        effectBean.state = EffectBean.STATE_DELETE
        onEffectEditListener?.deleteEffect(effectBean)
        mCurrentClickView?.visibility = View.GONE
        invalidate()
    }

    /**
     * 处理拖拽事件
     */
    private fun handleDrag(eventX: Float, eventY: Float, start: Boolean) {
        val currentClickView = mCurrentClickView
        if (currentClickView != null && preEventX != -1f && preEventY != -1f) {
            //计算距离
            val pivotX = currentClickView.pivotX + currentClickView.translationX
            val pivotY = currentClickView.pivotY + currentClickView.translationY
            val state = mGestureController.state
            val D = Math.hypot((pivotX - eventX).toDouble(), (pivotY - eventY).toDouble())
            val prevD = Math.hypot((pivotX - preEventX).toDouble(), (pivotY - preEventY).toDouble())
            val factor = (D / prevD).toFloat()
            if (start) {
                state.set(currentClickView.matrix)
            }
            state.zoomBy(currentClickView, factor, pivotX, pivotY)

            val radiansOffset = Math.atan2(
                (eventY - pivotY).toDouble(),
                (eventX - pivotX).toDouble()
            ) - Math.atan2(
                (preEventY - pivotY).toDouble(),
                (preEventX - pivotX).toDouble()
            )
            val degreesOffset = Math.toDegrees(radiansOffset)
            if (degreesOffset != 0.0) {
                state.rotateBy(currentClickView, degreesOffset.toFloat(), pivotX, pivotY)
            }

            invalidate()
        }
        preEventX = eventX
        preEventY = eventY
    }


    /**
     * 判断是否选中 drawableRect
     * @param drawableRect
     * @param scaleBounds
     * @param eventX
     * @param eventY
     * @return
     */
    private fun isContains(
        drawableRect: Rect?,
        scaleBounds: Int,
        eventX: Int,
        eventY: Int
    ): Boolean {
        val rect = Rect(drawableRect)
        rectCenterExpansion(rect, scaleBounds)
        return rect.contains(eventX, eventY)
    }

    /**
     * 对rect以中心点进行四边扩容
     *
     * @param rect
     * @param bound
     * @return
     */
    private fun rectCenterExpansion(rect: RectF, bound: Float): RectF {
        rect.rectCenterExpansion(bound)
        return rect
    }

    /**
     * 对rect以中心点进行四边扩容
     *
     * @param rect
     * @param bound
     * @return
     */
    private fun rectCenterExpansion(rect: Rect, bound: Int): Rect {
        rect.rectCenterExpansion(bound)
        return rect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

//        mPaint.color = Color.YELLOW
//        canvas.drawRect(mBoundRectOfView, mPaint)

        mPaint.style = Paint.Style.STROKE
        canvas.clipRect(0, 0, width, height)

        isEdit = false
        val currentClickView = mCurrentClickView
        if (currentClickView != null && currentClickView.visibility == View.VISIBLE) {
            isEdit = true
            val x = currentClickView.x + mBoundRectOfView.left
            val y = currentClickView.y + mBoundRectOfView.top
            val degrees = currentClickView.rotation
            val scaleFactorX = currentClickView.scaleX
            val scaleFactorY = currentClickView.scaleY
            val centerX = currentClickView.pivotX + x
            val centerY = currentClickView.pivotY + y

            val left = currentClickView.left.toFloat()
            val top = currentClickView.top.toFloat()
            val right = currentClickView.right.toFloat()
            val bottom = currentClickView.bottom.toFloat()
            mCurrentClickViewRect.set(left, top, right, bottom)
            if (currentClickView.tag is StickerBean) {
                rectCenterExpansion(mCurrentClickViewRect, mStickerBorderPadding)
            } else {
                rectCenterExpansion(mCurrentClickViewRect, mDefaultBorderPadding)
            }

            mCurrentClickViewRect.offset(x, y)


            val count = canvas.save()

            //画边框
            canvas.rotate(degrees, centerX, centerY)
            scaleRect(mCurrentClickViewRect, scaleFactorX, scaleFactorY)
            canvas.drawRect(mCurrentClickViewRect, mPaint)

            // 画删除按钮
            val btnDeleteDrawable = mBtnDeleteDrawable
            if (btnDeleteDrawable != null) {
                mBtnDeleteRect.set(
                    mCurrentClickViewRect.left.toInt() - mDeleteDrawableWidth / 2,
                    mCurrentClickViewRect.top.toInt() - mDeleteDrawableHeight / 2,
                    mCurrentClickViewRect.left.toInt() + mDeleteDrawableWidth / 2,
                    mCurrentClickViewRect.top.toInt() + mDeleteDrawableHeight / 2
                )
                btnDeleteDrawable.bounds = mBtnDeleteRect
                btnDeleteDrawable.draw(canvas)
            }


            // 画旋转按钮
            val btnDragDrawable = mBtnDragDrawable
            if (btnDragDrawable != null) {
                mBtnDragRect.set(
                    mCurrentClickViewRect.right.toInt() - mDragDrawableWidth / 2,
                    mCurrentClickViewRect.bottom.toInt() - mDragDrawableHeight / 2,
                    mCurrentClickViewRect.right.toInt() + mDragDrawableWidth / 2,
                    mCurrentClickViewRect.bottom.toInt() + mDragDrawableHeight / 2
                )
                btnDragDrawable.bounds = mBtnDragRect
                btnDragDrawable.draw(canvas)
            }


            canvas.restoreToCount(count)

            // 调整删除按钮实际位置
            rotateRect(centerX, centerY, mBtnDeleteRect, degrees)
            // 调整旋转按钮实际位置
            rotateRect(centerX, centerY, mBtnDragRect, degrees)

            // 用来测试功能按钮点击区域
            //            int oldColor = mPaint.getColor();
            //            mPaint.setColor(Color.RED);
            //            canvas.drawRect(mBtnDeleteRect, mPaint);
            //            mPaint.setColor(oldColor);
            //            oldColor = mPaint.getColor();
            //            mPaint.setColor(Color.WHITE);
            //            canvas.drawRect(mBtnDragRect, mPaint);
            //            mPaint.setColor(oldColor);
        }
    }

    /**
     * 缩放RectF
     *
     * @param rect
     * @param scaleFactorX
     * @param scaleFactorY
     */
    private fun scaleRect(rect: RectF, scaleFactorX: Float, scaleFactorY: Float) {
        val width = rect.width()
        val height = rect.height()
        val scaleWidth = width * (scaleFactorX - 1)
        val scaleHeight = height * (scaleFactorY - 1)
        val halfScaleWidth = scaleWidth / 2
        val halfScaleHeight = scaleHeight / 2
        rect.left = rect.left - halfScaleWidth
        rect.right = rect.right + halfScaleWidth
        rect.top = rect.top - halfScaleHeight
        rect.bottom = rect.bottom + halfScaleHeight
    }

    /**
     * @param originX
     * @param originY
     * @param rect
     * @param degrees 角度
     */
    private fun rotateRect(originX: Float, originY: Float, rect: Rect, degrees: Float) {
        var degrees = degrees
        val centerX = rect.centerX().toFloat()
        val centerY = rect.centerY().toFloat()
        val D = Math.hypot((originX - centerX).toDouble(), (originY - centerY).toDouble())
        val origenRadians = Math.atan(((centerY - originY) / (centerX - originX)).toDouble())
        // 坐上按钮,坐标需要+180度,如果右上和左下有按钮,可能也需要相应调整
        if (centerX < originX) {
            degrees += 180f
        } else {

        }
        val dscDegrees = Math.toRadians(degrees.toDouble()) + origenRadians
        val ncX = (originX + D * Math.cos(dscDegrees)).toFloat()
        val ncY = (originY + D * Math.sin(dscDegrees)).toFloat()
        rect.offset((ncX - centerX).toInt(), (ncY - centerY).toInt())
    }


    override fun getController(): GestureController? {
        return mGestureController
    }

    /**
     * 设置资源
     *
     * @param mEffectListBean
     */
    fun setEffectListBean(primaryBeans: EffectListBean, secondaryBeans: EffectListBean) {
        this.mPrimaryListBean = primaryBeans
        this.mSecondaryListBean = secondaryBeans

        if (primaryBeans.beans.size > 0) {
            val effectBean = primaryBeans.beans[0]
            val scale = effectBean.position.width() / effectBean.position.height()
            mDefaultRatio.scaleFactor = scale
        }

        var primaryVideoCount = 0
        mPrimaryListBean?.beans?.map {
            if (it is VideoBean) {
                primaryVideoCount++
            }
            mPrimaryEffectIds.add(it.id)
            addEffectBeanView(it)
        }
        mPrimaryVideoCount = primaryVideoCount


        mSecondaryListBean?.beans?.map {
            addSecondaryEffect(it)
        }

        onVideoPrepared()
        mNeedInitView = true
        requestLayout()
    }

    /**
     * 在Activity onResume 生命周期回调，解决VideoView黑屏的问题
     *
     * @param offsetTime 时间轴的偏移量
     */
    fun refreshVideoView(offsetTime: Int) {
        mOffsetTime = offsetTime
        val values = ArrayList(mBeanViewMap.values)
        for (i in values.indices) {
            val view = values[i]
            if (view is TextureVideoView) {
                view.prepare()
            }
        }
    }

    /**
     * 将相关的VideoView 移动到时间轴的位置
     *
     * @param offsetTime 时间轴的偏移量
     */
    fun flashTimeLineVideoView(bean: EffectBean?, offsetTime: Int) {
        if (bean == null) {
            return
        }
        mOffsetTime = offsetTime
        val view = mBeanViewMap[bean.id]
        if (view is TextureVideoView) {
            view.prepare()
        }
    }

    /**
     * 传入特效bean 添加相应的view
     *
     * @param effectBean
     */
    private fun addEffectBeanView(effectBean: EffectBean) {
        when (effectBean.type) {
            MediaTypeUtil.TYPE_JPG, MediaTypeUtil.TYPE_OTHER_IMAGE, MediaTypeUtil.TYPE_PNG -> addImageView(
                effectBean as ImageBean
            )
            MediaTypeUtil.TYPE_VIDEO -> addVideoView(effectBean)
            MediaTypeUtil.TYPE_STICKER -> addStickerView(effectBean as StickerBean)
            MediaTypeUtil.TYPE_TEXT -> addTextView(effectBean)
            else -> {
            }
        }
    }

    private fun addVideoView(effectBean: EffectBean) {
        val videoView = TextureVideoView(mContext)
        val position = effectBean.position
        val width = position.width()
        val height = position.height()
        videoView.layoutParams = ViewGroup.LayoutParams(width.toInt(), height.toInt())
        val uri = effectBean.uri
        if (uri != null) {
            videoView.setVideoURI(uri)
            videoView.setWheterEnd2Start(effectBean.whetherEnd2Start)
            videoView.setMediaPlayerCallback(this, effectBean.id)
            videoView.prepare()
            realAddView(videoView, effectBean)
        }
    }

    private fun addImageView(effectBean: ImageBean) {
        val imageView = ImageView(mContext)
        val position = effectBean.position
        val width = position.width()
        val height = position.height()
        imageView.layoutParams = FrameLayout.LayoutParams(width.toInt(), height.toInt())
        val path = effectBean.path
        if (path != null) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            val bitmap = effectBean.bitmap
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
            realAddView(imageView, effectBean)
        }
    }

    private fun addStickerView(effectBean: StickerBean) {
        val imageView = ImageView(mContext)
        val position = effectBean.position
        val width = position.width()
        val height = position.height()
        imageView.layoutParams = ViewGroup.LayoutParams(width.toInt(), height.toInt())
        var bitmap: Bitmap?
        val path = effectBean.path
        bitmap = if (path != null) {
            AssetsUtil.toBitmap(path)
        } else {
            effectBean.bitmap
        }

        if (bitmap != null) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageBitmap(bitmap)
        }
        realAddView(imageView, effectBean)

    }

    private fun addTextView(effectBean: EffectBean) {
        if (effectBean !is TextBean) {
            if (DebugUtil.isDebuggable()) {
                throw RuntimeException("effectBean is not TextBean")
            }
            return
        }
        val editTextView = EffectTextView(mContext)
        editTextView.background = null
        editTextView.isEnabled = false
        editTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editTextView.onTextConfirmListener = object : EffectTextView.OnTextConfirmListener {
            private var prevText: String = effectBean.text
            override fun onTextChanged(text: String) {
                if (prevText != text) {
                    effectBean.text = text

                    onEffectSelectListener?.onSelectSecondaryEffect(effectBean)
                }
                prevText = text
            }

            override fun onTextConfirm(text: String) {
                effectBean.text = text
                changeTextViewText(effectBean, prevText)
                prevText = text
            }
        }
        realAddView(editTextView, effectBean)
        effectBean.refresh(this)
    }

    /**
     * 获取相应的特效View
     *
     * @param bean
     * @return
     */
    override fun getEffectView(bean: EffectBean?): View? {
        return mBeanViewMap[bean?.id]
    }

    override fun removeEffectView(bean: EffectBean) {
        val effectView = mBeanViewMap[bean.id]
        if (effectView != null) {
            effectView.visibility = View.INVISIBLE
            invalidateSelf()
        }
    }

    /**
     * 获取子view的相对位置
     */
    override fun getEffectViewRect(bean: EffectBean?): RectF {
        var rect: RectF? = null
        val subView = getEffectView(bean)
        if (subView != null) {
            val x = subView.x/* + mBoundRectOfView.left*/
            val y = subView.y/* + mBoundRectOfView.top*/
            val scaleFactorX = subView.scaleX
            val scaleFactorY = subView.scaleY
            val left = subView.left.toFloat()
            val top = subView.top.toFloat()
            val right = subView.right.toFloat()
            val bottom = subView.bottom.toFloat()
            rect = RectF(left, top, right, bottom)
            scaleRect(rect, scaleFactorX, scaleFactorY)
            rect.offset(x, y)
        }
        return rect ?: RectF()
    }

    override fun getVideoRect(): Rect {
        return mBoundRectOfView
    }

    override fun updateState() {
        mPrimaryListBean?.beans?.map {
            val view = mBeanViewMap[it.id]
            if (view == null) {
                if (it is VideoBean) {
                    mPrimaryVideoCount++
                }
                mPrimaryEffectIds.add(it.id)
                addEffectBeanView(it)
            }
        }
        // 更新状态
        mSecondaryEffectIds.map {
            val view = mBeanViewMap[it]
            val effectBean = mSecondaryListBean?.getEffectBean(it)
            if (effectBean == null) {
                view?.visibility = View.GONE
            } else {
                view?.visibility = View.VISIBLE
            }
            effectBean
        }
    }

    override fun invalidateSelf() {
        invalidate()
    }

    /**
     * 获取当前的编辑Bean
     */
    fun getCurrentEditorBean(): EffectBean? {
        return mCurrentClickView?.tag as? EffectBean
    }

    /**
     * 改变状态
     */
    fun changeBeanState(effectBean: EffectBean) {
        effectBean.refresh(this)
    }

    /**
     * 动态添加特效View
     * @param bean
     */
    fun addSecondaryEffect(bean: EffectBean) {
        mSecondaryEffectIds.add(bean.id)
        addEffectBeanView(bean)
    }

    fun selectEffect(effectBean: EffectBean?) {
        val effectView = getEffectView(effectBean)
        if (mCurrentClickView != effectView) {
            mCurrentClickView = effectView
            invalidate()
        }
        if (mCurrentClickView !is EffectTextView && mPrevClickView is EffectTextView) {
            mPrevClickView?.isEnabled = false
        }
        mBeanViewMap.values.map {
            val effectBean = if (it.tag is EffectBean) it.tag as EffectBean else null
            if (it is EffectTextView && it != mCurrentClickView && effectBean?.state != EffectBean.STATE_DELETE) {
                it.visibility = it.prevVisibility
            }
        }
    }

    /**
     * 删除相应的特效View和Bean
     *
     * @param bean
     */
    fun removeView(bean: EffectBean) {
        if (mBeanViewMap.containsKey(bean.id)) {
            val view = mBeanViewMap[bean.id]
            if (view != null) {
                removeView(view)
                mBeanViewMap.remove(bean.id)
            }
        }
    }

    /**
     * 真正添加到ViewGroup
     *
     * @param view
     */
    private fun realAddView(view: View, effectBean: EffectBean) {
        view.tag = effectBean
        mContainerLayout.addView(view)
        mBeanViewMap[effectBean.id] = view

        view.post {
            // 添加的view放到中间
            val width = view.width
            val height = view.height
            val containerLayoutRect = ViewUtil.getViewRectF(mContainerLayout)
            view.translationX = (containerLayoutRect.width() - width) / 2
            view.translationY = (containerLayoutRect.height() - height) / 2
            view.requestLayout()
            mDefaultViewActions[effectBean.id] = makeViewAction(view, false)
        }
    }

    private fun updateFrame(
        init: Boolean,
        updateLayout: Boolean = false,
        scaleX: Float = 1.0f,
        scaleY: Float = 1.0f
    ) {
        val views = ArrayList(mBeanViewMap.values)

//        var maxDiagonal = 0f
//        for (view in views) {
//            val viewSrcWidth = (view.width * view.scaleX)
//            val viewSrcHeight = (view.height * view.scaleY)
//
//            val diagonal = Math.hypot(viewSrcWidth.toDouble(), viewSrcHeight.toDouble()).toFloat()
//            if (diagonal > maxDiagonal) {
//                maxDiagonal = diagonal
//            }
//        }

        for (view in views) {
            val lp = view.layoutParams as? FrameLayout.LayoutParams ?: continue
            val width = lp.width
            val height = lp.height
            val clipWidth = mBoundRectOfView.width()
            val clipHeight = mBoundRectOfView.height()
            if (init) {
                val zoomScale = width.toFloat() / height
                var showWidth = clipWidth.toFloat()
                var showHeight = (showWidth / zoomScale)
                if (showHeight > clipHeight) {
                    showHeight = clipHeight.toFloat()
                    showWidth = showHeight * zoomScale
                }
                val centerX = mBoundRectOfView.centerX()
                val centerY = mBoundRectOfView.centerY()

                val left = (centerX - width / 2).toFloat()
                val top = (centerY - height / 2).toFloat()

                view.translationX = left
                view.translationY = top
                view.scaleX = showWidth / width
                view.scaleY = showHeight / height

                val id = (view.tag as? EffectBean)?.id
                if (id != null) {
                    mDefaultViewActions[id] = makeViewAction(view)
                }

            } else {
                view.translationX += (clipWidth - clipWidth / scaleX) / 2
                view.translationY += (clipHeight - clipHeight / scaleY) / 2

                if (updateLayout) {
                    view.scaleX = view.scaleX * scaleX
                    view.scaleY = view.scaleY * scaleY
                } else {
                    val oldW = clipWidth / scaleX
                    val oldH = clipHeight / scaleY
                    val dstWidth = oldW
                    val dstHeight = dstWidth / clipWidth * clipHeight
                    var scale = clipWidth / dstWidth

                    var viewSrcWidth = (view.width * view.scaleX)
                    var viewSrcHeight = (view.height * view.scaleY)

//                    val effectBean = view.tag as EffectBean
//                    if (effectBean.primary) {
                    val diagonal =
                        Math.hypot(viewSrcWidth.toDouble(), viewSrcHeight.toDouble()).toFloat()
                    if (view.rotation != 0f) {
                        viewSrcWidth = diagonal
                        viewSrcHeight = diagonal
                    }
                    val srcScale = Math.max(viewSrcWidth / oldW, viewSrcHeight / oldH)
                    val dstScale = Math.min(dstWidth / viewSrcWidth, dstHeight / viewSrcHeight)
                    scale *= dstScale * srcScale
//                    }
                    // 这里如果直接设置scaleX和scaleY, 再次旋转会出现scale不对的问题
//                    view.scaleX = view.scaleX * scale
//                    view.scaleY = view.scaleY * scale

                    val pivotX = view.pivotX + view.translationX
                    val pivotY = view.pivotY + view.translationY
                    val state = mGestureController.state
                    val factor = /*view.scaleX * */scale
                    state.set(view.getMatrix())
                    state.zoomBy(view, factor, pivotX, pivotY)
                }

            }
        }
        mCurrentClickView?.matrix?.also {
            mGestureController.state?.set(it)
        }
    }

    override fun onPrepared(mp: MediaPlayer, id: Int) {
        mVideoPreparedCount++
        val view = mBeanViewMap[id]
        if (view is TextureVideoView) {
            view.seekTo(mOffsetTime)
        }

        onVideoPrepared()
    }

    private fun onVideoPrepared() {
        if (mVideoPreparedCount == mPrimaryVideoCount) {
            onVideoPreparedListener?.onVideoPrepared()
        }
    }

    override fun onCompletion(mp: MediaPlayer, id: Int) {
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int, id: Int) {

    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int, id: Int) {

    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int, id: Int): Boolean {
        return false
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int, id: Int): Boolean {
        return false
    }

    private fun makeViewAction(childView: View, wantPrev: Boolean = true): ViewAction {
        val viewAction = ViewAction()
        val effectBean = childView.tag as? EffectBean
        val prevAction = if (wantPrev && effectBean != null) {
            val defaultViewAction = mDefaultViewActions[effectBean.id]
            if (defaultViewAction != null) {
                defaultViewAction.effectBean = effectBean.clone()
                defaultViewAction
            } else null
        } else null
        viewAction.prevAction = prevAction
        viewAction.effectBean = effectBean?.clone()
        viewAction.left = childView.left
        viewAction.top = childView.top
        viewAction.right = childView.right
        viewAction.bottom = childView.bottom
        viewAction.translationX = childView.translationX
        viewAction.translationY = childView.translationY
        viewAction.pivotX = childView.pivotX
        viewAction.pivotY = childView.pivotY
        viewAction.rotation = childView.rotation
        viewAction.scaleX = childView.scaleX
        viewAction.scaleY = childView.scaleY
        return viewAction
    }

    private fun checkChangePosition(downViewAction: ViewAction?, childView: View) {
        val newAction = makeViewAction(childView)
        if (newAction == null || newAction.compareValue(downViewAction)) {
            return
        }
        onAddActionListener?.addAction(newAction)
    }

    private fun changeTextViewText(effectBean: TextBean, prevText: String) {
        val action = SingleEffectAction(effectBean)
        action.type = ActionType.TYPE_TEXT_INPUT
        action.prevObj = prevText
        action.obj = effectBean.text
        onAddActionListener?.addAction(action)
    }

    private fun applyViewAction(action: ViewAction) {
        val effectBean = action.effectBean ?: return
        val childView = getEffectView(effectBean)
        if (childView != null) {
            (childView.tag as? EffectBean)?.set(effectBean)
            var changed = false
            action.translationX?.let {
                childView.translationX = it
                changed = true
            }
            action.translationY?.let {
                childView.translationY = it
                changed = true
            }
//                    action.pivotX?.let {
//                        childView.pivotX = it
//                    }
//                    action.pivotY?.let {
//                        childView.pivotY = it
//                    }
            action.rotation?.let {
                childView.rotation = it
                changed = true
            }
            action.scaleX?.let {
                childView.scaleX = it
                changed = true
            }
            action.scaleY?.let {
                childView.scaleY = it
                changed = true
            }
            if (changed) {
                invalidate()
            }
        }
    }

    override fun applyAction(action: Action?) {
        when (action) {
            is SingleEffectAction -> {
                when (action.type) {
                    ActionType.TYPE_FORMAT -> {
                        val ratio = action.currentObj as? Ratio
                        if (ratio != null) {
                            setRatio(ratio, true)
                        } else {
                            setRatio(action = true)
                        }
                    }
                    ActionType.TYPE_LAYOUT_BACKGROUND -> {
                        val backgroundColor = action.currentObj as? Int
                        if (backgroundColor != null) {
                            setFrameBackgroundColor(backgroundColor, action = true)
                        }
                    }
                    ActionType.TYPE_TEXT_INPUT -> {
                        val textBean = action.effectBean as? TextBean
                        textBean?.text = action.currentObj as String
                    }

                    else -> {}
                }

            }
            is MultiEffectAction -> {
                val effectBean = action.effectBean as? TextBean
                if (effectBean != null) {
                    when (action.type) {
                        ActionType.TYPE_FONT_CHANGE -> {
                            val localPath = action.currentObj?.get(0) as? String
                            val fontName = action.currentObj?.get(1) as? String
                            if (localPath != null) {
                                effectBean.localFontPath = localPath
                            }
                            if (fontName != null) {
                                effectBean.fontName = fontName
                            }
                        }

                        else -> {}
                    }
                }
            }
            is ViewAction -> {
                applyViewAction(action)
            }
            is EffectAction -> {
            }
        }
        val effectBean = (action as? EffectAction)?.effectBean
        if (effectBean != null) {
            val childView = getEffectView(effectBean)
            if (effectBean.isDelete()) {
                childView?.visibility = View.GONE
            }
            effectBean.refresh(this)
        }
    }

    /**
     * 特效准备监听
     */
    interface OnVideoPreparedListener {
        fun onVideoPrepared()
    }
}
