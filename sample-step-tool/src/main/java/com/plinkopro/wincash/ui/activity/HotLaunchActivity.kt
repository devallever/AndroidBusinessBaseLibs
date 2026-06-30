package com.plinkopro.wincash.ui.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import com.plinkopro.wincash.base.BaseActivity
import com.plinkopro.wincash.databinding.ActivityLaunchBinding
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.utils.AdmobOpenAdUtil
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdDismissEvent
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt

class HotLaunchActivity: BaseActivity<ActivityLaunchBinding>() {

    private val animator = ValueAnimator.ofFloat(0f, 100f)

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityLaunchBinding {
        return  ActivityLaunchBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerEventbus()
        startProcessAnimation()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
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


    private fun startProcessAnimation(){
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
    fun onAdDismissEvent(event: AdDismissEvent){
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdShowFailedEvent(event: AdShowFailedEvent){
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            finish()
        }
    }

}