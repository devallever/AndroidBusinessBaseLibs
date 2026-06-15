package app.allever.android.sample.dj.csj

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabActivity
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.ActivityTabBinding

class CsjMainActivity : TabActivity<ActivityTabBinding, TabViewModel>() {
    override fun getPageTitle(): String = "短剧-穿山甲"

    override fun getTabTitles(): MutableList<String> = mutableListOf("剧单", "推荐")

    override fun getFragments(): MutableList<Fragment> =
        mutableListOf(PlayListFragment(), RecommendFragment())
}