package app.allever.android.sample.imageloader.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding


class SampleImageLoaderMainFragment : TabFragment<FragmentTabBinding, TabViewModel>() {

    override fun getTabTitles(): MutableList<String> = mutableListOf(
        "基础加载",
    )

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        BasicImageLoaderFragment(),
    )
}
