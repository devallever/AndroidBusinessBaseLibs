package com.allever.video.editor.ui.widget

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

/***
 * https://www.jianshu.com/p/d6fa7bbe80af
 */
class EmptyRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.recyclerview.widget.RecyclerView(context, attrs, defStyleAttr) {
    private var mEmptyView: View? = null

    /**
     * 创建一个观察者
     * 为什么要在onChanged里面写？
     * 因为每次notifyDataChanged的时候，系统都会调用这个观察者的onChange函数
     * 我们大可以在这个观察者这里判断我们的逻辑，就是显示隐藏
     */
    private val emptyObserver = object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            val adapter = adapter
            //这种写发跟之前我们之前看到的ListView的是一样的，判断数据为空否，在进行显示或者隐藏
            if (adapter != null) {
                if (adapter.itemCount == 0) {
                    mEmptyView?.visibility = View.VISIBLE
                    this@EmptyRecyclerView.visibility = View.GONE
                } else {
                    mEmptyView?.visibility = View.GONE
                    this@EmptyRecyclerView.visibility = View.VISIBLE
                }
            }

        }
    }

    fun setEmptyView(emptyView: View?) {
        mEmptyView = emptyView
    }

    override fun setAdapter(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>?) {
        super.setAdapter(adapter)

        adapter?.registerAdapterDataObserver(emptyObserver)
        //当setAdapter的时候也调一次
        emptyObserver.onChanged()
    }
}
