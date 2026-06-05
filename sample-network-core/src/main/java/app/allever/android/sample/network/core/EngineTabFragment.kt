package app.allever.android.sample.network.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class EngineTabFragment: TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> = mutableListOf("HttpUrlConnectionEngine", "OkhttpEngine")

    override fun getFragments(): MutableList<Fragment> = mutableListOf(HttpUrlConnectionEngineFragment(), OkhttpEngineFragment())
}