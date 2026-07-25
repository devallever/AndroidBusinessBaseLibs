package app.allever.android.ai.qr.scanner.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import app.allever.android.ai.qr.scanner.AppActivity
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.helper.SystemHelper
import com.allever.app.qr.code.scaner.R
import com.allever.app.qr.code.scaner.databinding.QrActivityAboutBinding

/**
 */

class AboutActivity : AppActivity(), View.OnClickListener {
    private lateinit var mBinding: QrActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = QrActivityAboutBinding.inflate(layoutInflater)
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
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.qr_about)
        mBinding.aboutPrivacy.setOnClickListener(this)
        val channel = "GooglePlay"
        findViewById<TextView>(R.id.about_app_version).text = "v1.0"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                finish()
            }

            R.id.about_privacy -> {
                val url =
                    "https://www.privacypolicies.com/live/9a33dbf9-6d73-4086-8463-dbd0528e6aca"
                SystemHelper.startWebView(App.context, url)
            }
        }
    }
}
