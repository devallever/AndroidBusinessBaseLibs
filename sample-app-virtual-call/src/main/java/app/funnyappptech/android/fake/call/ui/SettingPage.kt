package app.funnyappptech.android.fake.call.ui

import android.view.View
import app.funnyappptech.android.fake.call.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.FeedbackHelper
import app.allever.android.lib.core.helper.ShareHelper
import app.allever.android.lib.core.permission.internal.PermissionUtil
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.databinding.VcFragmentSettingBinding

class SettingPage : BasePage<VcFragmentSettingBinding, BaseViewModel>(),
    View.OnClickListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_permission -> {
                PermissionUtil.GoToSetting(this)
            }
            R.id.setting_tv_share -> {
                var url = "https://play.google.com/store/apps/details?id=${App.context.packageName}"
                val msg = getString(R.string.share_content, getString(R.string.vc_app_name), url)
                ShareHelper.shareText(this, msg)
            }
            R.id.setting_tv_feedback -> {
                FeedbackHelper.feedback(this)
            }
            R.id.setting_tv_about -> {
                AboutPage.start(
                    this
                )
            }
            R.id.setting_tv_support -> {

            }
        }
    }

    override fun inflate(): VcFragmentSettingBinding {
        return VcFragmentSettingBinding.inflate(layoutInflater)
    }

    override fun init() {
        mBinding.settingTvShare.setOnClickListener(this)
        mBinding.settingTvFeedback.setOnClickListener(this)
        mBinding.settingTvAbout.setOnClickListener(this)
        mBinding.settingTvPermission.setOnClickListener(this)
        mBinding.settingTvSupport.setOnClickListener(this)
    }

}