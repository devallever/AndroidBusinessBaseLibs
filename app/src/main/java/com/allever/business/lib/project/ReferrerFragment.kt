package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.therouter.TheRouter

class ReferrerFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("AppsFlyer") {
            TheRouter.build(RouterPath.PATH_SAMPLE_APPS_FLYER).navigation()
        },
        TextClickItem("Adjust") {
            TheRouter.build(RouterPath.PATH_SAMPLE_ADJUST).navigation()
        },
    )
}