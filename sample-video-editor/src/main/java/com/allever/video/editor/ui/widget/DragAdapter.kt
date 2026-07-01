package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.function.editor.bean.EffectListBean

class DragAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private val TAG = DragAdapter::class.java.simpleName
        private const val TYPE_LAST = 10000
    }

    private lateinit var mEffectListBean: EffectListBean
    private var mBitmapListList = mutableListOf<MutableList<Bitmap>>()
    private var mItemStateListener: OnItemStateListener? = null
    private var mContext: Context? = null

    var currentSelectBean: EffectBean? = null
        private set
    var cellWidth = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_bitmap_width).toInt()
    var cellHeight = cellWidth
    var cellPadding = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_padding).toInt()

    private var placeHolder: RecyclerView.ViewHolder? = null
    constructor(context: Context, bitmapList: MutableList<MutableList<Bitmap>>, beans: EffectListBean, itemStateListener: OnItemStateListener) {
        mItemStateListener = itemStateListener
        mContext = context
        mBitmapListList = bitmapList
        mEffectListBean = beans
    }

    private val baseItemStateListener = object : OnItemStateListener {
        override fun onItemClick(bean: EffectBean?, position: Int) {
            mItemStateListener?.onItemClick(bean, position)
        }

        override fun onItemLongClick(bean: EffectBean?, position: Int) {
            mItemStateListener?.onItemLongClick(bean, position)
        }

        override fun onItemFunDown(bean: EffectBean?, left: Boolean, right: Boolean) {
            mItemStateListener?.onItemFunDown(bean, left, right)
        }

        override fun onItemFunUp(bean: EffectBean?, left: Boolean, right: Boolean) {
            mItemStateListener?.onItemFunUp(bean, left, right)
        }

        override fun checkFunMove(bean: EffectBean?, left: Boolean, right: Boolean, offsetX: Float): Boolean {
            return mItemStateListener?.checkFunMove(bean, left, right, offsetX) ?: true
        }

        override fun onItemFunMoving(bean: EffectBean?, left: Boolean, right: Boolean, x: Float) {
            mItemStateListener?.onItemFunMoving(bean, left, right, x)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == TYPE_LAST){
            val lastView = TextView(mContext)
            //少了一个格子的距离
            lastView.layoutParams = ViewGroup.LayoutParams(cellWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            PlaceHolderHolder(lastView)
        }else{
            val itemView = LayoutInflater.from(mContext).inflate(R.layout.ve_time_time_line, parent, false) as TimeLineView
            MyViewHolder(itemView, baseItemStateListener)
        }
    }

    override fun getItemCount(): Int = mEffectListBean.beans.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == mEffectListBean.beans.size){
            placeHolder = holder
        }else{
            if(holder is MyViewHolder){
                val timeLineView = holder.timeLineView
                timeLineView.tag = holder
                val bean = mEffectListBean.beans[position]
                timeLineView.effectBean = bean
                holder.bitmapContentView.setData(bean)
                holder.timeLineView.showFrame(currentSelectBean?.id == bean.id)
                holder.needUpdate()
            }
        }

    }

    fun setData(beans: EffectListBean) {
        mEffectListBean = beans
        notifyDataSetChanged()
    }

    fun setCellSize(width: Int, height: Int) {
        cellWidth = width
        cellHeight = height
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if(holder is MyViewHolder){
            holder.bitmapContentView.clear()
        }
    }

    /**
     * 选择特效
     */
    fun selectData(effectBean: EffectBean?): Int {
        val prevSelectBean = currentSelectBean
        if (prevSelectBean != null) {
            val prevIndex = mEffectListBean.findBeanIndex(prevSelectBean)
            notifyItemChanged(prevIndex, prevIndex)
        }
        currentSelectBean = effectBean
        return if (effectBean != null) {
            val beanIndex = mEffectListBean.findBeanIndex(effectBean)
            this.notifyItemChanged(beanIndex, beanIndex)
            beanIndex
        } else -1
    }

    /**
     * 调整占位符的宽度
     */
    fun adjustPlaceholderWidth(width: Int){
        placeHolder?.itemView?.layoutParams?.width = cellWidth + width
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    inner class MyViewHolder(var timeLineView: TimeLineView, onItemStateListener: OnItemStateListener?)
        : RecyclerView.ViewHolder(timeLineView) {
        private val TAG = MyViewHolder::class.java.simpleName
        var bitmapContentView: BitmapContentView =
            BitmapContentView(timeLineView.context)
        val timelinePadding = ResourcesUtils.getDimension(R.dimen.effect_edit_timeline_padding).toInt()

        init {
            bitmapContentView.setCellSize(cellWidth, cellHeight)
            bitmapContentView.setPadding(cellPadding)
            bitmapContentView.needToCutThroughTime = true

            timeLineView.contentView = bitmapContentView
            timeLineView.showFrame(false)
            timeLineView.setOptionListener(object : TimeLineView.OnOptionListener {
                override fun onClick(timeLineView: TimeLineView) {
                    onItemStateListener?.onItemClick(timeLineView.effectBean, adapterPosition)
                }

                override fun onLongClick(timeLineView: TimeLineView) {
                    onItemStateListener?.onItemLongClick(timeLineView.effectBean, adapterPosition)
                }

            })

            timeLineView.setMovingListener(object : TimeLineView.OnFunMovingListener {
                override fun onMoveToScreenEdge(timeLineView: TimeLineView, left: Boolean, right: Boolean) {
                }

                override fun onFunDown(timeLineView: TimeLineView, left: Boolean, right: Boolean) {
                    onItemStateListener?.onItemFunDown(timeLineView.effectBean, left, right)
                }

                override fun onFunUp(timeLineView: TimeLineView, left: Boolean, right: Boolean) {
                    onItemStateListener?.onItemFunUp(timeLineView.effectBean, left, right)
                }

                override fun checkFunMove(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float): Boolean {
                    return onItemStateListener?.checkFunMove(timeLineView.effectBean, left, right, offsetX)
                            ?: true
                }

                override fun onFunMoving(timeLineView: TimeLineView, left: Boolean, right: Boolean, offsetX: Float, isMoveToLeft: Boolean) {
                    onItemStateListener?.onItemFunMoving(timeLineView.effectBean, left, right, offsetX)
                }

                override fun onScrollStart() {
                }

                override fun onScrollOffset(offsetX: Float) {

                }

                override fun onScrollEnd() {
                }
            })
            needUpdate()
        }

        fun needUpdate() {
            bitmapContentView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    val width = bitmapContentView.width
                    if (width == 0) {
                        return false
                    }
                    bitmapContentView.viewTreeObserver.removeOnPreDrawListener(this)

                    timeLineView.setContentViewSize(width, cellHeight, timelinePadding)

                    return true
                }

            })
        }
    }

    inner class PlaceHolderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (mEffectListBean.beans.size  == position) {
            TYPE_LAST
        } else {
            super.getItemViewType(position)
        }
    }

    /**
     * 获取指定区间的宽度
     * @param startPosition 起始下标
     * @param endPosition 结束下标
     */
    fun getItemWidth(startPosition: Int, endPosition: Int): Int{
        var width  = 0
        if(startPosition <0 || endPosition > mEffectListBean.getSize()){
            return width
        }
        for (index in startPosition..endPosition){
            width += mEffectListBean.beans[index].getCropTotalWidth(cellWidth, cellPadding)
        }
        return width
    }



    interface OnItemStateListener {
        fun onItemClick(bean: EffectBean?, position: Int)
        fun onItemLongClick(bean: EffectBean?, position: Int)

        fun onItemFunDown(bean: EffectBean?, left: Boolean, right: Boolean)
        fun onItemFunUp(bean: EffectBean?, left: Boolean, right: Boolean)
        fun checkFunMove(bean: EffectBean?, left: Boolean, right: Boolean, offsetX: Float): Boolean
        fun onItemFunMoving(bean: EffectBean?, left: Boolean, right: Boolean, x: Float)
    }
}