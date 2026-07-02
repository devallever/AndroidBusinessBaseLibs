package com.allever.app.gif.memes.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseActivity
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.func.store.Version
import com.allever.app.gif.memes.ui.mvp.presenter.SettingPresenter
import com.allever.app.gif.memes.ui.mvp.view.SettingView
import com.funny.gif.memes.util.SpUtils
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.core.helper.FeedbackHelper
import app.allever.android.lib.core.helper.ShareHelper

class SettingActivity : BaseActivity<SettingView, SettingPresenter>(),
    SettingView, View.OnClickListener {

    private lateinit var mSwitchVersion: SwitchCompat

    override fun getContentView(): Any = R.layout.gs_activity_setting

    override fun initView() {
        adaptStatusBar(findViewById(R.id.top_bar))
        findViewById<View>(R.id.setting_tv_share).setOnClickListener(this)
        findViewById<TextView>(R.id.setting_tv_feedback).setOnClickListener(this)
        findViewById<TextView>(R.id.setting_tv_about).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.setting)
        findViewById<View>(R.id.setting_tv_support).setOnClickListener(this)
        findViewById<View>(R.id.setting_tv_backup).setOnClickListener(this)
        mSwitchVersion = findViewById(R.id.switchVersion)
        mSwitchVersion.setOnClickListener(this)
        mSwitchVersion.isChecked = Store.getVersion() == Version.INTERNATIONAL
    }

    override fun initData() {
    }

    override fun createPresenter(): SettingPresenter =
        SettingPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_share -> {
                var url = ""
                if (TextUtils.isEmpty(url)) {
                    url = "https://play.google.com/store/apps/details?id=${App.context.packageName}"
                }
                val msg = getString(R.string.share_content, getString(R.string.gs_app_name), url)
                ShareHelper.shareText(this, msg)
            }
            R.id.setting_tv_feedback -> {
                FeedbackHelper.feedback(this)
            }
            R.id.setting_tv_about -> {
                AboutActivity.start(this)
            }
            R.id.iv_left -> {
                finish()
            }
            R.id.setting_tv_support -> {
                supportUs()
//                Tool.openInGooglePlay(this, App.context.packageName)
            }
            R.id.setting_tv_backup -> {
                ActivityHelper.startActivity<BackupRestoreActivity>()
            }
            R.id.switchVersion -> {
                Store.saveVersion(if (mSwitchVersion.isChecked) {
                    Version.INTERNATIONAL
                } else {
                    Version.INTERNAL
                })
                SpUtils.putString(Global.SP_OFFSET, "0")
                SpUtils.putString(Global.SP_SEARCH_OFFSET, "0")
            }
        }
    }

    private fun supportUs() {
        AlertDialog.Builder(this)
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
                toast("您可以每天点击下方小广告一次，也是对我们的一种支持。")
            }
            .create()
            .show()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingActivity::class.java)
            context.startActivity(intent)
        }
    }
}