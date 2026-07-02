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
//import com.allever.app.gif.search.ad.AdConstants
import com.funny.gif.memes.app.BaseActivity
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.func.store.Version
import com.allever.app.gif.memes.ui.mvp.presenter.SettingPresenter
import com.allever.app.gif.memes.ui.mvp.view.SettingView
import com.funny.gif.memes.util.SpUtils
//import com.allever.lib.ad.chain.AdChainHelper
//import com.allever.lib.ad.chain.AdChainListener
//import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.app.App
import com.allever.lib.common.util.*
//import com.allever.lib.recommend.RecommendGlobal

class SettingActivity : BaseActivity<SettingView, SettingPresenter>(),
    SettingView, View.OnClickListener {

//    private var mBannerAd: IAd? = null
//    private var mVideoAd: IAd? = null
//    private var mInsertAd: IAd? = null

    private lateinit var mSwitchVersion: SwitchCompat

    override fun getContentView(): Any = R.layout.activity_setting

    override fun initView() {
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
//        val bannerContainer = findViewById<ViewGroup>(R.id.bannerContainer)
//        AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, bannerContainer, object : AdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mBannerAd = ad
//            }
//
//            override fun onFailed(msg: String) {}
//            override fun onShowed() {}
//            override fun onDismiss() {}
//        })
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
                val msg = getString(R.string.share_content, getString(R.string.app_name), url)
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
                ActivityCollector.startActivity(this, BackupRestoreActivity::class.java)
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

    override fun onResume() {
        super.onResume()
//        mBannerAd?.onAdResume()
    }

    override fun onPause() {
        super.onPause()
//        mBannerAd?.onAdPause()
    }

    override fun onDestroy() {
//        mVideoAd?.destroy()
//        mInsertAd?.destroy()
//        mBannerAd?.destroy()
        super.onDestroy()
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

//    private fun loadEncourageVideoAd() {
//        mVideoAd?.destroy()
//        mVideoAd = null
//        AdChainHelper.loadAd(AdConstants.AD_NAME_VIDEO, null, object :
//            AdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mVideoAd = ad
//                mVideoAd?.show()
//            }
//
//            override fun onShowed() {
//            }
//
//            override fun onDismiss() {
//            }
//
//            override fun onFailed(msg: String) {
//                loadInsert()
//            }
//
//        })
//    }
//
//    private fun loadInsert() {
//        AdChainHelper.loadAd(
//            AdConstants.AD_NAME_EXIT_INSERT,
//            window?.decorView as ViewGroup,
//            object :
//                AdChainListener {
//                override fun onLoaded(ad: IAd?) {
//                    mInsertAd = ad
//                    mInsertAd?.show()
//                }
//
//                override fun onShowed() {
//                }
//
//                override fun onDismiss() {
//                }
//
//                override fun onFailed(msg: String) {
//                    toast("请求失败, 您可以点击下方小广告，也是对我们的一种支持。")
//                }
//
//            })
//    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingActivity::class.java)
            context.startActivity(intent)
        }
    }
}