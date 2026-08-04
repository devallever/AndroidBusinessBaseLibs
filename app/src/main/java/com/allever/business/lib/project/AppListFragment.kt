package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class AppListFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("TextCard", "sample-app-text-card") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_TEXT_CARD)
        },
        TextDetailClickItem("Sticker Icon", "sample-app-sticker-icon") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_STICKER_ICON)
        },
        TextDetailClickItem("文本翻译器", "sample-app-text-translator") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_TEXT_TRANSLATOR)
        },
        TextDetailClickItem("30天减肥", "sample-app-lose-weight") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_LOSE_WEIGHT)
        },
        TextDetailClickItem("倒数日", "sample-app-day-matter") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_DAY_MATTER)
        },
        TextDetailClickItem("虚拟来电", "sample-app-virtual-call") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_VIRTUAL_CALL)
        },
        TextDetailClickItem("Gif图搜索", "sample-app-gif-search") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_GIF_SEARCH)
        },
        TextDetailClickItem("贴纸相机", "sample-app-sticker-camera") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_STICKER_CAMERA)
        },
        TextDetailClickItem("隐私相机", "sample-app-syp-camera\n(部分机型后台相机运行中报错)") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_SPY_CAMERA)
        },
        TextDetailClickItem("私密相册", "sample-app-secret-album") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_SECRET_ALBUM)
        },
        TextDetailClickItem("网络测速", "sample-app-net-speed-test") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_NET_SPEED_TEST)
        },
        TextDetailClickItem("清理-Wood", "sample-app-cleaner-wood") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_CLEANER_WOOD)
        },
        TextDetailClickItem("网赚-ChargeReward", "sample-app-chargereward") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_CHARGE_REWARD)
        },
        TextDetailClickItem("网赚-记录步数", "sample-app-step-tool") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_STEP_TOOL)
        },
        TextDetailClickItem("点餐(内部)", "sample-app-hd-calculator") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_HD_CALCULATOR)
        },
        TextDetailClickItem("VPN-FlashTunnel", "sample-app-vpn-flashtunnel") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_VPN_FLASH_TUNNEL)
        },
        TextDetailClickItem("Lucky Spin", "sample-app-lucky-spin") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_LUCKY_SPIN)
        },
        TextDetailClickItem("视频编辑", "sample-app-video-editor") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_VIDEO_EDITOR)
        },
        TextDetailClickItem("二维码", "sample-app-qr-code") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_QR_CODE)
        },
        TextDetailClickItem("FFMpegCommand", "sample-app-ffmpeg-command") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APP_FFMPEG_COMMAND)
        },
        TextDetailClickItem("短剧-穿山甲", "sample-djcsj") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_DJ_CSJ)
        },
    )
}