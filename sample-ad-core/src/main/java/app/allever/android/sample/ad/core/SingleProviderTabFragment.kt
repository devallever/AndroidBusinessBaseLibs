package app.allever.android.sample.ad.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class SingleProviderTabFragment : TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> =
        mutableListOf("AdMob", "Pangle", "Bigo", "MultiProvider")

    override fun getFragments(): MutableList<Fragment> =
        mutableListOf(AdMobFragment(), PangleFragment(), BigoFragment(),MultiProviderFragment())
}