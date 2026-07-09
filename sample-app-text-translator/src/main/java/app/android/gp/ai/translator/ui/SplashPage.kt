package app.android.gp.ai.translator.ui

import androidx.lifecycle.lifecycleScope
import app.android.gp.ai.translator.app.AppActivity
import app.android.gp.ai.translator.databinding.ASplashBinding
import app.woejt.wwzdndgl.lib.util.ActivityCollector
import app.weong.ajkojt.notch.compat.notchcompat.NotchCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashPage : AppActivity() {

    private lateinit var mBinding: ASplashBinding

    override fun getContentView(): Any {
        mBinding = ASplashBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        NotchCompat.adaptNotchWithFullScreen(window)
    }

    override fun initData() {
        lifecycleScope.launch {
            delay(1000)
            ActivityCollector.startActivity(this@SplashPage, HomePage::class.java)
            finish()
        }
    }

    override fun onBackPressed() {

    }
}