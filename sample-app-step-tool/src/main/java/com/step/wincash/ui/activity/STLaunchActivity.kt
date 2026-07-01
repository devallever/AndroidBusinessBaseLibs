package com.step.wincash.ui.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import com.carefree.steplib.utils.StepTracker
import com.step.wincash.base.BaseActivity
import com.step.wincash.event.GoMainEvent
import com.step.wincash.init.AdIndex
import com.step.wincash.utils.InterAdUtil
import com.step.wincash.business.step.StepBusiness
import com.step.wincash.utils.MusicUtil
import com.step.wincash.utils.SoundUtil
import com.step.wincash.business.withdraw.WithdrawBusiness
import com.step.wincash.databinding.StActivityLaunchBinding
import com.step.wincash.event.AdDismissEvent
import com.step.wincash.event.AdShowFailedEvent
import com.step.wincash.utils.CurrencyUtils
import com.step.wincash.utils.SpKey
import com.step.wincash.utils.SpUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("CustomSplashScreen")
class STLaunchActivity : BaseActivity<StActivityLaunchBinding>() {
    private val animator = ValueAnimator.ofInt(5, 100)
    var toMain = true

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StActivityLaunchBinding {
        return StActivityLaunchBinding.inflate(inflater)
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
            goTo<STMainActivity>(this)
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
            preGoMain()
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