package app.android.gp.ai.translator.ui

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.helper.ActivityHelper
import app.android.gp.ai.translator.app.AppActivity
import app.android.gp.ai.translator.databinding.ASplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashPage : AppActivity() {

    private lateinit var mBinding: ASplashBinding

    override fun getContentView(): Any {
        mBinding = ASplashBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }

        })
    }

    override fun initData() {
        lifecycleScope.launch {
            delay(1000)
            ActivityHelper.startActivity<HomePage>()
            finish()
        }
    }

}