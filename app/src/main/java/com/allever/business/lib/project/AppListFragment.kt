package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter

class AppListFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("私密相册", "sample-app-secret-album") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_SECRET_ALBUM).navigation()
        },
        TextDetailClickItem("视频编辑", "sample-video-editor") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VIDEO_EDITOR).navigation()
        },
        TextDetailClickItem("网络测速", "sample-net-speed-test") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_NET_SPEED_TEST).navigation()
        },

        TextDetailClickItem("清理-Wood", "sample-cleaner-wood") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CLEANER_WOOD).navigation()
        },
        TextDetailClickItem("网赚-ChargeReward", "sample-chargereward") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CHARGE_REWARD).navigation()
        },
        TextDetailClickItem("网赚-记录步数", "sample-step-tool") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_STEP_TOOL).navigation()
        },
        TextDetailClickItem("短剧-穿山甲", "sample-djcsj") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_DJ_CSJ).navigation()
        },
        TextDetailClickItem("VPN-FlashTunnel", "sample-vpn-flashtunnel") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VPN_FLASH_TUNNEL).navigation()
        },
    )
}