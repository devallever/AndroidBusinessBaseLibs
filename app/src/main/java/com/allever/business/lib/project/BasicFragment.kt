package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter

class BasicFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()
    override fun getList() = mutableListOf(
        TextClickItem("Mvvm") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_MVVM).navigation()
        },
        TextClickItem("Permission") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_PERMISSION).navigation()
        },
        TextClickItem("Media") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_MEDIA).navigation()
        },
        TextClickItem("NetworkCore") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_NETWORK_CORE).navigation()
        },
        TextClickItem("PlayerCore") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_PLAYER_CORE).navigation()
        },
        TextClickItem("存储") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_STORE_CORE).navigation()
        },
        TextClickItem("图片加载") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_IMAGE_LOADER_CORE).navigation()
        },
    )
}