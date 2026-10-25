package app.funnyappptech.android.fake.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import app.allever.android.lib.core.app.App
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.databinding.VcActivityAboutBinding
import app.funnyappptech.android.fake.call.util.SystemUtils

class AboutPage : BasePage<VcActivityAboutBinding, BaseViewModel>(), View.OnClickListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.about_privacy -> {
                val privacyUrl =
                    "https://www.hadsky.com/privacy_policy.html?chkcsrfval=2371ca600d787f8e4e68aaf691d0d56c&name=%E8%99%9A%E6%8B%9F%E6%9D%A5%E7%94%B5&alias=%E8%99%9A%E6%8B%9F%E6%9D%A5%E7%94%B5"
                SystemUtils.startWebView(App.context, privacyUrl)
            }
        }
    }

    override fun inflate(): VcActivityAboutBinding {
        return VcActivityAboutBinding.inflate(layoutInflater)
    }

    override fun init() {
        mBinding.aboutPrivacy.setOnClickListener(this)
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_about)
        mBinding.aboutAppVersion.text = "v1.0"
        mBinding.aboutRight.text =
            String.format(getString(R.string.vc_about_right), getString(R.string.vc_app_name))
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AboutPage::class.java)
            context.startActivity(intent)
        }
    }
}