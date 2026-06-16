package com.allever.business.lib.project

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter

class SampleListFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("Basic") {
            FragmentActivity.start<BasicFragment>(it.title)
        },
        TextClickItem("Ad") {
            FragmentActivity.start<AdFragment>(it.title)
        },
        TextClickItem("Referrer") {
            FragmentActivity.start<ReferrerFragment>(it.title)
        },
        TextClickItem("音视频") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AUDIO_VIDEO).navigation()
        },
        TextClickItem("CameraCore") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CAMERA_CORE).navigation()
        },
        TextClickItem("Unity") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_UNITY).navigation()
        },
        TextClickItem("VPN") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VPN).navigation()
        },
        TextClickItem("VPN-FlashTunnel") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VPN_FLASH_TUNNEL).navigation()
        },
        TextClickItem("Cleaner") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CLEANER).navigation()
        },
        TextClickItem("短剧-穿山甲") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_DJ_CSJ).navigation()
        },
        TextClickItem("ChargeReward") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CHARGE_REWARD).navigation()
        },

    )
}