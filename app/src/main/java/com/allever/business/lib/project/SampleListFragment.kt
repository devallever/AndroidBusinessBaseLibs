package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.alibaba.android.arouter.launcher.ARouter

class SampleListFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf(
        TextClickItem("AppsFlyer") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_APPS_FLYER).navigation()
        },
        TextClickItem("Adjust") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_ADJUST).navigation()
        },
        TextClickItem("Mvvm") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_MVVM).navigation()
        },
        TextClickItem("AdMob") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AD_ADMOB).navigation()
        },
        TextClickItem("Pangle") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AD_PANGLE).navigation()
        },
        TextClickItem("Bigo") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AD_BIGO).navigation()
        },
        TextClickItem("AppLovin") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AD_APPLOVIN).navigation()
        },
        TextClickItem("AdCore") {
//            ActivityHelper.startActivity<AdProviderExampleActivity> {  }
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AD_CORE).navigation()
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
        TextClickItem("音视频") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_AUDIO_VIDEO).navigation()
        },
        TextClickItem("存储") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_STORE_CORE).navigation()
        },
        TextClickItem("图片加载") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_IMAGE_LOADER_CORE).navigation()
        },
        TextClickItem("CameraCore") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_CAMERA_CORE).navigation()
        },
        TextClickItem("Unity") {
            ARouter.getInstance().build(RouterPath.PATH_SAMPLE_UNITY).navigation()
        },
    )
}