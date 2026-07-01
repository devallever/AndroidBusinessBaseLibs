package org.xm.secret.photo.album.ui

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ShareHelper
import app.allever.android.lib.core.permission.internal.PermissionUtil
import org.xm.secret.photo.album.R
import org.xm.secret.photo.album.app.BaseFragment
import org.xm.secret.photo.album.ui.mvp.presenter.SettingPresenter
import org.xm.secret.photo.album.ui.mvp.view.SettingView
import org.xm.secret.photo.album.util.FeedbackHelper

class SettingFragment: BaseFragment<SettingView, SettingPresenter>(), SettingView, View.OnClickListener {

    override fun getContentView(): Int = R.layout.fragment_setting

    override fun initView(root: View) {
        root.findViewById<View>(R.id.setting_tv_share).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_permission).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_modify_password).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_feedback).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_about).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_support).setOnClickListener(this)

        mBannerContainer = root.findViewById(R.id.bannerContainer)

    }

    override fun initData() {
    }

    override fun createPresenter(): SettingPresenter = SettingPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_permission -> {
                PermissionUtil.GoToSetting(activity)
            }

            R.id.setting_tv_modify_password -> {
                ChangePasswordActivity.start(requireContext(), true)
            }

            R.id.setting_tv_feedback -> {
                FeedbackHelper.feedback(activity)
            }

            R.id.setting_tv_about -> {
                AboutActivity.start(activity)
            }

            R.id.setting_tv_share -> {
                val url = "https://play.google.com/store/apps/details?id=${App.context.packageName}"
                val msg = getString(R.string.share_content, getString(R.string.sa_app_name), url)
                ShareHelper.shareText(this, msg)
            }

            R.id.setting_tv_support -> {
                supportUs()
            }
        }
    }


    private lateinit var mBannerContainer: ViewGroup

    private fun supportUs() {
        AlertDialog.Builder(requireContext())
            .setTitle("温馨提示")
            .setMessage("该操作会消耗一定的数据流量，您要观看吗?")
            .setPositiveButton("立即观看") { dialog, which ->
                dialog.dismiss()
                //流程加载视频  -> 下载 -> 插屏
//                loadInsert()
            }
            .setNegativeButton("残忍拒绝") { dialog, which ->
                dialog.dismiss()
                toast("您可以点击下方小广告，也是对我们的一种支持。")
            }
            .create()
            .show()
    }
}