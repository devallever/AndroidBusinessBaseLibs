package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding

class LibListFragment : ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    override fun getAdapter() = TextDetailClickAdapter(Gravity.CENTER)

    override fun getList() = mutableListOf(
        TextDetailClickItem("MVVM", "lib-mvvm") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_MVVM)
        },
        TextDetailClickItem("广告", "lib-ad-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_CORE)
        },
        TextDetailClickItem("媒体", "lib-media-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_MEDIA)
        },
        TextDetailClickItem("媒体选择器", "lib-media-picker") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_MEDIA)
        },
        TextDetailClickItem("网络", "lib-network-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_NETWORK_CORE)
        },
        TextDetailClickItem("播放器", "lib-player-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_PLAYER_CORE)
        },
        TextDetailClickItem("图片加载", "lib-image-loader-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_IMAGE_LOADER_CORE)
        },
        TextDetailClickItem("存储", "lib-core.store") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_STORE_CORE)
        },
        TextDetailClickItem("相机", "lib-core.camera") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_CAMERA_CORE)
        },
        TextDetailClickItem("权限", "lib-core.permission") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_PERMISSION)
        },
        TextDetailClickItem("VPN-Shadowsocks", "lib-vpn-shadowsocks-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_VPN)
        },
    )
}