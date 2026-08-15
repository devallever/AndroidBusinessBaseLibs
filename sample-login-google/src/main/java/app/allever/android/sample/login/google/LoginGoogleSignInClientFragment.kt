package app.allever.android.sample.login.google

import android.content.Intent
import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import com.chad.library.adapter.base.BaseQuickAdapter

class LoginGoogleSignInClientFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("登录", "") {
            GoogleSignClientHelper.launchSign(this@LoginGoogleSignInClientFragment)
        },
        TextDetailClickItem("退出", "") {
            GoogleSignClientHelper.signOut()
        },
        TextDetailClickItem("检查登录状态", "") {
            toast(GoogleSignClientHelper.checkLogin().toJson())
        },
        TextDetailClickItem("获取用户信息", "") {
            val user = GoogleSignClientHelper.getLoginUser()?.toJson()
            toast(user)
            log("google user = ", user)
        },

    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        GoogleSignClientHelper.handleResult(requestCode, data, object : SignResultCallback {
            override fun onSuccess(googleUserInfo: GoogleUserInfo) {
                toast(googleUserInfo.toJson())
            }

            override fun onError(msg: String) {
                toast(msg)
            }

            override fun onCancel() {
                toast("取消")
            }

        })
        super.onActivityResult(requestCode, resultCode, data, )
    }
}