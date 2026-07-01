package app.android.allever.gp.quick.project.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.RESTART
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityWifiSafeBinding
import app.android.allever.gp.quick.project.util.InternetUtil
import app.android.allever.gp.quick.project.util.IPHelper

class WifiSafeActivity : AppActivity<ActivityWifiSafeBinding, BaseViewModel>() {

    private val deviceCountAnimator by lazy {
        createRotationAnimator(mBinding.ivDeviceLoading, mBinding.tvDeviceLoading) {
            mBinding.apply {
                tvDeviceLoading.text = "发现1台设备连接此网络"
                tvCheckStatus.text = "正在进行安全检测"
                ivDeviceMore.isVisible = true
            }
            fishWifiAnimator.start()
        }
    }

    private val fishWifiAnimator by lazy {
        createRotationAnimator(mBinding.ivFishLoading, mBinding.tvFishLoading) {
            mBinding.apply {
                tvFishLoading.text = "安全"
                tvFishLoading.setTextColor(
                    ContextCompat.getColor(
                        this@WifiSafeActivity,
                        R.color.googleBleu
                    )
                )
            }
            publishWifiAnimator.start()
        }
    }

    private val publishWifiAnimator by lazy {
        createRotationAnimator(mBinding.ivPublishLoading, mBinding.tvPublishLoading) {
            mBinding.apply {
                tvPublishLoading.text = "安全"
                tvPublishLoading.setTextColor(
                    ContextCompat.getColor(
                        this@WifiSafeActivity,
                        R.color.googleBleu
                    )
                )
            }
            autoJumpFishWifiAnimator.start()
        }
    }

    private val autoJumpFishWifiAnimator by lazy {
        createRotationAnimator(mBinding.ivaAutoJumpFishLoading, mBinding.tvaAutoJumpFishLoading) {
            mBinding.apply {
                tvaAutoJumpFishLoading.text = "安全"
                tvaAutoJumpFishLoading.setTextColor(
                    ContextCompat.getColor(
                        this@WifiSafeActivity,
                        R.color.googleBleu
                    )
                )
                radarView.stop()

                tvIp.text = IPHelper.getInternalIp()
                tvMaskCode.text = "0.0.0.0"

                tvCheckStatus.text = "设备安全"
            }
        }
    }

    private fun createRotationAnimator(iv: ImageView,tv: TextView,  end: () -> Unit): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(iv, "rotation", 0f, 360f)
        animator.interpolator = LinearInterpolator()
        animator.repeatMode = RESTART
        animator.repeatCount = 6
        animator.duration = 500
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                log("onAnimationStart")
                iv.isVisible = true
                tv.text = "检测中"
            }

            override fun onAnimationEnd(animation: Animator) {
                log("onAnimationEnd")
                iv.isVisible = false
                end.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {
                log("onAnimationCancel")
            }

            override fun onAnimationRepeat(animation: Animator) {
                log("onAnimationRepeat")
            }

        })
        return animator
    }

    override fun inflate() = ActivityWifiSafeBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener { finish() }

            if (InternetUtil.isWifi(this@WifiSafeActivity)) {
                radarView.start()

                ivDeviceMore.setOnClickListener {
                    ActivityHelper.startActivity<OnlineDeviceActivity>(this@WifiSafeActivity) {  }
                }
                deviceCountAnimator.start()
            } else {
                AlertDialog.Builder(this@WifiSafeActivity)
                    .setTitle("温馨提示")
                    .setMessage("请在WIFI网络下进行")
                    .setCancelable(false)
                    .setPositiveButton(
                        "确定"
                    ) { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }



        }

    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding.radarView.stop()
        deviceCountAnimator.cancel()
        fishWifiAnimator.cancel()
        publishWifiAnimator.cancel()
        autoJumpFishWifiAnimator.cancel()
    }
}