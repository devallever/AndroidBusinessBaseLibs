package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.absbase.utils.DebugUtil
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.OnEffectEditListener
import com.allever.video.editor.function.editor.OnEffectSelectListener
import com.allever.video.editor.function.editor.OnEffectStateChangeListener
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.function.editor.bean.EffectListBean
import com.allever.video.editor.function.timeline.TimeLineController
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.utils.DragHelper
import com.allever.video.editor.utils.MediaTypeUtil
import com.allever.video.editor.utils.TimeUtils
import kotlin.math.max
import kotlin.math.min


class EffectEditLayout : RelativeLayout,
    IContentView,
        GestureDetector.OnGestureListener,
        TimeLineController.TimeDispatchEventByControllerListener, View.OnClickListener {


    companion object {
        private val TAG = EffectEditLayout::class.java.simpleName
    }


    private lateinit var mRecyclerView: EffectRecyclerView
    private lateinit var mPaint: Paint
    private lateinit var mCurrentEffectFrameView: TimeLineView
    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mAddBtn: ImageView


    private var mCurrentLine = 0f
    private var mCurrentTimelineOffset = 0L
    private val mMartinStart = DeviceUtils.SCREEN_WIDTH_PX / 2
    private var mTimelineEnd = DeviceUtils.SCREEN_WIDTH_PX / 2
    private val mMartinEnd = mMartinStart
    private val mTimeLineHeight = ResourcesUtils.getDimension(R.dimen.effect_edit_timeline_height)
    private val mNormalStateListTop = ResourcesUtils.getDimension(R.dimen.effect_edit_video_list_normal_top)
    private val mSelectedEffectStateListTop = ResourcesUtils.getDimension(R.dimen.effect_edit_video_list_selected_effect_top)
    private val mTimeLineBackgroundColor = Color.BLACK
    private var mCurrentSecondlyEditMode = false
    /**
     * 拖拽修改特效开始时间外部不允许修改该view 位置
     */
    private var dragStartTimeState = false
    private var mCurrentSelectEffectBean: EffectBean? = null
    private val mVisibleDisplayRect = Rect()


    private var mBitmapListList = mutableListOf<MutableList<Bitmap>>()
    private var mPrimaryListBean: EffectListBean = EffectListBean()
    private var mSecondaryListBean: EffectListBean = EffectListBean()
    /**
     * 绘制的特效bean
     */
    private var mDrawEffectListBean:EffectListBean = EffectListBean()

    private var mHashEffectList:MutableMap<Long,EffectListBean> = mutableMapOf()

    /**
     * 当前时间线标线宽度
     */
    private var mTimeLineMarkingWidth = ResourcesUtils.getDimension(R.dimen.effect_edit_timeline_marking_width)
    /**
     * 时间线间隔圆点宽度
     */
    private var mTimeLineIntervalDotWidth = ResourcesUtils.getDimension(R.dimen.effect_edit_timeline_interval_dot_width)

    private var mAddEffectLabelBackground = resources.getDrawable(R.drawable.video_editor_add_effect_label_background)
    private var mAddEffectLabelSelectStroke = DeviceUtils.dip2px(2f)
    private var mAddEffectLabelSelectStrokeColor = ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_label_stroke_color)
    private var mAddEffectLabelDefaultColor = ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_default_color)
    private var mAddEffectLabelZoomPadding = DeviceUtils.dip2px(5f)
    private var mAddEffectLabelMoveSecondaryMaskColor = ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_move_secondary_mask_color)
    /**
     * 移动叠加的特效标签
     */
    private val tagEffectBeans= EffectListBean()
    /**
     * 添加的特效标签的宽度和高度
     */
    private var mAddEffectLabelWidth = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_width)
    private var mAddEffectLabelHeight = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_height)
    /**
     * 添加的特效标签的连接线高度
     */
    private var mAddEffectLabelCableHeight = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_cable_height)
    /**
     * 选中时，特效标签的连接线与下面控件的间隔
     */
    private var mEffectLabelCableSelectedInterval = ResourcesUtils.getDimension(R.dimen.effect_edit_effect_label_cable_selected_interval)
    /**
     * 添加的特效标签的连接线宽度
     */
    private var mAddEffectLabelCableWidth = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_cable_width)
    /**
     * 添加的特效标签的标线的top
     */
    private var mAddEffectLabelMarkingTop = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_marking_top)
    /**
     * 添加的特效标签的标线的高度
     */
    private var mAddEffectLabelMarkingHeight = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_marking_height)
    /**
     * 添加的特效标签的标线小圆点直径
     */
    private var mAddEffectLabelMarkingDotRadius = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_marking_dot_radius)
    /**
     * 添加的特效标签的标线小圆点底部间距
     */
    private var mAddEffectLabelMarkingDotBottomMargin = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_label_marking_dot_bottom_margin)

    private var mTimeLineViewFunWidth = ResourcesUtils.getDimension(R.dimen.time_line_view_item_fun_width)

    private var mTimeLineCurrentTimeWidth = DeviceUtils.dip2px(40f)
    private var mTimeLineCurrentTimeColor = ResourcesUtils.getColor(R.color.effect_edit_timeline_current_time_color)
    private var mTimeLineCurrentTimeTextSize = DeviceUtils.sp2pxF(12f)
    private var mTimeLineIntervalDotColor = ResourcesUtils.getColor(R.color.effect_edit_timeline_interval_dot_color)
    private var mTimeLineIntervalDotTextSize = DeviceUtils.sp2pxF(10f)
    private var mTimeLineTimeLineColor = ResourcesUtils.getColor(R.color.effect_edit_timeline_current_time_color)

    private var mAddEffectTimeLineContentHeight = ResourcesUtils.getDimension(R.dimen.effect_edit_add_effect_view_content_height).toInt()

    private var mCellWidth = ResourcesUtils.getDimension(R.dimen.effect_edit_cell_width).toInt()
    private var mCellHeight = mCellWidth
    private var mCellPadding = 0

    /**
     * timeline
     */
    private var totalTime: Long = 0
    private var step = 2000L/6
    private val bitmapSize = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_bitmap_width)
    private var timeLineWidth = 0f
    private var centerLineStartY = 0f
    private var centerLineStartX = 0f
    //时间线x的单位 = s/t
    private var speed = 0f
    private var placeHolderWidth = 0f
    private var mDefaultTopOffset: Float = ResourcesUtils.getDimension(R.dimen.icon_top_offset)
    private var mIconTopOffsets = HashMap<Int, Float>()

    private var mCurrentMoveSecondaryEffectState = false
    private var mCurrentMoveSecondaryEffectBean: EffectBean? = null

    var onEffectStateChangeListener: OnEffectStateChangeListener? = null
    var onEffectSelectListener: OnEffectSelectListener? = null
    var onEffectEditListener: OnEffectEditListener? = null

    var onItemListener: OnItemListener? = null

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)

        mPaint = Paint()
        mPaint.color = Color.RED
        mPaint.strokeWidth = 6f
        mPaint.isAntiAlias = true

        mGestureDetector = GestureDetector(context, this)
    }

    private var mDragAdapter: DragAdapter? = null
    private var currentState = androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
    override fun onFinishInflate() {
        super.onFinishInflate()


        viewTreeObserver.addOnScrollChangedListener {
            getLocalVisibleRect(mVisibleDisplayRect)
            invalidate()
        }

        mAddBtn = findViewById(R.id.btn_add)
        OnClickListenerHelper.setOnClickListener(
            mAddBtn,
            this,
            true,
            (com.android.absbase.utils.TimeUtils.TimeConstant.ONE_SEC * 2).toInt()
        )
        mCurrentEffectFrameView = findViewById(R.id.edit_effect_frame)
        mCurrentEffectFrameView.interceptContentContainerEvent = true
        mCurrentEffectFrameView.setOptionListener(object :
            TimeLineView.OnOptionListener {
            override fun onClick(timeLineView: TimeLineView) {
                timeLineView.showFrame(true)
            }
            override fun onLongClick(timeLineView: TimeLineView) {
                timeLineView.showFrame(true)
            }
        })
        mCurrentEffectFrameView.setMovingListener(object :
            TimeLineView.OnFunMovingListener {

            override fun onFunDown(timeLineView: TimeLineView, left: Boolean, right: Boolean) {
                dragStartTimeState = true
            }

            override fun onFunUp(timeLineView: TimeLineView, left: Boolean, right: Boolean) {
                mCurrentSelectEffectBean?.also {
                    updateBeanTag(it)
                }
                dragStartTimeState = false
                onEffectSelectListener?.onDragTimeLineViewEndUp()
            }

            override fun checkFunMove(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float): Boolean {

                return checkEffectTimeAndDuration(timeLineView, left, right, offsetX)
            }

            override fun onFunMoving(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float, isMoveToLeft: Boolean) {
                if (left) {
                    changeEffectTimeAndDuration(timeLineView,true, false, offsetX)
                } else if (right) {
                    changeEffectTimeAndDuration(timeLineView,false, true, offsetX)
                }
                onEffectEditListener?.trimSecondEffectInTimeLine()
            }

            override fun onMoveToScreenEdge(timeLineView: TimeLineView, left: Boolean, right: Boolean) {
            }

            override fun onScrollStart() {
                mCurrentMoveSecondaryEffectState = true
                mCurrentMoveSecondaryEffectBean = mCurrentSelectEffectBean
                invalidate()
            }

            override fun onScrollOffset(offsetX: Float) {
                changeSecondaryEffectOffset(offsetX)
            }

            override fun onScrollEnd() {
                mCurrentMoveSecondaryEffectState = false
                mCurrentMoveSecondaryEffectBean?.also {
                    updateBeanTag(it)
                }
                mCurrentMoveSecondaryEffectBean = null
                invalidate()
            }
        })
        mRecyclerView = findViewById(R.id.id_recycler_view_2)

        val dragStateCallback = object : DragHelper.DragStateCallback {
            var currentAllowDrag = true
            override fun getData(): MutableList<out Any> {
                return mPrimaryListBean.beans
            }

            override fun allowDrag(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                return currentAllowDrag
            }

            override fun allowSwipe(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onItemRangeMoved(from: Int, to: Int) {
            }
            private var mCurrentSelectBean: EffectBean? = null
            override fun onDragStart() {
                currentState = androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
                onEffectStateChangeListener?.onTimelinePause()
                mCurrentSelectBean = mDragAdapter?.currentSelectBean
                mDragAdapter?.selectData(null)
                mRecyclerView.setSelectedPosition(-1)
            }
            override fun onDragEnd(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, from: Int, to: Int) {
                onEffectStateChangeListener?.onTimelineStart()
                onItemListener?.onItemRangeMoved()
                mCurrentSelectBean?.let {
                    val position = mPrimaryListBean.findBeanIndex(it)
                    mDragAdapter?.selectData(it)
                    mRecyclerView.setSelectedPosition(position)
                    null
                }
                mCurrentSelectBean = null
                val position = holder.position
                val bean = if (position < mPrimaryListBean.beans.size) {
                    mPrimaryListBean.beans[position]
                } else null
                val offset = bean?.videoTime?.dstStartTime ?: mCurrentTimelineOffset
                val time2distance = time2distance(offset)
//                DLog.e("breeze", "position $position ,to $to ,time2distance $time2distance")
//                mRecyclerView.scrollBy(-Int.MAX_VALUE, mRecyclerView.scrollY)
                mRecyclerView.scrollBy((time2distance - mRecyclerView.getRealScrollX()).toInt(), mRecyclerView.scrollY)
//                mRecyclerView.currentScrollX = time2distance.toInt()
                mCurrentLine = mRecyclerView.getRealScrollX()
                if (mRecyclerView.scrollState != androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING) {
                    currentState = androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
                }
            }
        }
        mDragAdapter = DragAdapter(context, mBitmapListList, mPrimaryListBean, object : DragAdapter.OnItemStateListener {
            private var startTime = Long.MIN_VALUE
            private var endTime = Long.MIN_VALUE

            private val funUpRunnable = Runnable {
                if(mRecyclerView.scrollState !=  RecyclerView.SCROLL_STATE_SETTLING){
                    currentState = RecyclerView.SCROLL_STATE_IDLE
                    dragStateCallback.currentAllowDrag = true
                }
            }

            override fun onItemFunDown(bean: EffectBean?, left: Boolean, right: Boolean) {
                removeCallbacks(funUpRunnable)
                startTime = bean?.videoTime?.dstStartTime ?: -1L
                endTime = bean?.videoTime?.dstEndTime ?: -1L
                currentState = RecyclerView.SCROLL_STATE_DRAGGING
                dragStateCallback.currentAllowDrag = false
            }
            override fun onItemFunUp(bean: EffectBean?, left: Boolean, right: Boolean) {
                if (bean != null && startTime != Long.MIN_VALUE && endTime != Long.MIN_VALUE) {
                    onEffectEditListener?.trimEffectInTimeLine(bean, startTime, endTime)
                }
                removeCallbacks(funUpRunnable)
                postDelayed(funUpRunnable, 2000)
            }

            override fun checkFunMove(bean: EffectBean?, left: Boolean, right: Boolean, offsetX: Float): Boolean {
                if (bean != null) {
                    if (right) {
                        val timeOffset = distance2time(offsetX)
                        return bean.allowMoveDst(null, endTime + timeOffset)
                    } else if (left) {
                        val timeOffset = distance2time(offsetX)
                        return bean.allowMoveDst(startTime - timeOffset, null)
                    }
                }
                return true
            }

            override fun onItemFunMoving(bean: EffectBean?, left: Boolean, right: Boolean, x: Float) {
                val timeOffset = distance2time(x)
                if (left) {
                    if (startTime != Long.MIN_VALUE) {
                        startTime += timeOffset
                    }
                    recyclerViewScrollBy(-x.toInt(), mRecyclerView.scrollY)
                } else {
                    if (endTime != Long.MIN_VALUE) {
                        endTime += timeOffset
                    }
                }
            }

            override fun onItemClick(bean: EffectBean?, position: Int) {
                val selectBean = if (position in 0 until mPrimaryListBean.beans.size) {
                    mPrimaryListBean.beans[position]
                } else bean
                if (selectBean != null) {
                    onEffectSelectListener?.onSelectPrimaryEffect(selectBean)
                    mRecyclerView.setSelectedPosition(position)
                }
            }

            override fun onItemLongClick(bean: EffectBean?, position: Int) {
            }
        })
        mDragAdapter?.setCellSize(mCellWidth, mCellWidth)
        mRecyclerView.adapter = mDragAdapter
        DragHelper.bind(mRecyclerView, mPrimaryListBean.beans, dragStateCallback)


        mRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(mRecyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(mRecyclerView, dx, dy)
                onEffectScroll(dx.toFloat(), dy.toFloat())
            }

            override fun onScrollStateChanged(mRecyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(mRecyclerView, newState)
                currentState = newState
                when (currentState) {
                    androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE -> onEffectStateChangeListener?.onTimelineStart()
                    androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING -> onEffectStateChangeListener?.onTimelinePause()
                }
            }
        })

        mRecyclerView.setItemMargin(mMartinStart, - mTimeLineViewFunWidth.toInt() * 2,mMartinEnd)

    }

    private fun changeSecondaryEffectOffset(offset: Float) {
        val bean = mCurrentMoveSecondaryEffectBean
        if (bean?.primary != false || offset == 0f) {
            return
        }
        val timeOffset = distance2time(offset)
        val dstStartTime = bean.videoTime.dstStartTime + timeOffset
        val dstEndTime = bean.videoTime.dstEndTime + timeOffset

        if (dstStartTime < 0) {
            return
        }

        onEffectEditListener?.trimEffectInTimeLine(bean, dstStartTime, null)

        updateBeanTag(bean)

        invalidate()
    }

    private fun checkEffectTimeAndDuration(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float): Boolean {
        val bean = mCurrentSelectEffectBean ?: return false
        val offset = distance2time(offsetX)
        return if (right) {
            bean.allowMoveDst(null, endTime = bean.videoTime.dstEndTime + offset)
        } else true
    }

    /**
     * 遍历mHashEffectList 调整时间
     * 修改目标视频开始时间并修改时长
     */
    private fun changeEffectTimeAndDuration(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float) {
        val bean = mCurrentSelectEffectBean ?: return
        val offset = distance2time(offsetX)
        var startTime: Long = bean.videoTime.dstStartTime
        var endTime: Long = bean.videoTime.dstEndTime
        var needUpdateTag = false
        if (left) {
            if(bean.videoTime.dstStartTime + offset <= 0){
                return
            }
            timeLineView.x += offsetX
            startTime += offset

            if (bean.primary) {
            } else {
                needUpdateTag = true
            }
        } else if (right) {
            endTime += offset
        }

        onEffectEditListener?.trimEffectInTimeLine(bean, startTime, endTime)

        if (needUpdateTag) {
            updateBeanTag(bean)
        }
    }

    private fun updateBeanTag(bean: EffectBean) {
        val removeKeys = ArrayList<Long>()
        val dstStartTime = bean.videoTime.dstStartTime
        var addTime: Long? = null
        //清除
        mHashEffectList.map {
            val key = it.key
            it.value.remove(bean)
            if (it.value.getSize() == 0) {
                removeKeys.add(key)
            } else {
                if (dstStartTime in key - 50..key + 50) {
                    addTime = key
                }
            }
            adjustIconTopOffset(it.value)
        }
        removeKeys.map {
            mHashEffectList.remove(it)
        }

        //添加
        var tempAddTime = addTime
        var effectListBean = if (tempAddTime != null) {
            mHashEffectList[tempAddTime]
        } else mHashEffectList[dstStartTime]
        if (effectListBean == null) {
            effectListBean = EffectListBean()
            effectListBean.add(bean)
            mHashEffectList[dstStartTime] = effectListBean
        } else {
            effectListBean.add(bean)
        }
        adjustIconTopOffset(effectListBean)
    }

    /**
     * 更新时间线
     */
    private fun updateTimeline() {
        totalTime = mPrimaryListBean.totalDuration
        //计算占位宽度
        placeHolderWidth = (mPrimaryListBean.secondExtraTime.toFloat() / com.android.absbase.utils.TimeUtils.TimeConstant.ONE_SEC) * mCellWidth
        mDragAdapter?.adjustPlaceholderWidth(placeHolderWidth.toInt())
        timeLineWidth = mPrimaryListBean.getCropTotalWidth(mCellWidth, mCellPadding).toFloat() + placeHolderWidth
        speed = timeLineWidth / totalTime
        mTimelineEnd = (timeLineWidth + mMartinStart).toInt()

        if (mCurrentLine < 0) {
//            mRecyclerView.currentScrollX = 0
            mCurrentLine = 0f
            mCurrentTimelineOffset = distance2time(mCurrentLine)
        } else if (mCurrentLine > timeLineWidth) {
//            mRecyclerView.currentScrollX = timeLineWidth.toInt()
            mCurrentLine = timeLineWidth
            mCurrentTimelineOffset = distance2time(mCurrentLine)
        }

        updateAddBtnState()
        invalidate()
    }

    private var mTouchPrevX = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val event = event ?: return super.onTouchEvent(event)
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchPrevX = event.x
                currentState = androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
                onEffectStateChangeListener?.onTimelinePause()
            }
            MotionEvent.ACTION_MOVE -> {
                if (mCurrentMoveSecondaryEffectState) {
                    changeSecondaryEffectOffset(event.x - mTouchPrevX)
                }
            }
            MotionEvent.ACTION_UP -> {
                if(mRecyclerView.scrollState !=  androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING){
                    currentState = androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
                    onEffectStateChangeListener?.onTimelineStart()
                }
                if (mCurrentMoveSecondaryEffectState) {
                    changeSecondaryEffectOffset(event.x - mTouchPrevX)
                    mCurrentMoveSecondaryEffectState = false
                    invalidate()
                }
            }
        }
        mTouchPrevX = event.x
        return mGestureDetector.onTouchEvent(event)
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        //这里判断点击 特效 Tag
        val effectBean = selectEffectTap(e)
        if(effectBean != null){
            onEffectSelectListener?.onSelectSecondaryEffect(effectBean)
        }else{
            onEffectSelectListener?.onNoSelect()
        }



        return true
    }

    private fun selectEffectTap(e: MotionEvent?): EffectBean? {
        for (it in mHashEffectList.iterator()) {
            val currentMapBeans = it.value
            val validList = currentMapBeans.beans.filter {
                it.state != EffectBean.STATE_DELETE
            } as MutableList<EffectBean>
            currentMapBeans.clear()
            currentMapBeans.addAll(validList)
            val tab = containsTab(e, tagEffectBeans)
            val currentSelectEffectBean = mCurrentSelectEffectBean
            if(tab != null){
                //第二次点击切换effectBean
                if(tagEffectBeans.beans.contains(currentSelectEffectBean)){
                    tagEffectBeans.switchEffect()
                    //重新调整IconTopOffset
                    adjustIconTopOffset(tagEffectBeans)
                    invalidate()
                }
                return  tagEffectBeans.beans.last()
            }
            val bean = containsTab(e, currentMapBeans)
            if(bean != null){
                //第二次点击切换effectBean
                if (currentSelectEffectBean != null && currentMapBeans.contains(currentSelectEffectBean)) {
                    if (currentSelectEffectBean != bean) {
                        currentMapBeans.switchEffect()
                        //重新调整IconTopOffset
                        adjustIconTopOffset(currentMapBeans)
                        invalidate()
                    }
                }
                return  currentMapBeans.beans.last()
            }
        }
        return null
    }


    /**
     * 点击是否选择tab
     */
    private fun containsTab(e: MotionEvent?, effectListBean: EffectListBean?): EffectBean? {
        effectListBean ?: return null
        val x = e?.x ?: 0f
        val y = e?.y ?: 0f
        val tabRectF = RectF()
        val radius = mAddEffectLabelWidth / 2
        for (index in effectListBean.beans.indices){
            val bean = effectListBean.beans[index]
            val tabCenterX = bean.videoTime.dstStartTime * speed - mCurrentLine + mMartinStart
            val centerX = if (tabCenterX in (-(bean.dstDuration * speed) + radius)..radius) { radius } else{ tabCenterX }
            val iconTopOffset = mIconTopOffsets[bean.id]?:mDefaultTopOffset
            val tabCenterY = mAddEffectLabelMarkingTop - mAddEffectLabelCableHeight - radius - iconTopOffset
            tabRectF.set(centerX - radius, tabCenterY - radius, centerX + radius, tabCenterY + radius)
            if(bean.state != EffectBean.STATE_DELETE && tabRectF.contains(x,y)){
                return bean
            }
        }
        return null
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        recyclerViewScrollBy(distanceX.toInt(), distanceY.toInt())
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        val bean = selectEffectTap(e)
        if (bean?.primary == false) {
            mCurrentMoveSecondaryEffectState = true
            mCurrentMoveSecondaryEffectBean = bean
        }
    }

    /**
     * 距离转时间
     */
    private fun distance2time(dx: Float): Long {
        return (dx / speed).toLong()
    }


    /**
     * 时间转距离
     * @param intervalTime  时间间隔
     */
    private fun time2distance(intervalTime: Long): Float {
        return speed * intervalTime
    }

    /**
     * 特效滚动
     */
    private fun onEffectScroll(distanceX: Float, distanceY: Float) {
        if (currentState != androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {
            val prevLine = mCurrentLine
            mCurrentLine = mRecyclerView.getRealScrollX()
//            if (mCurrentLine < 0) {
//                mCurrentLine = 0f
//            }
//            if (mCurrentLine > timeLineWidth) {
//                mCurrentLine = timeLineWidth
//            }
            postInvalidate()
            val newLine = mCurrentLine - prevLine
            onEffectStateChangeListener?.onTimelineOffset(distance2time(newLine))
        }

        updateAddBtnState()

        var lastDrawBean: EffectBean? = null
        tagEffectBeans.clear()
        mHashEffectList.map {
            adjustIconTopOffset(it.value)
            it.value.beans.map { bean ->
                if (bean.state != EffectBean.STATE_DELETE) {
                    val tagRealX = bean.videoTime.dstStartTime * speed - mCurrentLine + mMartinStart
                    val width = bean.dstDuration * speed
                    val radius = mAddEffectLabelWidth / 2
                    if (tagRealX in (-width + radius)..radius) {
                        if(mCurrentSelectEffectBean != bean){
                            tagEffectBeans.add(bean)
                            adjustIconTopOffset(tagEffectBeans)
                        }else{
                            lastDrawBean = bean
                        }
                    }

                }
            }
        }
        lastDrawBean?.let {
            tagEffectBeans.add(it)
            adjustIconTopOffset(tagEffectBeans)
        }
    }

    private fun updateAddBtnState() {
        // 处理添加特效按钮的位置
        val marginEnd = width - (mTimelineEnd - mCurrentLine) + placeHolderWidth
        val lp = mAddBtn.layoutParams as? RelativeLayout.LayoutParams
        if (lp != null) {
            lp.marginEnd = if (marginEnd < mAddBtn.width / 2) 0 else marginEnd.toInt() - mAddBtn.width / 2
            mAddBtn.layoutParams = lp
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }


    override fun playFrameStart() {
    }

    override fun playFramePause() {

    }

    /**
     *  用来调整精度
     */
    private var remainderOffset = 0f

    private fun recyclerViewScrollBy(distanceX: Int, distanceY: Int) {
//        val offsetF = distanceX + remainderOffset
//        val offset = Math.round(offsetF)
        mRecyclerView.scrollBy(distanceX, distanceY)
//        remainderOffset = offsetF - offset
    }

    override fun frameAtTime(currentPlayTimeReferenceOffset: Long, currentPlayTimeReferenceStart: Long, auto: Boolean) {
        mCurrentTimelineOffset = currentPlayTimeReferenceStart

        // recyclerview偏移范围只能是在0 - mCurrentLine之间
//        val prevScrollX = mRecyclerView.currentScrollX
        val prevScrollX = mRecyclerView.getRealScrollX()
        val prevLine = prevScrollX
        mCurrentLine = time2distance(mCurrentTimelineOffset)
        val currentLine = mCurrentLine + remainderOffset
        val distanceOffset = Math.round(currentLine - prevLine)
        remainderOffset = currentLine - prevLine - distanceOffset

        if (auto) {
            mRecyclerView.scrollBy(distanceOffset, centerLineStartY.toInt())
        }
        invalidate()
    }

    override fun playFrameEnd() {
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val canvas = canvas ?: return
        //////// 时间轴 start
        val offsetY = mVisibleDisplayRect.top
        // 画时间轴背景
        mPaint.color = mTimeLineBackgroundColor
        canvas.drawRect(0f, offsetY.toFloat(), width.toFloat(), offsetY.toFloat() + mTimeLineHeight, mPaint)

        val midline = width.toFloat() / 2

        val fontMetrics = mPaint.fontMetrics
        val top = fontMetrics.top//为基线到字体上边框的距离,即上图中的top
        val bottom = fontMetrics.bottom//为基线到字体下边框的距离,即上图中的bottom
        val baseLineY = mTimeLineHeight / 2 - top / 2 - bottom / 2//基线中间点的y轴计算公式

        // 画当前中线的时间
        mPaint.color = mTimeLineCurrentTimeColor
        mPaint.textSize = mTimeLineCurrentTimeTextSize
        val formatTime = TimeUtils.formatTime(mCurrentTimelineOffset)
        val centerTimeTextWidth = mPaint.measureText(formatTime)
        val centerTimeTextX = midline - centerTimeTextWidth / 2
        val centerTimeTextLeft = midline - mTimeLineCurrentTimeWidth / 2
        val centerTimeTextRight = centerTimeTextLeft + mTimeLineCurrentTimeWidth
        canvas.drawText(formatTime, centerTimeTextX, offsetY + baseLineY, mPaint)

        mPaint.strokeWidth = mTimeLineIntervalDotWidth
        mPaint.color = mTimeLineIntervalDotColor
        mPaint.textSize = mTimeLineIntervalDotTextSize
        for (index in 0..totalTime step step) {
            val x = index * speed - mCurrentLine + mMartinStart
            if (x < 0) continue
            if (x > width) continue

            if (index % (step * 3) == 0L) {
                val ts = "${(index + 999) / 1000 % 60}"
                val tsw = mPaint.measureText(ts)
                val x = x - tsw / 2
                if (x in centerTimeTextLeft..centerTimeTextRight || (x + tsw) in centerTimeTextLeft..centerTimeTextRight) {
                    continue
                }
                canvas.drawText(ts, x, offsetY + baseLineY, mPaint)
            } else {
                if (x in centerTimeTextLeft..centerTimeTextRight || (x + mTimeLineIntervalDotWidth) in centerTimeTextLeft..centerTimeTextRight) {
                    continue
                }
                canvas.drawCircle(x, offsetY + mTimeLineHeight / 2, mTimeLineIntervalDotWidth / 2, mPaint)
            }
        }

        //////// 时间轴 end

        //////// 中心线下部分 start
        // 这部分是要画到子view上面的
        val preAntiAlias = mPaint.isAntiAlias
        mPaint.isAntiAlias = false
        mPaint.color = mTimeLineTimeLineColor
        val prevStrokeWidth = mPaint.strokeWidth
        mPaint.strokeWidth = mTimeLineMarkingWidth
        centerLineStartX = midline
        centerLineStartY = max(mVisibleDisplayRect.top + mTimeLineHeight, mRecyclerView.top.toFloat())
        canvas.drawLine(centerLineStartX, centerLineStartY, width.toFloat() / 2, height.toFloat(), mPaint)
        mPaint.strokeWidth = prevStrokeWidth
        mPaint.isAntiAlias = preAntiAlias
        //////// 中心线下部分 end

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val canvas = canvas ?: return

        if (mCurrentMoveSecondaryEffectState) {
            mPaint.color = mAddEffectLabelMoveSecondaryMaskColor
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint)
        }

        //////// 中心线上部分 start
        // 这部分是要画到子view下面的
        val preAntiAlias = mPaint.isAntiAlias
        mPaint.isAntiAlias = false
        mPaint.color = mTimeLineTimeLineColor
        val prevStrokeWidth = mPaint.strokeWidth
        mPaint.strokeWidth = mTimeLineMarkingWidth
        val centerLineStartY = min(mVisibleDisplayRect.top + mTimeLineHeight, mRecyclerView.top.toFloat())
        canvas.drawLine(width.toFloat() / 2, centerLineStartY, width.toFloat() / 2, mRecyclerView.top.toFloat(), mPaint)
        mPaint.strokeWidth = prevStrokeWidth
        mPaint.isAntiAlias = preAntiAlias
        //////// 中心线上部分 end

        if(DebugUtil.isDebuggable()){
            canvas.drawText("cLx:$mCurrentLine",width.toFloat() / 2, centerLineStartY + 30,mPaint)
            val currentTimeLineX = time2distance(mCurrentTimelineOffset)
            canvas.drawText("TlX:$currentTimeLineX",width.toFloat() / 2, centerLineStartY + 60,mPaint)
            val realScrollX = mRecyclerView.getRealScrollX()
            canvas.drawText("rlX:$realScrollX",width.toFloat() / 2, centerLineStartY + 90,mPaint)
        }
        // 画新增的特效标签
        drawAddEffect(canvas)
        if(mCurrentSecondlyEditMode && !dragStartTimeState){
            dragStartTimeState = false
            mCurrentEffectFrameView.x = (mCurrentSelectEffectBean?.videoTime?.dstStartTime ?: 0) * speed - mCurrentLine + mMartinStart - mCurrentEffectFrameView.getStartImageWidth() - mCurrentEffectFrameView.getStartImageLeft()
        }
    }

    private fun drawAddEffect(canvas: Canvas) {
        val currentNeedDrawBeans: EffectListBean = EffectListBean()
        mHashEffectList.map {
            it.value.beans.map { bean ->
                if (bean.state != EffectBean.STATE_DELETE) {
                    if (!tagEffectBeans.beans.contains(bean)) {
                        currentNeedDrawBeans.add(bean)
                    }
                }
            }
        }
        currentNeedDrawBeans.beans.map {
            drawAddEffectTag(canvas, it)
        }
        tagEffectBeans.beans.map {
            drawAddEffectTag(canvas, it)
        }
    }

    private fun drawAddEffectTag(canvas: Canvas, bean: EffectBean) {
        val tagRealX = bean.videoTime.dstStartTime * speed - mCurrentLine + mMartinStart
        val width = bean.dstDuration * speed
        var radius = mAddEffectLabelWidth / 2
        var centerX = if (tagRealX in (-width + radius)..radius) { radius } else{ tagRealX }
        val iconTopOffset = mIconTopOffsets[bean.id]?:mDefaultTopOffset
        // 画添加的特效的标签
        var labelCenterY = mAddEffectLabelMarkingTop - mAddEffectLabelCableHeight - radius - iconTopOffset

        val labelBackgroundDrawable = mAddEffectLabelBackground
        if (mCurrentMoveSecondaryEffectState && bean == mCurrentMoveSecondaryEffectBean) {
            radius += mAddEffectLabelZoomPadding
            labelCenterY -= mAddEffectLabelZoomPadding
        }
        labelBackgroundDrawable.setBounds((centerX-radius).toInt(),
                (labelCenterY-radius).toInt(),
                (centerX+radius).toInt(),
                (labelCenterY+radius).toInt())
        val labelStartColor = bean.labelStartColor
        val labelEndColor = bean.labelEndColor ?: mAddEffectLabelDefaultColor
        if (labelBackgroundDrawable is GradientDrawable) {
            val colors = when {
                labelStartColor != null -> intArrayOf(labelStartColor, labelEndColor)
                else -> intArrayOf(labelEndColor)
            }
            if (colors != null) {
                labelBackgroundDrawable.colors = colors
            }
            if (mCurrentSelectEffectBean == bean) {
                labelBackgroundDrawable.setStroke(mAddEffectLabelSelectStroke, mAddEffectLabelSelectStrokeColor)
            } else {
                labelBackgroundDrawable.setStroke(0, Color.TRANSPARENT)
            }
        }
        labelBackgroundDrawable.draw(canvas)

        // 画添加的特效的logo
        val drawable = bean.smallIcon
        if (drawable != null) {
            val dw = drawable.intrinsicWidth
            val dh = drawable.intrinsicHeight
            val dl = centerX - dw / 2
            val dt = labelCenterY - dh / 2
            val dr = dl + dw
            val db = dt + dh
            drawable.setBounds(dl.toInt(), dt.toInt(), dr.toInt(), db.toInt())
            drawable.draw(canvas)
        }

        var effectLabelCableHeight = 0f
        effectLabelCableHeight = if (mCurrentSelectEffectBean == bean) {
            //选中时候，连接线与下面控件有3dp间隙
            mAddEffectLabelMarkingTop - mEffectLabelCableSelectedInterval
        } else {
            mAddEffectLabelMarkingTop + mAddEffectLabelMarkingHeight
        }

        val prevColor = mPaint.color
        mPaint.color = labelEndColor
        // 画添加的特效的连接线
        val preStrokeWidth = mPaint.strokeWidth
        var preAnitAlias = mPaint.isAntiAlias
        mPaint.isAntiAlias = false
        mPaint.strokeWidth = mAddEffectLabelCableWidth
        canvas.drawLine(centerX,
                mAddEffectLabelMarkingTop - mAddEffectLabelCableHeight - iconTopOffset,
                centerX,
                effectLabelCableHeight, mPaint)

        mPaint.strokeWidth = preStrokeWidth
        mPaint.isAntiAlias = preAnitAlias

        if (mCurrentSelectEffectBean != bean) {
            // 画添加的特效的宽度
            canvas.drawRect(tagRealX,
                    mAddEffectLabelMarkingTop,
                    tagRealX + width,
                    mAddEffectLabelMarkingTop + mAddEffectLabelMarkingHeight, mPaint)

            preAnitAlias = mPaint.isAntiAlias
            mPaint.isAntiAlias = true
            // 画添加的特效的结束标记
            canvas.drawCircle(tagRealX + width - mAddEffectLabelMarkingDotRadius,
                    mAddEffectLabelMarkingTop - mAddEffectLabelMarkingDotRadius - mAddEffectLabelMarkingDotBottomMargin,
                    mAddEffectLabelMarkingDotRadius, mPaint)
            mPaint.color = prevColor
            mPaint.isAntiAlias = preAnitAlias
        }
        if( mCurrentEffectFrameView.effectBean == bean && bean.type == MediaTypeUtil.TYPE_TEXT){
            val contentView = mCurrentEffectFrameView.contentView
            if (tagRealX in - ( width + mCurrentEffectFrameView.getEndImageWidth().toFloat())..0f) {
                contentView?.translationX  = -tagRealX
            }else{
                contentView?.translationX = 0f
            }
        }
    }



    fun setData(primaryBeans: EffectListBean, secondaryBeans: EffectListBean) {
        mIconTopOffsets.clear()
        mPrimaryListBean = primaryBeans
        mSecondaryListBean = secondaryBeans
        mDragAdapter?.setData(mPrimaryListBean)
        mDragAdapter?.notifyDataSetChanged()
        updateTimeline()
    }

    fun add(effect: EffectBean, offsetTime:Long){
        mDrawEffectListBean.add(effect)
        if(mHashEffectList.containsKey(offsetTime)){
            val listBean = mHashEffectList[offsetTime]
            listBean?.add(effect)
        }else{
            val effectList = EffectListBean()
            effectList.add(effect)
            mHashEffectList[offsetTime] = effectList
        }
        mHashEffectList.map {
            adjustIconTopOffset(it.value)
        }
        invalidate()
    }

    fun selectEffectBean(effectBean: EffectBean?) {
        mCurrentSelectEffectBean = effectBean
        showSecondlyEditEffect(false)
        val postion = mDragAdapter?.selectData(effectBean) ?: -1
        mRecyclerView.setSelectedPosition(postion)
        if(effectBean != null){
            if(effectBean.primary){

            }else{
                //调整top offset
                val effectListBean = mHashEffectList[effectBean.videoTime.dstStartTime]
                if(effectListBean != null && !tagEffectBeans.beans.contains(effectBean)){
                    effectListBean.switchEffect(effectBean)
                    adjustIconTopOffset(effectListBean)
                    invalidate()
                }
                showSecondlyEditEffect(true, effectBean)
            }
        }
    }

    private fun showSecondlyEditEffect(show: Boolean, effectBean: EffectBean? = null) {
        if (show && effectBean != null) {
            mCurrentSecondlyEditMode = true
            mCurrentEffectFrameView.visibility = View.VISIBLE
            val lp = mRecyclerView.layoutParams as RelativeLayout.LayoutParams
            lp.topMargin = mSelectedEffectStateListTop.toInt()
            TimeLineViewFactory.updateView(
                mCurrentEffectFrameView,
                effectBean,
                speed,
                mAddEffectTimeLineContentHeight
            )
            mCurrentEffectFrameView.contentView?.setBackgroundColor(effectBean.labelEndColor?:mAddEffectLabelDefaultColor)
            mCurrentEffectFrameView.x = effectBean.videoTime.dstStartTime * speed - mCurrentLine + mMartinStart - mCurrentEffectFrameView.getStartImageWidth() - mCurrentEffectFrameView.getStartImageLeft()

        } else {
            if (mCurrentSecondlyEditMode) {
                mCurrentEffectFrameView.visibility = View.GONE
                val lp = mRecyclerView.layoutParams as RelativeLayout.LayoutParams
                lp.topMargin = mNormalStateListTop.toInt()
                mCurrentSecondlyEditMode = false
            }
        }
    }

    /**
     * 调整Icon的上偏移
     * 第三个开始默认都是18 pixels
     */
    private fun adjustIconTopOffset(effectListBean: EffectListBean) {
        // 去除无效特效
        val validList = effectListBean.beans.filter {
            it.state != EffectBean.STATE_DELETE
        } as MutableList<EffectBean>
        val size = validList.size
        for(index in 0 until size){
            val bean = validList[(size - 1) - index]
            //检测多少段开始播放的时间相同
            var count =  index
            if(count > 2){
                count = 2
            }
            val offset = mDefaultTopOffset * count
            mIconTopOffsets[bean.id] = offset
        }
    }

    override fun getEffectView(bean: EffectBean?): View? {

        return null
    }

    override fun removeEffectView(bean: EffectBean) {

    }
    override fun updateState() {
        val currentSelectEffectBean = mCurrentSelectEffectBean
        if (currentSelectEffectBean != null) {
            if (currentSelectEffectBean.isDelete()) {
                selectEffectBean(null)
            }
            TimeLineViewFactory.updateView(
                mCurrentEffectFrameView,
                currentSelectEffectBean,
                speed
            )
        }
        if (!mRecyclerView.isComputingLayout) {
            mDragAdapter?.notifyDataSetChanged()
        }
        updateTimeline()
    }

    override fun invalidateSelf() {


    }

    override fun getEffectViewRect(bean: EffectBean?): RectF {
        return RectF()
    }


    override fun getVideoRect(): Rect {
        return Rect()
    }

    override fun onClick(v: View?) {
        when (v) {
            mAddBtn -> {
                onEffectEditListener?.requestAddEffect()
            }
        }
    }
    interface OnItemListener {
        fun onItemRangeMoved()
    }
}
