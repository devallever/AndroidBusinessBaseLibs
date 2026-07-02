package com.allever.app.gif.memes.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.allever.app.gif.memes.R
//import com.allever.app.gif.search.ad.AdConstants
import com.funny.gif.memes.app.BaseActivity
import com.allever.app.gif.memes.ui.mvp.presenter.BackupRestorePresenter
import com.allever.app.gif.memes.ui.mvp.view.BackupRestoreView
//import com.allever.lib.ad.chain.AdChainHelper
//import com.allever.lib.ad.chain.AdChainListener
//import com.allever.lib.ad.chain.IAd

class BackupRestoreActivity : BaseActivity<BackupRestoreView, BackupRestorePresenter>(),
    BackupRestoreView,
    View.OnClickListener {

    private lateinit var mBtnBackup: Button
    private lateinit var mBtnRestore: Button
    private lateinit var mBtnDelBackup: Button

//    private var mBannerAd: IAd? = null
//    private var mInsertAd: IAd? = null

    override fun getContentView(): Any = R.layout.activity_backup_restore

    override fun initView() {
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.backup)
        mBtnBackup = findViewById(R.id.btnBackup)
        mBtnBackup.setOnClickListener(this)
        mBtnRestore = findViewById(R.id.btnRestore)
        mBtnRestore.setOnClickListener(this)
        mBtnDelBackup = findViewById(R.id.btnDeleteBackup)
        mBtnDelBackup.setOnClickListener(this)
    }

    override fun initData() {
//        loadAndShowBanner()
    }

    override fun createPresenter(): BackupRestorePresenter = BackupRestorePresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.btnBackup -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.warm_tips)
                    .setMessage(R.string.backup_tips)
                    .setPositiveButton(
                        R.string.backup
                    ) { dialog, which ->
                        mBtnBackup.isClickable = false
                        mPresenter?.backup(Runnable {
                            mBtnBackup.isClickable = true
//                            loadAndShowInsert()
                        })
                        dialog.dismiss()
                    }
                    .setNegativeButton(
                        R.string.cancle
                    ) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
            R.id.btnRestore -> {
                mBtnRestore.isClickable = false
                mPresenter?.restore(Runnable {
                    mBtnRestore.isClickable = true
//                    loadAndShowInsert()
                })
            }

            R.id.btnDeleteBackup -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.warm_tips)
                    .setMessage(R.string.del_backup_tips)
                    .setPositiveButton(
                        R.string.del_backup
                    ) { dialog, which ->
                        mBtnDelBackup.isClickable = false
                        mPresenter?.delBackup(Runnable {
                            mBtnDelBackup.isClickable = true
//                            loadAndShowInsert()
                        })
                        dialog.dismiss()
                    }
                    .setNegativeButton(
                        R.string.cancle
                    ) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
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
        super.onDestroy()
//        mBannerAd?.destroy()
//        mInsertAd?.destroy()
        setResult(Activity.RESULT_OK)
    }

//    private fun loadAndShowBanner() {
//        val container = findViewById<ViewGroup>(R.id.adContainer)
//        AdChainHelper.loadAd(AdConstants.AD_NAME_BANNER, container, object : AdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mBannerAd = ad
//            }
//
//            override fun onFailed(msg: String) {}
//            override fun onShowed() {}
//            override fun onDismiss() {}
//
//        })
//    }
//
//    private fun loadAndShowInsert() {
//        AdChainHelper.loadAd(
//            AdConstants.AD_NAME_EXIT_INSERT,
//            window.decorView as ViewGroup,
//            object : AdChainListener {
//                override fun onLoaded(ad: IAd?) {
//                    mInsertAd = ad
//                    ad?.show()
//                }
//
//                override fun onFailed(msg: String) {}
//                override fun onShowed() {}
//                override fun onDismiss() {}
//
//            })
//    }

    companion object {
        val RC_RESULT = 0X01
        fun start(activity: Activity) {
            val intent = Intent(activity, BackupRestoreActivity::class.java)
            activity.startActivityForResult(intent, RC_RESULT)
        }
    }

}