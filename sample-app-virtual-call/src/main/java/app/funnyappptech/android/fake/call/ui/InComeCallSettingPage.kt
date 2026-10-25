package app.funnyappptech.android.fake.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.databinding.VcActivityInComeCallSettingBinding
import app.funnyappptech.android.fake.call.function.SettingStore

class InComeCallSettingPage : BasePage<VcActivityInComeCallSettingBinding, BaseViewModel>(),
     View.OnClickListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
        }
    }

    override fun onDestroy() {
        SettingStore.setTime(mBinding.settingItemEtTime.text.toString().toIntOrNull() ?: 5)
        SettingStore.setPhone(mBinding.settingItemEtPhone.text.toString())
        SettingStore.setContact(mBinding.settingItemEtContact.text.toString())
        SettingStore.setLocal(mBinding.settingItemEtLocal.text.toString())
        super.onDestroy()
    }

    override fun inflate(): VcActivityInComeCallSettingBinding {
        return VcActivityInComeCallSettingBinding.inflate(layoutInflater)
    }

    override fun init() {
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_setting_in_come_call)
        findViewById<View>(R.id.iv_left).setOnClickListener(this)

        mBinding.settingItemEtTime.setText(SettingStore.getTime().toString())
        mBinding.settingItemEtPhone.setText(SettingStore.getPhone())
        mBinding.settingItemEtContact.setText(SettingStore.getContact())
        mBinding.settingItemEtLocal.setText(SettingStore.getLocal())
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, InComeCallSettingPage::class.java)
            context.startActivity(intent)
        }
    }
}