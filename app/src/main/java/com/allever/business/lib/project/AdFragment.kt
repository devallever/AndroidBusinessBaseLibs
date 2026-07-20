package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding

class AdFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("AdMob") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_ADMOB)
        },
        TextClickItem("Pangle") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_PANGLE)
        },
        TextClickItem("Bigo") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_BIGO)
        },
        TextClickItem("AppLovin") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_APPLOVIN)
        },
        TextClickItem("AdCore") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_CORE)
        },
    )
}