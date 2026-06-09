package com.allever.business.lib.project

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class MainTabFragment: TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> = mutableListOf("Sample", "Sample-Old", "Lib")

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        SampleListFragment(),
        SampleOldListFragment(),
        LibListFragment()
    )
}