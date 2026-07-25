package app.allever.android.ai.qr.scanner.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import app.allever.android.ai.qr.scanner.AppActivity
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import com.allever.app.qr.code.scaner.R
import com.allever.app.qr.code.scaner.databinding.ActivityHistoryBinding

/**
 */

class HistoryActivity : AppActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityHistoryBinding
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(mBinding.rootLayout)
        initView()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, HistoryFragment()).commit()
        }
    }

    private fun initView() {
        NotchCompat.adaptNotchWithFullScreen(window)
        val v = findViewById<View>(R.id.top_bar)
        checkNotch(Runnable {
            addStatusBar(mBinding.rootLayout, v)
        })
        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.tab_name_history)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentFragment = supportFragmentManager.fragments[0] as BaseFragment
        if (currentFragment.onKeyDown(keyCode, event)) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }
}
