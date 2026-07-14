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
        TextDetailClickItem("TextCard", "sample-app-text-card") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_TEXT_CARD).navigation()
        },
        TextDetailClickItem("文本翻译器", "sample-app-text-translator") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_TEXT_TRANSLATOR).navigation()
        },
        TextDetailClickItem("FFMpegCommand", "sample-app-ffmpeg-command") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_FFMPEG_COMMAND).navigation()
        },
        TextDetailClickItem("30天减肥", "sample-app-lose-weight") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_LOSE_WEIGHT).navigation()
        },
        TextDetailClickItem("倒数日", "sample-app-day-matter") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_DAY_MATTER).navigation()
        },
        TextDetailClickItem("虚拟来电", "sample-app-virtual-call") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_VIRTUAL_CALL).navigation()
        },
        TextDetailClickItem("Gif图搜索", "sample-app-gif-search") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_GIF_SEARCH).navigation()
        },
        TextDetailClickItem("贴纸相机", "sample-app-sticker-camera") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_STICKER_CAMERA).navigation()
        },
        TextDetailClickItem("隐私相机", "sample-app-syp-camera") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APP_SPY_CAMERA).navigation()
        },
        TextDetailClickItem("私密相册", "sample-app-secret-album") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_SECRET_ALBUM).navigation()
        },
        TextDetailClickItem("视频编辑", "sample-app-video-editor") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VIDEO_EDITOR).navigation()
        },
        TextDetailClickItem("网络测速", "sample-app-net-speed-test") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_NET_SPEED_TEST).navigation()
        },

        TextDetailClickItem("清理-Wood", "sample-app-cleaner-wood") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CLEANER_WOOD).navigation()
        },
        TextDetailClickItem("网赚-ChargeReward", "sample-app-chargereward") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CHARGE_REWARD).navigation()
        },
        TextDetailClickItem("网赚-记录步数", "sample-app-step-tool") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_STEP_TOOL).navigation()
        },
        TextDetailClickItem("短剧-穿山甲", "sample-djcsj") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_DJ_CSJ).navigation()
        },
        TextDetailClickItem("VPN-FlashTunnel", "sample-app-vpn-flashtunnel") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_VPN_FLASH_TUNNEL).navigation()
        },
    )
}