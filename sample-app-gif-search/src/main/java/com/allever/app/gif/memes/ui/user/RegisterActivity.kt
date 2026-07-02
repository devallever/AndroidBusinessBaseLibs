package com.allever.app.gif.memes.ui.user

import android.content.Context
import android.os.Bundle
import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseDataActivity2
import com.allever.app.gif.memes.databinding.ActivityRegisterBinding
import com.allever.app.gif.memes.ui.user.model.RegisterViewModel
import com.xm.lib.base.config.DataBindingConfig
import com.xm.lib.manager.IntentManager

class RegisterActivity: BaseDataActivity2<ActivityRegisterBinding, RegisterViewModel>() {

    companion object {
        private const val EXTRA_PHONE = "EXTRA_PHONE"
        private const val EXTRA_V_CODE = "EXTRA_V_CODE"
        fun start(context: Context, phone: String, vCode: String) {
            val bundle  = Bundle()
            bundle.putString(EXTRA_PHONE, phone)
            bundle.putString(EXTRA_V_CODE, vCode)
            IntentManager.startActivity(context, RegisterActivity::class.java, bundle)
        }
    }

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_register, BR.registerViewModel)

    override fun initDataAndEvent() {
        mViewModel.phone = intent.getStringExtra(EXTRA_PHONE)?:""
        mViewModel.vCode = intent.getStringExtra(EXTRA_V_CODE)?:""
        mBinding.includeTapBar.tvLabel.text = getStringRes(R.string.register)
        mBinding.includeTapBar.ivLeft.setOnClickListener {
            finish()
        }
    }

    override fun destroyView() {
    }

}