package app.allever.android.sample.im.business

import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.GsonHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.IMGlobal
import app.allever.android.sample.im.databinding.ImLoginFragmentBinding
import app.allever.android.sample.im.http.request.AuthRequest
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.UserInfoData
import app.allever.android.sample.im.websocket.SampleWebSocketClientManageFragment
import app.allever.android.sample.im.websocket.client.IMWebSocketClient
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LoginSampleFragment : BaseFragment<ImLoginFragmentBinding, BaseViewModel>() {
    override fun inflate() = ImLoginFragmentBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            btnLogin.setOnClickListener {
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()
                if (username.isEmpty()) {
                    toast("请输入用户名")
                    return@setOnClickListener
                }
                if (password.isEmpty()) {
                    toast("请输入密码")
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    val response = NetCore.post<BaseResponse<UserInfoData>>(
                        "/api/user/login",
                        AuthRequest(username, password)
                    )
                    if (response.isSuccess() && response.data != null) {
                        toast("登录成功")
                        IMConfig.saveUser(response.data.username)
                        IMWebSocketClient.connect(IMConfig.getConnectWebsocketUrl(response.data.username))
                    } else {
                        toast("登录失败: ${response.msg}")
                    }

                    updateLoginStatus()
                }
            }

            btnLogout.setOnClickListener {
                lifecycleScope.launch {
                    val response = NetCore.post<BaseResponse<Any>>(
                        "/api/user/logout", AuthRequest(username = IMConfig.getLoginUser()))
                    if (response.isSuccess()) {
                        toast("退出成功")
                        IMConfig.saveUser("")
                        IMWebSocketClient.disconnect()
                    } else {
                        toast("退出失败：${response.msg}")
                    }
                    updateLoginStatus()
                }
            }

            btnEnterChatroom.setOnClickListener {
                FragmentActivity.start<SampleWebSocketClientManageFragment>("聊天室")
            }

            btnOnlineUserList.setOnClickListener {
                lifecycleScope.launch {
                    FragmentActivity.start<OnlineUserListFragment>("用户列表")
//                    val response = NetCore.get<BaseResponse<List<UserInfoData>>>("/api/user/onlineList")
//                    if (response.isSuccess() && response.data != null) {
//                        toast("获取在线用户列表成功: ${response.data.toJson()}")
//                    } else {
//                        toast("获取在线用户列表失败：${response.msg}")
//                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.etUsername.setText(IMConfig.getLoginUser())
        updateLoginStatus()
    }

    private fun updateLoginStatus() {
        lifecycleScope.launch(Dispatchers.Main) {
            val isLogin = IMConfig.isLogin()
            mBinding.apply {
                btnLogin.isEnabled = !isLogin
                btnLogout.isEnabled = isLogin
                btnEnterChatroom.isEnabled = isLogin
            }
        }
    }
}