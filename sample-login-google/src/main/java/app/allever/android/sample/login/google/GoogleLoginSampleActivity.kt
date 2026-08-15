package app.allever.android.sample.login.google

import android.os.Build
import android.view.Gravity
import androidx.annotation.RequiresApi
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/logingoogle/main")
class GoogleLoginSampleActivity :
    ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "Google 登录"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("Credential Manager") {
            FragmentActivity.start<LoginByCredentialManagerFragment>(it.title)
        },
        TextDetailClickItem("GoogleSignInClient-旧方式") {
            FragmentActivity.start<LoginGoogleSignInClientFragment>(it.title)
        },
    )
}