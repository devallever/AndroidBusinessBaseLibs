package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class SampleOldListFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("谷歌内购订阅支付") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_BILLING)
        },
        TextClickItem("清理工具(Demo)") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_CLEANER)
        },
        TextClickItem("设计模式") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_DESIGN_PATTERN)
        },
        TextClickItem("功能实现") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_FUNCTION)
        },
        TextClickItem("Jetpack") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_JETPACK)
        },
        TextClickItem("JNI") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_JNI)
        },
        TextClickItem("JNI-MK") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_JNI_MK)
        },
        TextClickItem("Kotlin") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_KOTLIN)
        },
        TextClickItem("LearningAndroid") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_LEARNING_ANDROID)
        },
        TextClickItem("登录") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_LOGIN)
        },
        TextClickItem("MaterialDesign") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_MATERIAL_DESIGN)
        },
        TextClickItem("MicrosoftSpeech") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_MICROSOFT_SPEECH)
        },
        TextClickItem("Safe") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_SAFE)
        },
        TextClickItem("ThirtyPart") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_THIRTY_PART)
        },
        TextClickItem("Toolbox") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_TOOLBOX)
        },
        TextClickItem("UI") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_UI)
        },
        TextClickItem("VideoEditor") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_VIDEO_EDITOR)
        },
        TextClickItem("音视频") {
            Navi.navigateTo(RouterPath.PATH_Z_SAMPLE_AUDIO_VIDEO)
        },
    )
}