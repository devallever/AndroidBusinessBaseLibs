package z.app.allever.android.sample.function.im

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding
import z.app.allever.android.sample.function.im.ui.ContactsListFragment
import z.app.allever.android.sample.function.im.ui.ConversationListFragment
import z.app.allever.android.sample.function.im.ui.UserManageFragment

class IMMainFragment : TabFragment<FragmentTabBinding, TabViewModel>() {

    override fun getTabTitles() = mutableListOf("会话列表", "联系人", "用户管理")

    override fun getFragments(): MutableList<Fragment> =
        mutableListOf(ConversationListFragment(), ContactsListFragment(), UserManageFragment())
}