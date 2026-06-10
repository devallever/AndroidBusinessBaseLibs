package z.app.allever.android.sample.jni.mk

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class JniMkMainFragment : TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles() = mutableListOf(
        "Android.mk", "Application.mk","Build"
    )

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        AndroidMkFragment(),
        ApplicationMkFragment(),
        BuildMkFragment()
    )
}