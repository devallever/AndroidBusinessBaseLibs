package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter

class SampleListFragment : ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    override fun getAdapter() = TextDetailClickAdapter(Gravity.CENTER)

    override fun getList() = mutableListOf(
        TextDetailClickItem("基础组件示例代码") {
            FragmentActivity.start<BasicFragment>(it.title)
        },
        TextDetailClickItem("广告组件", "sample-ad-core") {
            FragmentActivity.start<AdFragment>(it.title)
        },
        TextDetailClickItem("归因-Referrer", "sample-adjust/sample-appsflyer") {
            FragmentActivity.start<ReferrerFragment>(it.title)
        },
        TextDetailClickItem("音视频", "sample-audiovideo") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AUDIO_VIDEO).navigation()
        },
        TextDetailClickItem("Unity", "sample-unity") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_UNITY).navigation()
        },
        TextDetailClickItem("VPN", "sample-vpn") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VPN).navigation()
        },
        TextDetailClickItem("VPN-FlashTunnel", "sample-vpn-flashtunnel") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VPN_FLASH_TUNNEL).navigation()
        },
        TextDetailClickItem("清理", "sample-cleaner") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CLEANER).navigation()
        },
        TextDetailClickItem("清理-Wood", "sample-cleaner-wood") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CLEANER_WOOD).navigation()
        },
        TextDetailClickItem("短剧-穿山甲", "sample-djcsj") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_DJ_CSJ).navigation()
        },
        TextDetailClickItem("网赚-ChargeReward", "sample-chargereward") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CHARGE_REWARD).navigation()
        },
        TextDetailClickItem("网赚-记录步数", "sample-step-tool") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_STEP_TOOL).navigation()
        },
        TextDetailClickItem("多进程通信-IPC", "sample-ipc") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_IPC).navigation()
        },
        TextDetailClickItem("网络测速", "sample-net-speed-test") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_NET_SPEED_TEST).navigation()
        },
        TextDetailClickItem("视频编辑", "sample-video-editor") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VIDEO_EDITOR).navigation()
        },
        TextDetailClickItem("私密相册", "sample-app-secret-album") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_SECRET_ALBUM).navigation()
        },
    )
}