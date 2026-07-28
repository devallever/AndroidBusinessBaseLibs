package com.allever.business.lib.project

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class MainTabFragment: TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> = mutableListOf("示例代码", "项目代码", "基础组件", "示例代码(旧)", "Github", "Company")

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        SampleListFragment(),
        AppListFragment(),
        LibListFragment(),
        SampleOldListFragment(),
        GithubListFragment(),
        CompanyListFragment()
    )
}