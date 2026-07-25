package app.allever.android.ai.qr.scanner.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import app.allever.android.ai.qr.scanner.AppActivity
import com.allever.app.qr.code.scaner.BuildConfig
import com.allever.app.qr.code.scaner.R
import com.allever.app.qr.code.scaner.databinding.ActivityAboutBinding
import app.android.base.lib.App
import app.android.base.lib.util.SystemUtils
import app.android.base.lib.notchcompat.NotchCompat

/**
 */

class AboutActivity : AppActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(mBinding.rootLayout)
        initView()
    }

    private fun initView() {
        NotchCompat.adaptNotchWithFullScreen(window)
        val v = findViewById<View>(R.id.top_bar)
        checkNotch(Runnable {
            addStatusBar(mBinding.rootLayout, v)
        })
        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.about)
        mBinding.aboutPrivacy.setOnClickListener(this)
        val channel = "GooglePlay"
        findViewById<TextView>(R.id.about_app_version).text = "v${BuildConfig.VERSION_NAME}"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                finish()
            }

            R.id.about_privacy -> {
                val url =
                    "https://www.privacypolicies.com/live/9a33dbf9-6d73-4086-8463-dbd0528e6aca"
                SystemUtils.startWebView(App.context, url)
            }
        }
    }
}
