package com.example.charge.ui.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import app.allever.android.lib.core.app.App
import com.example.charge.ad.AdIndex
import com.example.charge.ad.InterAdUtil
import com.example.charge.base.BaseActivity
import com.example.charge.databinding.ActivityLaunchBinding
import com.example.charge.event.GoMainEvent
import com.example.charge.utils.LogUtil
import com.example.charge.utils.MusicUtil
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdDismissEvent
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("CustomSplashScreen")
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {
    private val animator = ValueAnimator.ofInt(5, 100)

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityLaunchBinding {
        return ActivityLaunchBinding.inflate(inflater)
    }

    override fun enableEventBus(): Boolean {
        return true
    }

    override fun initView() {
        MusicUtil.init()
        startProcessAnimation()
    }

    fun goMain() {
        if (App.DEBUG) {
            LogUtil.local("进入首页")
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun startProcessAnimation() {
        if (App.DEBUG) {
            LogUtil.local("启动动画")
        }
        val isFirstLaunchApp = SpUtil.get(SpKey.IS_FIRST_LAUNCH_APP, true)
        InterAdUtil.isNewUser = isFirstLaunchApp

        if (isFirstLaunchApp) {
            InterAdUtil.openAppTime = System.currentTimeMillis()
            SpUtil.put(SpKey.IS_FIRST_LAUNCH_APP, false)
        }

        animator.duration = if (isFirstLaunchApp) 8000L else 3000L

        animator.addUpdateListener { animation ->
            (animation.animatedValue as Int).let {
                binding.apply {
                    progressBar.progress = it
                    progressBarIcon.progress = it
                    tvProgress.text = "$it%"
                }
            }
        }

        animator.doOnEnd {
            if (App.DEBUG) {
                LogUtil.local("结束动画")
            }
            animator.removeAllUpdateListeners()
            if (isFirstLaunchApp) {
                goMain()
            } else {
                //获取开屏广告缓存
                val isShowSuccess = AdManager.isCanShowAdmobOpenAd()
                if (App.DEBUG) {
                    LogUtil.ad("获取开屏广告缓存 state : $isShowSuccess")
                }
                if (App.DEBUG) {
                    LogUtil.local("启动页 获取开屏广告缓存 state : $isShowSuccess")
                }
                if (isShowSuccess) {
                    AdManager.showAdMobOpenAd(this, AdIndex.ADMOB_SPLASH_INDEX)
                } else {
                    goMain()
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
            if (App.DEBUG) {
                LogUtil.local("启动页接收到开屏广告关闭事件")
            }
            goMain()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdShowFailedEvent(event: AdShowFailedEvent) {
        if (event.adIndex == AdIndex.ADMOB_SPLASH_INDEX) {
            if (App.DEBUG) {
                LogUtil.local("启动页接收到开屏广告显示失败事件")
            }
            goMain()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGoMainEvent(event: GoMainEvent) {
        animator.cancel()
        binding.progressBar.progress = 100
        if (App.DEBUG) {
            LogUtil.local("启动页接收到FP拉取成功事件")
        }
        goMain()
    }

}