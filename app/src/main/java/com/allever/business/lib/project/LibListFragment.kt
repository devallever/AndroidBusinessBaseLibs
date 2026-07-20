package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.therouter.TheRouter

class LibListFragment : ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    override fun getAdapter() = TextDetailClickAdapter(Gravity.CENTER)

    override fun getList() = mutableListOf(
        TextDetailClickItem("MVVM", "lib-mvvm") {
            TheRouter.build(RouterPath.PATH_SAMPLE_MVVM).navigation()
        },
        TextDetailClickItem("广告", "lib-ad-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AD_CORE).navigation()
        },
        TextDetailClickItem("媒体", "lib-media-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_MEDIA).navigation()
        },
        TextDetailClickItem("媒体选择器", "lib-media-picker") {
            TheRouter.build(RouterPath.PATH_SAMPLE_MEDIA).navigation()
        },
        TextDetailClickItem("网络", "lib-network-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_NETWORK_CORE).navigation()
        },
        TextDetailClickItem("播放器", "lib-player-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AUDIO_VIDEO).navigation()
        },
        TextDetailClickItem("图片加载", "lib-image-loader-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_IMAGE_LOADER_CORE).navigation()
        },
        TextDetailClickItem("存储", "lib-core.store") {
            TheRouter.build(RouterPath.PATH_SAMPLE_STORE_CORE).navigation()
        },
        TextDetailClickItem("相机", "lib-core.camera") {
            TheRouter.build(RouterPath.PATH_SAMPLE_CAMERA_CORE).navigation()
        },
        TextDetailClickItem("权限", "lib-core.permission") {
            TheRouter.build(RouterPath.PATH_SAMPLE_PERMISSION).navigation()
        },
        TextDetailClickItem("VPN-Shadowsocks", "lib-vpn-shadowsocks-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_VPN).navigation()
        },
    )
}