package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.allever.videoeditordemo.DragAdapter


class EffectRecyclerView : androidx.recyclerview.widget.RecyclerView {
    private var mSelectedPosition = 0
    private var itemDecoration: SpaceItemDecoration? = null
    private var mFirstItemOffsetX = 0
//    var currentScrollX = 0
    private var currentCacheSize = 6
    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        //启用子视图排序功能     
        isChildrenDrawingOrderEnabled = true

        // 需要设置一个合适的size, 解决往回滚时,突然跳过某几个item的问题
        //设置当前ViewHolder缓存数量不能小于adapter.ItemCount()，不然就会出现滚动位置错乱问题
        setItemViewCacheSize(currentCacheSize)

        addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(mRecyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(mRecyclerView, dx, dy)
//                currentScrollX += dx
//                DLog.e("breeze","currentScrollX    $currentScrollX" )
            }

            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        this.layoutManager = layoutManager
        itemDecoration = SpaceItemDecoration(0, 0, 0)
        this.addItemDecoration(itemDecoration!!)
    }

    fun getRealScrollX():Float{
        var offset = 0f
        val layoutManager = this.layoutManager
        if(layoutManager is androidx.recyclerview.widget.LinearLayoutManager){
            val visibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val adapter = adapter
            if( adapter is DragAdapter){
                val itemCount = adapter.itemCount
                if(currentCacheSize < itemCount){
                    setItemViewCacheSize(itemCount)
                    currentCacheSize = itemCount
                }
                val firstVisibleChildView = layoutManager.findViewByPosition(visibleItemPosition)
                val width = adapter.getItemWidth(0,visibleItemPosition -1)
                offset = width - firstVisibleChildView!!.x + mFirstItemOffsetX
            }
        }
        return offset
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)

//        currentScrollX = x
//        scrollBy(x - currentScrollX, y)
    }

    override fun onDraw(canvas: Canvas) {
        // focusedChild始终未null
//        val focusedChild = layoutManager.focusedChild
//        val focusedChild = focusedChild
//        mSelectedPosition = getChildAdapterPosition(focusedChild)
//        mSelectedPosition = indexOfChild(focusedChild);
        super.onDraw(canvas)
    }

    /**
     * focusedChild始终为null, 调整为外面告诉recyclerView谁被点击了
     */
    fun setSelectedPosition(position: Int) {
        mSelectedPosition = position
        invalidate()
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        // 这里传进来的childCount并不是adapter的所有个数,而是当前可见的个数, i也是 0...childCount
        var position = mSelectedPosition;
        val linearLayoutManager = layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
        if (linearLayoutManager != null) {
            val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
            position -= firstVisibleItemPosition
        }
        val currentDrawPos = if (position in 0 until childCount) {
            when {
                i < position -> i
                i == childCount - 1 -> position
                else -> i + 1
            }
        } else super.getChildDrawingOrder(childCount, i)
        return currentDrawPos
    }

    fun setItemMargin(startSpace: Int, space: Int, endSpace: Int) {
        itemDecoration?.startSpace = startSpace
        itemDecoration?.space = space
        itemDecoration?.endSpace = endSpace
        mFirstItemOffsetX = startSpace + space / 2
    }

    private inner class SpaceItemDecoration internal constructor(var startSpace: Int, var space: Int, var endSpace: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView,
                                    state: androidx.recyclerview.widget.RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            when (position) {
                0 -> outRect.left = (startSpace + space / 2)
                else -> outRect.left = +(space)
            }
            if (position + 1 == parent.adapter?.itemCount) {
                outRect.right = (endSpace + space / 2)
            }
        }
    }
}
