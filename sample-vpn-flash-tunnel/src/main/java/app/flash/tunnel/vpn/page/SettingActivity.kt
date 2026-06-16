package app.flash.tunnel.vpn.page

import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.databinding.ActivitySettingBinding
import app.flash.tunnel.vpn.lib.common.util.ActivityManager
import app.flash.tunnel.vpn.page.dialog.DialogHelper

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun inflate() = ActivitySettingBinding.inflate(layoutInflater)

    override fun init() {
        fixStatusBar(mBinding.topBar)
        mBinding.ivClose.setOnClickListener { finish() }

        mBinding.tvVersion.text = "v${BuildConfig.VERSION_NAME}"

        mBinding.btnFaq.setOnClickListener {
            ActivityManager.start(this@SettingActivity, QuestionActivity::class.java)
        }
        mBinding.btnPrivacy.setOnClickListener {
            WebViewActivity.start(this@SettingActivity, Constants.PRIVACY_URL, "Privacy")
        }
        mBinding.btnRateUs.setOnClickListener {
            DialogHelper.obtainRateDialog(this@SettingActivity).show()
        }
    }
}