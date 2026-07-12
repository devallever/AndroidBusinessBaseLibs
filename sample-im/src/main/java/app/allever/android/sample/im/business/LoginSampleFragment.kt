package app.allever.android.sample.im.business

import androidx.lifecycle.lifecycleScope
import android.net.Uri
import android.widget.ImageView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.GsonHelper
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.databinding.ImLoginFragmentBinding
import app.allever.android.sample.im.http.request.AuthRequest
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.UserInfoData
import app.allever.android.sample.im.websocket.SampleWebSocketClientManageFragment
import app.allever.android.sample.im.websocket.client.IMWebSocketClient
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class LoginSampleFragment : BaseFragment<ImLoginFragmentBinding, BaseViewModel>() {
    private val okHttpClient = OkHttpClient.Builder().build()
    private val picturePicker = MediaPickerCore.registerPickerLauncher(this) {
        if (it.isEmpty()) {
            toast("请选择图片")
            return@registerPickerLauncher
        }

        uploadImage(it[0].uri)
    }

    override fun inflate() = ImLoginFragmentBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            Glide.with(this@LoginSampleFragment).load("http://192.168.43.106:8080/api/image/b2063dc8-1cdf-4d30-85ce-8f3e12a9d920.jpg").into(ivPreview)
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
                    }
                }

                btnUploadImage.setOnClickListener {
                    MediaPickerCore.launchImage(picturePicker)
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

    private fun uploadImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(uri)
                if (file == null) {
                    toast("文件转换失败")
                    logE("文件转换失败")
                    return@launch
                }

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.name, file.asRequestBody("image/*".toMediaType()))
                    .build()

                val httpUrl = IMConfig.getHttpBaseUrl()
                if (httpUrl.isEmpty()) {
                    toast("HTTP 服务未启动")
                    logE("HTTP 服务未启动")
                    return@launch
                }

                val request = Request.Builder()
                    .url("$httpUrl/api/image/upload")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()?:""
                    val result = GsonHelper.fromJson(body, UploadResult::class.java)?: return@launch
                    if (result.code == 0 && result.data != null) {
                        val imageUrl = result.data.url
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBinding.tvImageUrl.text = "图片地址: $imageUrl"
                            mBinding.tvImageUrl.visibility = ImageView.VISIBLE
                            toast("上传成功: $imageUrl")
                            log("上传成功: $imageUrl")
                            Glide.with(this@LoginSampleFragment).load(imageUrl).into(mBinding.ivPreview)
                        }
                    } else {
                        toast("上传失败: ${result.msg}")
                        log("上传失败: ${result.msg}")
                    }
                } else {
                    toast("上传失败: ${response.code}")
                    log("上传失败: ${response.code}")
                }
            } catch (e: Exception) {
                toast("上传异常: ${e.message}")
                log("上传异常: ${e.message}")
            }
        }

        lifecycleScope.launch {
//            NetCore.post("/api/image/upload", "")
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use {
                val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { outputStream ->
                    it.copyTo(outputStream)
                }
                tempFile
            }
        } catch (e: Exception) {
            null
        }
    }

    private data class UploadResult(
        val code: Int,
        val msg: String,
        val data: UploadImageData?
    )

    private data class UploadImageData(
        val url: String,
        val filename: String
    )
}