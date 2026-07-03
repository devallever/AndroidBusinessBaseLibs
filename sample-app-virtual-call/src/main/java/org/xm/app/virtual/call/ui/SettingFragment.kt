package org.xm.app.virtual.call.ui

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.allever.app.virtual.call.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.FeedbackHelper
import app.allever.android.lib.core.helper.ShareHelper
import app.allever.android.lib.core.permission.internal.PermissionUtil
import org.xm.app.virtual.call.app.BaseFragment
import org.xm.app.virtual.call.ui.mvp.presenter.SettingPresenter
import org.xm.app.virtual.call.ui.mvp.view.SettingView

class SettingFragment : BaseFragment<SettingView, SettingPresenter>(), SettingView,
    View.OnClickListener {

    override fun getContentView(): Int = R.layout.vc_fragment_setting

    override fun initView(root: View) {
        root.findViewById<View>(R.id.setting_tv_share).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_feedback).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_about).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_permission).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_support).setOnClickListener(this)
    }

    override fun initData() {
    }

    override fun createPresenter(): SettingPresenter = SettingPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_permission -> {
                PermissionUtil.GoToSetting(activity)
            }
            R.id.setting_tv_share -> {
                var url = "https://play.google.com/store/apps/details?id=${App.context.packageName}"
                val msg = getString(R.string.share_content, getString(R.string.vc_app_name), url)
                ShareHelper.shareText(this, msg)
            }
            R.id.setting_tv_feedback -> {
                FeedbackHelper.feedback(activity)
            }
            R.id.setting_tv_about -> {
                AboutActivity.start(
                    requireActivity()
                )
            }
            R.id.setting_tv_support -> {
                supportUs()

            }
        }
    }

    private fun supportUs() {
        AlertDialog.Builder(requireActivity())
            .setTitle("温馨提示")
            .setMessage("该操作会消耗一定的数据流量，您要观看吗?")
            .setPositiveButton("立即观看") { dialog, which ->
                dialog.dismiss()
                //流程加载视频  -> 下载 -> 插屏
//                loadEncourageVideoAd()
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