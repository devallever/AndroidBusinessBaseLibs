package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding

class ReferrerFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("AppsFlyer") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APPS_FLYER)
        },
        TextClickItem("Adjust") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_ADJUST)
        },
    )
}