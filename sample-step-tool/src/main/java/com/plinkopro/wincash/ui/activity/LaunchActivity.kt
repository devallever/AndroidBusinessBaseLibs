package com.plinkopro.wincash.ui.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import com.carefree.steplib.utils.StepTracker
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.base.BaseActivity
import com.plinkopro.wincash.databinding.ActivityLaunchBinding
import com.plinkopro.wincash.event.GoMainEvent
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.utils.InterAdUtil
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.business.step.StepBusiness
import com.plinkopro.wincash.utils.MusicUtil
import com.plinkopro.wincash.utils.SoundUtil
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
import com.plinkopro.wincash.utils.CurrencyUtils
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdDismissEvent
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("CustomSplashScreen")
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {
    private val animator = ValueAnimator.ofInt(5, 100)
    var toMain = true

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityLaunchBinding {
        return ActivityLaunchBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MusicUtil.init()
        SoundUtil.isOpenSound = SpUtil.get(SpKey.IS_SOUND_OPEN, true)
        startProcessAnimation()
        registerEventbus()
        generateUserId()

        if (SpUtil.get(SpKey.GUIDE, true)) {
            CurrencyUtils.clearCurrencyNum()
        }
    }

    private fun preGoMain() {
        if (!StepBusiness.hasRequirePermission(this)){
            StepBusiness.requestPermission(this)
        } else {
            StepTracker.startTrackingService()
            goMain()
        }
    }

    fun goMain() {
        if (toMain) {
            toMain = false
            goTo<MainActivity>(this)
            finish()
        }
    }

    private fun generateUserId() {
        val userId = SpUtil.get(SpKey.USER_ID, "")
        if (userId.isEmpty()) {
            val newUserId = WithdrawBusiness.generateRandomUserId()
            SpUtil.put(SpKey.USER_ID, newUserId)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        StepBusiness.handlePermissionResult(this, requestCode, grantResults) {
            goMain()
        }
    }

    private fun startProcessAnimation() {
        val isFirstLaunchApp = SpUtil.get(SpKey.IS_FIRST_LAUNCH_APP, true)

        SdkManager.dot("app_main_show", mapOf("is_first" to isFirstLaunchApp))
        InterAdUtil.isNewUser = isFirstLaunchApp

        if (isFirstLaunchApp) {
            InterAdUtil.openAppTime = System.currentTimeMillis()
            SpUtil.put(SpKey.IS_FIRST_LAUNCH_APP, false)
        }

        animator.duration = if (isFirstLaunchApp) 8000L else 3000L

        animator.addUpdateListener { animation ->
            binding.progressBar.progress = animation.animatedValue as Int
            binding.progressBarIcon.progress = animation.animatedValue as Int
            binding.tvProgress.text = "${animator.animatedValue}%"
        }

        animator.doOnEnd {
            animator.removeAllUpdateListeners()
            if (isFirstLaunchApp) {
                preGoMain()
            } else {
                //获取开屏广告缓存
                val isShowSuccess = AdManager.isCanShowAdmobOpenAd()
                if (isShowSuccess) {
                    AdManager.showAdMobOpenAd(this, AdIndex.ADMOB_SPLASH_INDEX)
                } else {
                    preGoMain()
                }
                if (BuildConfig.LOG_OUTPUT) {
                    LogUtil.ad("获取开屏广告缓存 state : $isShowSuccess")
                }
            }
        }
        animator.start()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdDismissEvent(event: AdDismissEvent) {
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            goMain()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdShowFailedEvent(event: AdShowFailedEvent) {
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            goMain()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGoMainEvent(event: GoMainEvent) {
        animator.cancel()
        binding.progressBar.progress = 100
    }
}