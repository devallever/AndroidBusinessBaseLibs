package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding

class BasicFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter() = TextDetailClickAdapter(Gravity.CENTER)
    override fun getList() = mutableListOf(
        TextDetailClickItem("Mvvm", "sample-mvvm") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_MVVM)
        },
        TextDetailClickItem("权限", "sample-permission") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_PERMISSION)
        },
        TextDetailClickItem("网络", "sample-network-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_NETWORK_CORE)
        },
        TextDetailClickItem("存储", "sample-store-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_STORE_CORE)
        },
        TextDetailClickItem("图片加载", "sample-imageloader-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_IMAGE_LOADER_CORE)
        },
        TextDetailClickItem("媒体/媒体选择器", "sample-media-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_MEDIA)
        },
        TextDetailClickItem("音视频播放器", "sample-audiovideo") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AUDIO_VIDEO)
        },
        TextDetailClickItem("相机", "sample-camera-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_CAMERA_CORE)
        },
    )
}