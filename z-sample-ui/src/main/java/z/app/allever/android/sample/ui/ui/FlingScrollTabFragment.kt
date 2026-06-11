package z.app.allever.android.sample.ui.ui

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding
import z.app.allever.android.sample.ui.ui.rebound.ReboundScrollViewFragment
import z.app.allever.android.sample.ui.ui.rebound.ReboundViewDragHelperFragment
import z.app.allever.android.sample.ui.ui.rebound.SmartRefreshReboundFragment

class FlingScrollTabFragment : TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> =
        mutableListOf("ScrollView方式", "ViewDragHelper方式", "SmartRefreshLayout方式")

    override fun getFragments(): MutableList<Fragment> =
        mutableListOf(
            ReboundScrollViewFragment(),
            ReboundViewDragHelperFragment(),
            SmartRefreshReboundFragment()
        )
}