package com.alsg.bakericon.base

import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.helper.KeyEventHelper
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

abstract class BaseListFragment<DB : ViewBinding, VM : BaseViewModel, T> :
    AppFragment<FragmentListBinding, ListViewModel>() {

    protected var mAdapter: BaseQuickAdapter<T, *>? = null

    override fun inflate() = FragmentListBinding.inflate(layoutInflater)

    override fun init() {

        initTopBar()

        mBinding.smartRefreshLayout.setEnableOverScrollDrag(true)

        mBinding.recyclerView.layoutManager = layoutManager()

        mAdapter = getAdapter()
        mBinding.recyclerView.adapter = mAdapter

        mAdapter?.setList(getList())

        mAdapter?.setOnItemClickListener { adapter, view, position ->
            onItemClick(position, mAdapter?.getItem(position) ?: return@setOnItemClickListener)
        }
    }

    private fun initTopBar() {
        val title = getTitle()
        val showTopBar = !TextUtils.isEmpty(title)
        if (showTopBar) {
            ViewHelper.setViewHeight(
                mBinding.statusBar,
                DisplayHelper.getStatusBarHeight(requireContext())
            )
            ViewHelper.setVisible(mBinding.topBar, showTopBar)
            mBinding.tvTitle.text = title
            mBinding.ivBack.setOnClickListener {
                KeyEventHelper.clickBack()
            }
        }
    }

    abstract fun getAdapter(): BaseQuickAdapter<T, *>
    abstract fun getList(): MutableList<T>
    open protected fun onItemClick(position: Int, item: T) {

    }

    open protected fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(requireContext())
    }

    open fun getTitle(): String {
        return ""
    }

    protected fun updateList(list: MutableList<T>) {
        mAdapter?.setList(list)
    }
}

class ListViewModel : BaseViewModel() {

}