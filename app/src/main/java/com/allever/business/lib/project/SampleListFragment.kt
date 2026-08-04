package com.allever.business.lib.project

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding

class SampleListFragment : ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    override fun getAdapter() = TextDetailClickAdapter(Gravity.CENTER)

    override fun getList() = mutableListOf(
        TextDetailClickItem("Compose Project", "app-compose") {
            Navi.navigateTo(RouterPath.PATH_APP_COMPOSE)
        },
        TextDetailClickItem("基础组件") {
            FragmentActivity.start<LibListFragment>(it.title)
        },
        TextDetailClickItem("项目代码") {
            FragmentActivity.start<AppListFragment>(it.title)
        },
        TextDetailClickItem("示例代码(旧)") {
            FragmentActivity.start<SampleOldListFragment>(it.title)
        },
        TextDetailClickItem("Github") {
            FragmentActivity.start<GithubListFragment>(it.title)
        },
        TextDetailClickItem("Demo") {
            FragmentActivity.start<DemoListFragment>(it.title)
        },
        TextDetailClickItem("广告组件", "sample-ad-core") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AD_CORE)
        },
        TextDetailClickItem("音视频", "sample-audiovideo") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_AUDIO_VIDEO)
        },
        TextDetailClickItem("IM", "sample-im") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_IM)
        },
        TextDetailClickItem("----------------------"),
        TextDetailClickItem("归因-Referrer", "sample-adjust/sample-appsflyer") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_APPS_FLYER)
        },
        TextDetailClickItem("Unity", "sample-unity") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_UNITY)
        },
        TextDetailClickItem("VPN", "sample-vpn") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_VPN)
        },
        TextDetailClickItem("清理", "sample-cleaner") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_CLEANER)
        },
        TextDetailClickItem("多进程通信-IPC", "sample-ipc") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_IPC)
        },
        TextDetailClickItem("蓝牙", "sample-bluetooth") {
            Navi.navigateTo(RouterPath.PATH_SAMPLE_BLUETOOTH)
        },
    )
}