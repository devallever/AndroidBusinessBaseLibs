package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.therouter.TheRouter

class AdFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("AdMob") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AD_ADMOB).navigation()
        },
        TextClickItem("Pangle") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AD_PANGLE).navigation()
        },
        TextClickItem("Bigo") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AD_BIGO).navigation()
        },
        TextClickItem("AppLovin") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AD_APPLOVIN).navigation()
        },
        TextClickItem("AdCore") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AD_CORE).navigation()
        },
    )
}