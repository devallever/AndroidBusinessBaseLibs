package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class CompanyShuGeFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("✅StepTool", "sample-app-step-tool") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_STEP_TOOL)
        },
        TextDetailClickItem("✅ChargeReward", "sample-app-charge-reward") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_CHARGE_REWARD)
        }
    )
}