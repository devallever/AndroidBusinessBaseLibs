package app.android.allever.gp.quick.project.ui

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.ext.log
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityPingBinding
import app.android.allever.gp.quick.project.vm.PingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pk.farimarwat.speedtest.Ping

class PingActivity: AppActivity<ActivityPingBinding, PingViewModel>() {
    override fun inflate() = ActivityPingBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }

            gaugeView.prepareGauge(this@PingActivity)

            btnStart.setOnClickListener {
                btnStart.isVisible = false
                btnStop.isVisible = true

                startTest()
            }

            btnStop.setOnClickListener {
                btnStart.isVisible = true
                btnStop.isVisible = false
                mReceiveCount = 0
                mSendCount = 0
                mErrorCount = 0
                tvReceive.text = "0"
                tvSend.text = "0"
                tvDismissRate.text = "0.0%"
                gaugeView.setProgress(0f)

                stopTest()
            }

        }
    }

    private var pingJob: Job? = null
    private var mStop = true
    private var mSendCount = 0
    private var mReceiveCount = 0
    private var mErrorCount = 0
    private fun startTest() {
        mStop = false
        pingJob?.cancel()
        pingJob = lifecycleScope.launch (Dispatchers.Default){
            val builder = Ping.Builder("www.baidu.com")
                .setListener(object : Ping.PingListener {
                    override fun onStarted() {
                        log("startPing: onStarted")
                    }

                    override fun onError(error: String) {
                        log("startPing: onError -> $error")
                        mErrorCount++
                        mSendCount++
                        updateMissRate()
                    }

                    override fun onInstantRtt(instantRtt: Double) {
                        log("startPing: onInstantRtt -> ${instantRtt.toInt()}")
                        if (mStop) {
                            runOnUiThread {
                                mBinding.apply {
                                    tvReceive.text = "0"
                                    tvSend.text = "0"
                                    tvDismissRate.text = "0.0 %"
                                    gaugeView.setProgress(0f)
                                }
                            }
                            return
                        }
                        mSendCount++
                        if (instantRtt > 0) {
                            mReceiveCount++
                        }
                        runOnUiThread {
                            mBinding.tvReceive.text = mReceiveCount.toString()
                            mBinding.tvSend.text = mSendCount.toString()
                            mBinding.gaugeView.setProgress(instantRtt.toFloat())
                            updateMissRate()
                        }
                    }

                    override fun onAvgRtt(avgRtt: Double) {
                        log("startPing: onAvgRtt -> ${avgRtt.toInt()}")
                    }

                    override fun onFinished(jitter: Int) {
                        log("startPing: onFinished -> $jitter")
                        var value = jitter

                        if (!mStop) {
                            startTest()
                        }
                    }

                })
                .build()
            builder.start()

        }
    }

    private fun stopTest() {
        mStop = true
        pingJob?.cancel()
    }

    private fun updateMissRate() {
        val value = mErrorCount.toFloat() / mSendCount.toFloat()
        runOnUiThread {
            mBinding.tvDismissRate.text = "${value * 100} %"
        }
    }
    
}