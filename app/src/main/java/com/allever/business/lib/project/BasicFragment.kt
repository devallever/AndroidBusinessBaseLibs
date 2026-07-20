package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.therouter.TheRouter

class BasicFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter() = TextDetailClickAdapter(Gravity.CENTER)
    override fun getList() = mutableListOf(
        TextDetailClickItem("Mvvm", "sample-mvvm") {
            TheRouter.build(RouterPath.PATH_SAMPLE_MVVM).navigation()
        },
        TextDetailClickItem("权限", "sample-permission") {
            TheRouter.build(RouterPath.PATH_SAMPLE_PERMISSION).navigation()
        },
        TextDetailClickItem("网络", "sample-network-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_NETWORK_CORE).navigation()
        },
        TextDetailClickItem("存储", "sample-store-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_STORE_CORE).navigation()
        },
        TextDetailClickItem("图片加载", "sample-imageloader-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_IMAGE_LOADER_CORE).navigation()
        },
        TextDetailClickItem("媒体/媒体选择器", "sample-media-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_MEDIA).navigation()
        },
        TextDetailClickItem("音视频播放器", "sample-audiovideo") {
            TheRouter.build(RouterPath.PATH_SAMPLE_AUDIO_VIDEO).navigation()
        },
        TextDetailClickItem("相机", "sample-camera-core") {
            TheRouter.build(RouterPath.PATH_SAMPLE_CAMERA_CORE).navigation()
        },
    )
}