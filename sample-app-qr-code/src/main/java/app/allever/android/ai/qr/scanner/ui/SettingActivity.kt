package app.allever.android.ai.qr.scanner.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import app.allever.android.ai.qr.scanner.AppActivity
import com.allever.app.qr.code.scaner.R
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import com.allever.app.qr.code.scaner.databinding.QrActivitySettingBinding

/**
 */

class SettingActivity : AppActivity(), View.OnClickListener {
    private lateinit var mBinding: QrActivitySettingBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = QrActivitySettingBinding.inflate(layoutInflater)
        setContentView(mBinding.rootLayout)
        initView()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, SettingFragment()).commit()
        }
    }

    @SuppressLint("CommitTransaction")
    private fun initView() {
        NotchCompat.adaptNotchWithFullScreen(window)
        val v = findViewById<View>(R.id.top_bar)
        checkNotch(Runnable {
            addStatusBar(mBinding.rootLayout, v)
        })
        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.qr_tab_name_setting)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                finish()
            }
        }
    }
}
