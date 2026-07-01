package app.android.allever.gp.quick.project.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.allever.android.lib.core.ext.log
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityNetworkCheckBinding
import app.android.allever.gp.quick.project.util.InternetUtil
import app.android.allever.gp.quick.project.util.PhoneStateUtils
import app.android.allever.gp.quick.project.vm.NetworkCheckViewModel
import pk.farimarwat.speedtest.Ping

class NetworkCheckActivity: AppActivity<ActivityNetworkCheckBinding, NetworkCheckViewModel>() {

    private val netSettingAnimator by lazy {
        createRotationAnimator(mBinding.ivNetSettingLoading, mBinding.tvNetSettingLoading) {
            mBinding.apply {
                if (!InternetUtil.isConnectedNetwork(this@NetworkCheckActivity)) {
                    tvNetSettingDesc.text = "未连接WIFI或蜂窝移动网络"
                    tvNetSettingLoading.text = "连接异常"
                    tvNetSettingLoading.setTextColor(ContextCompat.getColor(
                        this@NetworkCheckActivity,
                        R.color.googleRed
                    ))
                    btnStart.isVisible = true
                    btnStart.text = "重新检测"
                    return@apply
                }
                val netWorkType = InternetUtil.getNetworkStateName(this@NetworkCheckActivity)
                tvNetSettingDesc.text = "已经连接到${netWorkType}网络"
                tvNetSettingLoading.text = "连接正常"
                tvNetSettingLoading.setTextColor(ContextCompat.getColor(
                    this@NetworkCheckActivity,
                    R.color.googleGreen
                ))
                netSignalAnimator.start()
            }
        }
    }

    private val netSignalAnimator by lazy {
        createRotationAnimator(mBinding.ivNetSignalLoading, mBinding.tvNetSignalLoading) {
            mBinding.apply {
                tvNetSignalLoading.text = "信号正常"
                tvNetSignalLoading.setTextColor(
                    ContextCompat.getColor(
                        this@NetworkCheckActivity,
                        R.color.googleGreen
                    )
                )
                tvNetSignalDesc.text = "${PhoneStateUtils.getCurrentSignalStrength()} dbm"

            }
            netConnectionAnimator.start()
        }
    }

    private val netConnectionAnimator by lazy {
        createRotationAnimator(mBinding.ivNetConnectionLoading, mBinding.tvNetConnectionLoading) {
            mBinding.apply {
                tvNetConnectionLoading.text = "网络正常"

                val builder = Ping.Builder("www.baidu.com")
                    .setListener(object : Ping.PingListener {
                        override fun onStarted() {
                            log("startPing: onStarted")
                        }

                        override fun onError(error: String) {
                            log("startPing: onError -> $error")
                        }

                        override fun onInstantRtt(instantRtt: Double) {
                            log("startPing: onInstantRtt -> ${instantRtt.toInt()}")
                            runOnUiThread {
                                tvNetConnectionDesc.text = "网络延时${instantRtt.toInt()}"
                            }
                        }

                        override fun onAvgRtt(avgRtt: Double) {
                            log("startPing: onAvgRtt -> ${avgRtt.toInt()}")
                        }

                        override fun onFinished(jitter: Int) {
                            log("startPing: onFinished -> $jitter")
                        }

                    })
                    .build()
                builder.start()

                tvNetConnectionLoading.setTextColor(
                    ContextCompat.getColor(
                        this@NetworkCheckActivity,
                        R.color.googleGreen
                    )
                )
            }
            netCommunicationAnimator.start()
        }
    }

    private val netCommunicationAnimator by lazy {
        createRotationAnimator(mBinding.ivNetCommunicationLoading, mBinding.tvNetCommunicationLoading) {
            mBinding.apply {
                tvNetCommunicationLoading.text = "服务器正常"
                tvNetCommunicationLoading.setTextColor(
                    ContextCompat.getColor(
                        this@NetworkCheckActivity,
                        R.color.googleGreen
                    )
                )

                val builder = Ping.Builder("www.baidu.com")
                    .setListener(object : Ping.PingListener {
                        override fun onStarted() {
                            log("startPing: onStarted")
                        }

                        override fun onError(error: String) {
                            log("startPing: onError -> $error")
                        }

                        override fun onInstantRtt(instantRtt: Double) {
                            log("startPing: onInstantRtt -> ${instantRtt.toInt()}")
                            runOnUiThread {
                                tvNetCommunicationDesc.text = "网络延时${instantRtt.toInt()}"
                            }
                        }

                        override fun onAvgRtt(avgRtt: Double) {
                            log("startPing: onAvgRtt -> ${avgRtt.toInt()}")
                        }

                        override fun onFinished(jitter: Int) {
                            log("startPing: onFinished -> $jitter")
                        }

                    })
                    .build()
                builder.start()

                btnStart.text = "重新检测"
                btnStart.isVisible = true
            }
        }
    }

    override fun inflate() = ActivityNetworkCheckBinding.inflate(layoutInflater)

    override fun init() {
        PhoneStateUtils.registerPhoneStateListener(this) {}
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }

            btnStart.setOnClickListener {
                initUnCheckView()
                netSettingAnimator.start()
                btnStart.isVisible = false
            }

        }
    }

    private fun initUnCheckView() {
        mBinding.apply {
            initLoading(tvNetSettingDesc, tvNetSettingLoading)
            initLoading(tvNetSignalDesc, tvNetSignalLoading)
            initLoading(tvNetConnectionDesc, tvNetConnectionLoading)
            initLoading(tvNetCommunicationDesc, tvNetCommunicationLoading)
        }
    }

    private fun initLoading(tvDesc: TextView, tvLoading: TextView) {
        tvDesc.text = "--"
        tvLoading.text = "待检测"
        tvLoading.setTextColor(ContextCompat.getColor(
            this@NetworkCheckActivity,
            R.color.text_gray_light
        ))
    }

    override fun onDestroy() {
        super.onDestroy()
        PhoneStateUtils.unRegisterPhoneStateListener(this)
    }

    private fun createRotationAnimator(iv: ImageView, tv: TextView, end: () -> Unit): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(iv, "rotation", 0f, 360f)
        animator.interpolator = LinearInterpolator()
        animator.repeatMode = ValueAnimator.RESTART
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
}