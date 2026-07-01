package app.android.allever.gp.quick.project

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.animation.AccelerateInterpolator
import app.allever.android.lib.core.ext.launchAndCollectIn
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.NstActivityLoadingBinding
import app.android.allever.gp.quick.project.ui.NSTHomeActivity
import app.android.allever.gp.quick.project.util.LevelProgress

@SuppressLint("CustomSplashScreen")
class NSTLoadingActivity : AppActivity<NstActivityLoadingBinding, NSTLoadingActivity.LoadingViewModel>() {

    private val levelProgress = LevelProgress(this)

    private val delayTime = 500L

    override fun inflate() = NstActivityLoadingBinding.inflate(layoutInflater)

    override fun init() {
        ValueAnimator.ofInt(0, 100).apply {
            duration = 2000
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                mBinding.progressBar.progress = value
                mBinding.tvProgressText.text = "${value}%"
                if (value == 100) {
                    jumpMain()
                }
            }
        }.start()

        initObserver()
    }

    fun initObserver() {
        levelProgress.progressLiveData.launchAndCollectIn(this) {
            mBinding.progressBar.progress = it
            mBinding.tvProgressText.text = "${it}%"
            if (it == 100) {
                jumpMain()
            }
        }
    }

    private fun jumpMain() {
        ActivityHelper.startActivity<NSTHomeActivity>(this@NSTLoadingActivity) { }
        finish()
    }

    override fun onBackPressed() {

    }

    class LoadingViewModel : BaseViewModel() {

    }
}