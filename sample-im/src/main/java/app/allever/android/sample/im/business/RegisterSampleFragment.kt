package app.allever.android.sample.im.business

import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.GsonHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.IMGlobal
import app.allever.android.sample.im.databinding.ImRegisterFragmentBinding
import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.request.AuthRequest
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.UserInfoData
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RegisterSampleFragment : BaseFragment<ImRegisterFragmentBinding, BaseViewModel>() {
    override fun inflate() = ImRegisterFragmentBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            etUsername.setText(IMConfig.getLoginUser())
            btnRegister.setOnClickListener {
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
//                lifecycleScope.launch(Dispatchers.IO) {
//                    try {
//                        // 1. 构建 JSON 请求体
//                        val requestBody = AuthRequest(username, password)
//                        val jsonStr = GsonHelper.toJson(requestBody)
//                        val mediaType = "application/json; charset=utf-8".toMediaType()
//                        val body = jsonStr.toRequestBody(mediaType)
//
//                        // 2. 构建请求
//                        val request = Request.Builder()
//                            .url("${IMConfig.getHttpBaseUrl()}/api/user/register")
//                            .post(body)
//                            .build()
//
//                        // 3. 执行请求
//                        val response = IMGlobal.okHttpClient.newCall(request).execute()
//
//                        // 4. 解析响应（body.string() 只能调用一次）
//                        val responseStr = response.body?.string() ?: return@launch
//                        val type = object : TypeToken<BaseResponse<UserInfoData>>() {}.type
//                        val baseResponse = GsonHelper.getGson().fromJson<BaseResponse<UserInfoData>>(responseStr, type)
//                        if (baseResponse.isSuccess() && baseResponse.data != null) {
//                            toast("okHttp注册成功")
//                        } else {
//                            toast("okHttp注册失败")
//                        }
//
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        null
//                    }
//                }
                lifecycleScope.launch {
                    val response = NetCore.post<BaseResponse<UserInfoData>>(
                        API.REGISTER,
                        AuthRequest(username, password)
                    )
                    if (response.isSuccess() && response.data != null) {
                        toast("注册成功")
                    } else {
                        toast("注册失败: ${response.msg}")
                    }
                }
            }
        }
    }
}