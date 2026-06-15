package com.example.charge.ui.activity

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import com.example.charge.ad.AdIndex
import com.example.charge.ad.AdmobOpenAdUtil
import com.example.charge.base.BaseActivity
import com.example.charge.databinding.ActivityLaunchBinding
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdDismissEvent
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

class HotLaunchActivity : BaseActivity<ActivityLaunchBinding>() {

    private val animator = ValueAnimator.ofFloat(0f, 100f)


    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityLaunchBinding {
        return ActivityLaunchBinding.inflate(layoutInflater)
    }

    override fun enableEventBus(): Boolean {
        return true
    }


    override fun onPause() {
        super.onPause()
        animator.pause()
    }

    override fun onResume() {
        super.onResume()
        animator.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        animator.cancel()
    }

    override fun initView() {
        startProcessAnimation()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!animator.isRunning) {
                    //禁止此回调，交给系统处理
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    //处理完重新开启回调
                    isEnabled = true
                }
            }

        })
    }


    private fun startProcessAnimation() {
        animator.duration = 500
        animator.addUpdateListener { animation ->
            val progress = (animation.animatedValue as Float).roundToInt()
            binding.progressBar.progress = progress
        }
        animator.doOnEnd {
            AdmobOpenAdUtil.updateShowTime()
            AdManager.showAdMobOpenAd(this, AdIndex.ADMOB_SPLASH_INDEX)
        }
        animator.start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdDismissEvent(event: AdDismissEvent) {
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdShowFailedEvent(event: AdShowFailedEvent) {
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            finish()
        }
    }

}