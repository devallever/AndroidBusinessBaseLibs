package app.allever.android.sample.ipc

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabActivity
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.ActivityTabBinding
import app.allever.android.lib.router.annotation.Route

@Route(path = "/ipc/main")
class IPCSampleActivity : TabActivity<ActivityTabBinding, TabViewModel>() {
    override fun getPageTitle(): String = "IPC"

    override fun getTabTitles(): MutableList<String> = mutableListOf("Binder", "AIDL", "Messenger")

    override fun getFragments(): MutableList<Fragment> {
        return mutableListOf(
            IPCBinderFragment(), IPCAIDLFragment(), IPCMessengerFragment()
        )
    }
}