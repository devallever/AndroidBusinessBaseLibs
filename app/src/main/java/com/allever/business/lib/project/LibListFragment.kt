package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter

class LibListFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("MVVM") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_MVVM).navigation()
        },
        TextClickItem("Ad") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AD_CORE).navigation()
        },
        TextClickItem("Media") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_MEDIA).navigation()
        },
        TextClickItem("Network") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_NETWORK_CORE).navigation()
        },

    )
}