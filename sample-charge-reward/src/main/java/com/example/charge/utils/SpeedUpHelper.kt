package com.example.charge.utils

import app.allever.android.lib.core.app.App

class SpeedUpHelper {
    // 金币: 每秒增加1个，充电状态+1，加速气泡+1(30秒)
    var goldCreateValue = 1f
        private  set
    // 金币生产时间，默认1秒，加速按钮500L(60秒)
    var goldCreateTime = 1f
        private  set
    // 钞票: 每秒增加0个，加速按钮$0.1
    var greenCreateValue = 0f
        private  set
    // 钞票生产时间，默认1秒，加速状态500L
    val greenCreateTime = 1f

    val btnSpeedupCountdownTimer by lazy {
        CountdownTimer()
    }

    val floatIconCountdownTimer by lazy {
        CountdownTimer()
    }

    var btnSpeedCountdownListener: CountdownListener? = null
    var floatIconCountdownListener: CountdownListener? = null

    interface CountdownListener {
        fun onFinish()
        fun onProgressUpdate(progress: Int, remainingSeconds: Int)
    }

    private var lastChargeState = false
    fun onChargeStatusChange(isCharging: Boolean) {
        if (lastChargeState == isCharging) {
            return
        }
        lastChargeState = isCharging
        if (isCharging) {
            goldCreateValue += 1
        } else {
            reduceGoldValue()
        }
    }

    fun isCountingDown() = btnSpeedupCountdownTimer.isCountingDown()

    fun speedUpBtn() {
        goldCreateTime = 0.5f
        greenCreateValue = 0.1f
        //counter1 append time 60
        if (btnSpeedupCountdownTimer.isCountingDown()) {
            // append time
            btnSpeedupCountdownTimer.addSeconds(60)
        } else {
            btnSpeedupCountdownTimer.start(totalSeconds = 60, listener = object :
                CountdownTimer.OnCountdownListener {
                override fun onTick(remainingSeconds: Int, progress: Int) {
                    btnSpeedCountdownListener?.onProgressUpdate(100 - progress, remainingSeconds)
                }

                override fun onFinish() {
                    btnSpeedCountdownListener?.onFinish()
                    goldCreateTime = 1f
                    greenCreateValue = 0f
                }

            })
        }

    }

    fun speedUpFloatIcon() {
        //counter2 append time 30
        if (floatIconCountdownTimer.isCountingDown()) {
            floatIconCountdownTimer.addSeconds(30)
        } else {
            goldCreateValue += 1
            floatIconCountdownTimer.start(totalSeconds = 30, listener = object :
                CountdownTimer.OnCountdownListener {
                override fun onTick(remainingSeconds: Int, progress: Int) {
                    floatIconCountdownListener?.onProgressUpdate(100 - progress, remainingSeconds)
                }

                override fun onFinish() {
                    floatIconCountdownListener?.onFinish()
                    reduceGoldValue()
                }
            })
        }
    }

    fun reduceGoldValue() {
        goldCreateValue -= 1
        if (goldCreateValue <= 0) {
            goldCreateValue = 1f
        }
    }

    fun release() {
        btnSpeedupCountdownTimer.release()
        floatIconCountdownTimer.release()
        btnSpeedCountdownListener = null
        floatIconCountdownListener = null
        goldCreateValue = 1f
        greenCreateValue = 0f
    }
}