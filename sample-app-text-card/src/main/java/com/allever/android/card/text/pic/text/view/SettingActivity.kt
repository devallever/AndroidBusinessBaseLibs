package com.allever.android.card.text.pic.text.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.allever.android.card.text.pic.text.util.StatusBarUtil
import com.allever.android.card.text.pic.text.BuildConfig
import com.allever.android.card.text.pic.text.base.AbsViewModel
import com.allever.android.card.text.pic.text.base.AppActivity
import com.allever.android.card.text.pic.text.databinding.ActivitySettingBinding
import com.allever.android.card.text.pic.text.util.toast

class SettingActivity : AppActivity<ActivitySettingBinding, AbsViewModel>() {
    override fun viewModelClass() = AbsViewModel::class.java

    override fun inflate() = ActivitySettingBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            StatusBarUtil.fixStatusBar(ivClose)
            ivClose.setOnClickListener {
                finish()
            }

            tvRateUs.setOnClickListener {
                toast("rate us")
            }

            tvShare.setOnClickListener {
                toast("Share")
            }

            tvPrivacy.setOnClickListener {
                //跳转体统浏览器打开网页
                startWebView(this@SettingActivity, "https://www.privacypolicies.com/live/01bbffb5-0610-43dc-8a1a-ef18de59b3b5")
            }

            tvVersion.text = BuildConfig.VERSION_NAME
        }
    }

    private fun startWebView(context: Context, uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Sorry, Your mobile can't be supported", Toast.LENGTH_LONG)
                .show()
        }

    }
}