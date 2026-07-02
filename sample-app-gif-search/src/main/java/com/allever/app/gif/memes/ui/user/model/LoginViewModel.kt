package com.allever.app.gif.memes.ui.user.model

import android.os.CountDownTimer
import android.text.TextUtils
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allever.app.gif.memes.R
import com.funny.gif.memes.func.network.NetRepository
import com.funny.gif.memes.func.store.Store
import com.allever.app.gif.memes.ui.user.RegisterActivity
import com.allever.lib.common.util.getString
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.util.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModelKt<IBaseView>() {
    val phone = MediatorLiveData<String>()
    val vCode = MutableLiveData<String>()
    val btnVCodeText = MediatorLiveData<String>()
    val btnVCodeClickable = MediatorLiveData<Boolean>()

    private val mCountTimer by lazy {
        object : CountDownTimer(60 * 1000L, 1000L) {
            override fun onTick(left: Long) {
                btnVCodeText.value = "剩余 ${left / 1000} 秒"
            }

            override fun onFinish() {
                btnVCodeText.value = getString(R.string.fetchVCode)
                btnVCodeClickable.value = true
            }
        }
    }

    override fun onCreated() {
        btnVCodeClickable.value = true
        btnVCodeText.value = getString(R.string.fetchVCode)
    }

    override fun destroyEvent() {
        super.destroyEvent()
        mCountTimer.onFinish()
    }

    fun onClickFetchVCode() {
        if (TextUtils.isEmpty(phone.value)) {
            toast("输入手机号")
            return
        }

        if (phone.value?.length ?: 0 < 11) {
            toast("请输入正确手机号")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {

            phone.value?.let {
                val response = NetRepository.fetchVCode(it) {
                    toast(it)
                    loge(it)
                }
                response.data?.let {
                    mCountTimer.start()
                    btnVCodeClickable.value = false
                }
            }
        }
    }

    fun onClickLogin() {
        if (TextUtils.isEmpty(phone.value)) {
            toast("输入手机号")
            return
        }

        if (TextUtils.isEmpty(vCode.value)) {
            toast("输入验证码")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            val response = NetRepository.login(phone.value!!, vCode.value!!) {
                toast(it)
                log(it)
            }
            response.data?.let {
                Store.saveToken(it.token)
                Store.saveUserId(it.userId.toInt())
                Store.savePhone(phone.value!!)
                finish()
            }

            if (response.errorCode == 10101) {
                RegisterActivity.start(mCxt, phone.value!!, vCode.value!!)
            }
        }
    }
}