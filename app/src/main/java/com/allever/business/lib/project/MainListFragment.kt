package com.allever.business.lib.project

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ui.EmptyFragment
import com.alibaba.android.arouter.launcher.ARouter

class MainListFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("Lib") {
            FragmentActivity.start<EmptyFragment>(it.title)
        },
        TextClickItem("Sample") {
            FragmentActivity.start<EmptyFragment>(it.title)
        },
        TextClickItem("MVVM") {
            ARouter.getInstance().build(RouterPath.PATH_MVVM).navigation()
        },
    )
}