package app.allever.android.sample.permission

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class PermissionTabFragment : TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> =
        mutableListOf("BasePermission", "PermissionComponent")

    override fun getFragments(): MutableList<Fragment> =
        mutableListOf(PermissionBaseSampleFragment(), PermissionComponentSampleFragment())
}