package app.android.allever.gp.quick.project.ui

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityWifiSignalBinding
import app.android.allever.gp.quick.project.util.InternetUtil
import java.util.Timer
import java.util.TimerTask

class WifiSignalActivity: AppActivity<ActivityWifiSignalBinding, BaseViewModel>() {

    override fun inflate() = ActivityWifiSignalBinding.inflate(layoutInflater)

    override fun init() {


        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }

            if (!InternetUtil.isWifi(this@WifiSignalActivity)) {
                AlertDialog.Builder(this@WifiSignalActivity)
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
                return
            }

            gaugeView.prepareGauge(this@WifiSignalActivity)

            btnStart.setOnClickListener {
                btnStart.isVisible = false
                btnStop.isVisible = true
                tvTime.isVisible = true
                startTest()
            }

            btnStop.setOnClickListener {
                btnStart.isVisible = true
                btnStop.isVisible = false
                tvTime.isVisible = false

                gaugeView.setProgress(0F)
                tvWifiValue.text = "信号强度: --"
                mSecond = -1

                stopTest()
            }

        }
    }

    private var mTimer: Timer? = null
    private var mSecond = -1

    private fun startTest() {
        mTimer?.cancel()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mTimer = Timer()
        mTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val signalStrength = wifiManager.connectionInfo.rssi
                val newValue = signalStrength + 80
                log("WiFi Signal Strength: $signalStrength dBm")
                mBinding.apply {
                    runOnUiThread {
                        gaugeView.setProgress(newValue.toFloat())
                        tvWifiValue.text = "信号强度: $newValue"
                        mSecond ++
                        val time = secondsToTimeString(mSecond)
                        tvTime.text = time
                    }
                }

            }
        }, 0, 1000)
    }

    private fun stopTest() {
        mTimer?.cancel()
    }

    private fun secondsToTimeString(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTest()
    }

}