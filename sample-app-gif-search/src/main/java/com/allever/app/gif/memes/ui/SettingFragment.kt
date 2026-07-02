package com.allever.app.gif.memes.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.allever.app.gif.memes.R
//import com.allever.app.gif.search.ad.AdConstants
import com.funny.gif.memes.app.BaseFragment
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.func.store.Version
import com.allever.app.gif.memes.ui.mvp.presenter.SettingPresenter
import com.allever.app.gif.memes.ui.mvp.view.SettingView
import com.funny.gif.memes.util.SystemUtils
//import com.allever.lib.ad.chain.AdChainHelper
//import com.allever.lib.ad.chain.AdChainListener
//import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.app.App
import com.allever.lib.common.util.FeedbackHelper
import com.allever.lib.common.util.toast
import com.allever.lib.permission.PermissionUtil

class SettingFragment : BaseFragment<SettingView, SettingPresenter>(), SettingView,
    View.OnClickListener {

    private var mBannerContainer: ViewGroup? = null
    private lateinit var mSwitchVersion: SwitchCompat
//    private var mVideoAd: IAd? = null
//    private var mBannerAd: IAd? = null
//    private var mInsertAd: IAd? = null

    override fun getContentView(): Int = R.layout.fragment_setting

    override fun initView(root: View) {
        root.findViewById<View>(R.id.setting_tv_share).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_feedback).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_about).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_permission).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_support).setOnClickListener(this)
        mBannerContainer = root.findViewById<ViewGroup>(R.id.banner_container)
        mSwitchVersion = root.findViewById<SwitchCompat>(R.id.switchVersion)
        mSwitchVersion.setOnClickListener(this)
        mSwitchVersion.isChecked = Store.getVersion() == Version.INTERNATIONAL

    }

    override fun initData() {
//        mHandler.postDelayed({
//            loadAndShowBanner()
//        }, 9000)
    }

    override fun createPresenter(): SettingPresenter = SettingPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_permission -> {
                PermissionUtil.GoToSetting(activity)
            }
            R.id.setting_tv_share -> {
                val msg =
                    "【${getString(R.string.app_name)}】这个应用很不错，推荐一下。获取地址 \nhttp://app.mi.com/details?id=com.allever.app.virtual.call"
                val intent = SystemUtils.getShareIntent(App.context, msg)
                startActivity(intent)
            }
            R.id.setting_tv_feedback -> {
                FeedbackHelper.feedback(activity)
            }
            R.id.setting_tv_about -> {
                AboutActivity.start(activity!!)
            }
            R.id.setting_tv_support -> {
                supportUs()
            }
            R.id.switchVersion -> {
                Store.saveVersion(if (mSwitchVersion.isChecked) {
                    Version.INTERNATIONAL
                } else {
                    Version.INTERNAL
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        mBannerAd?.destroy()
//        mVideoAd?.destroy()
//        mInsertAd?.destroy()
    }

//    private fun loadAndShowBanner() {
//        mHandler.postDelayed({
//            AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, mBannerContainer, object :
//                AdChainListener {
//                override fun onLoaded(ad: IAd?) {
//                    mBannerAd = ad
//                }
//
//                override fun onFailed(msg: String) {}
//                override fun onShowed() {}
//                override fun onDismiss() {}
//
//            })
//        }, 3000)
//    }

    private fun supportUs() {
        AlertDialog.Builder(activity!!)
            .setTitle("温馨提示")
            .setMessage("该操作会消耗一定的数据流量，您要观看吗?")
            .setPositiveButton("立即观看") { dialog, which ->
                dialog.dismiss()
//                AdChainHelper.loadAd(AdConstants.AD_NAME_VIDEO, null, object : AdChainListener {
//                    override fun onLoaded(ad: IAd?) {
//                        mVideoAd = ad
//                        ad?.show()
//                    }
//
//                    override fun onFailed(msg: String) {
////                        loadInsertAd()
//                    }
//
//                    override fun onShowed() {}
//                    override fun onDismiss() {}
//
//                })
            }
            .setNegativeButton("残忍拒绝") { dialog, which ->
                dialog.dismiss()
                toast("您可以点击小广告，也是对我们的一种支持.")
            }
            .create()
            .show()
    }

//    private fun loadInsertAd() {
//        AdChainHelper.loadAd(AdConstants.AD_NAME_EXIT_INSERT, null, object : AdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mInsertAd = ad
//                mInsertAd?.show()
//            }
//
//            override fun onFailed(msg: String) {
//                toast("暂时没有视频，您可以点击小广告，也是对我们的一种支持.")
//            }
//
//            override fun onShowed() {
//            }
//
//            override fun onDismiss() {
//            }
//
//        })
//    }
}