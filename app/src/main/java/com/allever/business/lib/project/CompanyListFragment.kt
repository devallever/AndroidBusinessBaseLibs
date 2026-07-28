package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class CompanyListFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("饭团科技") {
            FragmentActivity.start<CompanyFanTuanFragment>(it.title)
        },
        TextDetailClickItem("黑蜂科技") {
            FragmentActivity.start<CompanyHeiFengFragment>(it.title)
        },
        TextDetailClickItem("天聊") {
            FragmentActivity.start<CompanyTianLiaoFragment>(it.title)
        },
        TextDetailClickItem("量岛科技") {
            FragmentActivity.start<CompanyLiangDaoFragment>(it.title)
        },
        TextDetailClickItem("书歌") {
            FragmentActivity.start<CompanyShuGeFragment>(it.title)
        },
    )
}