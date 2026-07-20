package com.allever.business.lib.project

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.therouter.TheRouter
import com.chad.library.adapter.base.BaseQuickAdapter

class SampleOldListFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("谷歌内购订阅支付") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_BILLING).navigation()
        },
        TextClickItem("清理工具(Demo)") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_CLEANER).navigation()
        },
        TextClickItem("设计模式") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_DESIGN_PATTERN).navigation()
        },
        TextClickItem("功能实现") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_FUNCTION).navigation()
        },
        TextClickItem("Jetpack") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_JETPACK).navigation()
        },
        TextClickItem("JNI") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_JNI).navigation()
        },
        TextClickItem("JNI-MK") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_JNI_MK).navigation()
        },
        TextClickItem("Kotlin") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_KOTLIN).navigation()
        },
        TextClickItem("LearningAndroid") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_LEARNING_ANDROID).navigation()
        },
        TextClickItem("登录") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_LOGIN).navigation()
        },
        TextClickItem("MaterialDesign") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_MATERIAL_DESIGN).navigation()
        },
        TextClickItem("MicrosoftSpeech") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_MICROSOFT_SPEECH).navigation()
        },
        TextClickItem("Safe") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_SAFE).navigation()
        },
        TextClickItem("ThirtyPart") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_THIRTY_PART).navigation()
        },
        TextClickItem("Toolbox") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_TOOLBOX).navigation()
        },
        TextClickItem("UI") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_UI).navigation()
        },
        TextClickItem("VideoEditor") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_VIDEO_EDITOR).navigation()
        },
        TextClickItem("音视频") {
            TheRouter.build(RouterPath.PATH_Z_SAMPLE_AUDIO_VIDEO).navigation()
        },
    )
}