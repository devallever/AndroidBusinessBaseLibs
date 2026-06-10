package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter

class SampleOldListFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("谷歌内购订阅支付") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_BILLING).navigation()
        },
        TextClickItem("清理工具(Demo)") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_CLEANER).navigation()
        },
        TextClickItem("设计模式") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_DESIGN_PATTERN).navigation()
        },
        TextClickItem("功能实现") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_FUNCTION).navigation()
        },
        TextClickItem("Jetpack") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_JETPACK).navigation()
        },
        TextClickItem("JNI") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_JNI).navigation()
        },
        TextClickItem("JNI-MK") {
            ARouter.getInstance().build(RouterPath.PATH_Z_SAMPLE_JNI_MK).navigation()
        },
    )
}