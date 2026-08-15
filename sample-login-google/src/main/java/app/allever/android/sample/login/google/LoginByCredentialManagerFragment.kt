package app.allever.android.sample.login.google

import android.os.Build
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.launch

/***
 * 通过CredentialManager实现Google登录
 * https://codelabs.developers.google.com/sign-in-with-google-android?hl=zh-cn#0
 * 
 */
class LoginByCredentialManagerFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(

        TextDetailClickItem("按钮登录") {
            lifecycleScope.launch {
                GoogleCredentialHelper.launchSign(object : SignResultCallback {
                    override fun onSuccess(googleUserInfo: GoogleUserInfo) {
                        toast("登录成功: ${googleUserInfo.toJson()}")
                    }

                    override fun onError(msg: String) {
                        toast(msg)
                    }

                    override fun onCancel() {
                        toast("取消登录")
                    }
                })
            }
        },
        TextDetailClickItem("弹窗登录", "待完善"),

        )
}