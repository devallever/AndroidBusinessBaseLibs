package com.example.charge

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import app.allever.android.lib.core.app.App
import com.example.charge.base.AppLifecycleCallback
import com.example.charge.constant.LogTag
import com.example.charge.event.InterAdCDTimeEvent
import com.example.charge.init.FpManger
import com.example.charge.init.InitManager
import com.example.charge.utils.CustomTimer
import com.example.charge.utils.NetworkHelper
import com.example.charge.utils.SpUtil
import com.example.charge.utils.log
import com.example.charge.withdraw.WithdrawHelper
import org.greenrobot.eventbus.EventBus

@SuppressLint("StaticFieldLeak")
object ChargeApp {
    lateinit var instance: Application
    val minuteTimer by lazy {
        CustomTimer().apply {
            setInterval(60 * 1000L)
            setTickCallback {
                WithdrawHelper.updateWaitingPlayer()
            }
        }
    }

    val interAdTimer by lazy {
        CustomTimer().apply {
            var timeSecond = FpManger.chargeConfig.interAdTime
            if (timeSecond <= 0) {
                timeSecond = 90
            }
            setInterval(timeSecond * 1000L)
            setTickCallback {
                if (App.DEBUG) {
                    log(LogTag.INTER_AD_CD, "插屏cd时间到")
                }
                EventBus.getDefault().post(InterAdCDTimeEvent())
            }
        }
    }

    private var isInit = false

    fun init() {
        if (isInit) {
            return
        }
        instance = App.app
        SpUtil.Companion.init(App.context)
        InitManager.init(App.app)
        App.app.registerActivityLifecycleCallbacks(AppLifecycleCallback())
        NetworkHelper.setupNetworkCallback()
        isInit = true
    }
}