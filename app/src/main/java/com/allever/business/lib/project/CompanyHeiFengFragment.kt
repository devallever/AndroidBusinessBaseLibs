package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class CompanyHeiFengFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("AI健身", "sample-company-heifeng-ai-fitness"),
        TextDetailClickItem("机器人", "sample-company-heifeng-robot"),
        TextDetailClickItem("灯控", "sample-company-heifeng-light-control"),
    )
}