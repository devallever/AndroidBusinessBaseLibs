package com.step.wincash.base

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.net.Network
import android.os.Build
import android.os.Looper
import android.os.Process
import android.webkit.WebView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.CoroutineHelper
import com.carefree.steplib.utils.StepTracker
import com.step.wincash.R
import com.step.wincash.business.withdraw.WithdrawBusiness
import com.step.wincash.event.AdDismissEvent
import com.step.wincash.init.FpManger
import com.step.wincash.utils.LanguageUtils
import com.step.wincash.utils.SpUtil
import com.step.wincash.init.InitManager
import com.step.wincash.utils.LogUtil
import com.step.wincash.utils.NetworkHelper
import com.step.wincash.utils.TimerUtil
import com.step.wincash.utils.log
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

object BaseApplication {

    lateinit var instance: Application
    val timer by lazy { TimerUtil() }

    var isInit = false

    private fun attachBaseContext(base: Context) {
        SpUtil.init(base)
        // 初始化语言设置
        val context = LanguageUtils.initLanguage(base)
        LanguageUtils.setAppLanguage(context, LanguageUtils.SYSTEM) //手动修改语言
    }

    fun onCreate() {
        if (isInit) {
             return
        }
        isInit = true
        attachBaseContext(App.context)
        // 为不同进程设置不同的WebView数据目录，避免多进程冲突
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            if (processName != null && processName != App.context.packageName) {
//                log("非主进程")
                //设置数据目录后缀：为非主进程的WebView调用WebView.setDataDirectorySuffix(processName)，将当前进程名设置为其数据目录的后缀
                WebView.setDataDirectorySuffix(processName.replace(":", ""))
            }
        }

        MMKV.initialize(App.context)
        if (isMainProcess()) {
            if (App.DEBUG) {
                log("主进程 初始化sdk")
            }
            instance = App.app
            SpUtil.init(App.context)
            InitManager.init(App.app)
            App.app.registerActivityLifecycleCallbacks(AppLifecycleCallback())
            NetworkHelper.setupNetworkCallback()
//            initStepLib()
            timer.setTickCallback {
                WithdrawBusiness.updateRank()
            }
            FpManger.stepConfig
        } else {
//            initStepLibConfig()
        }
    }

    private fun initStepLib() {
        StepTracker.initialize(
            App.app, StepTracker.Config(
                App.context.getString(R.string.step_notification_title),
                App.context.getString(R.string.step_notification_message)
            ), App.DEBUG
        )
        // 启动计步服务
        StepTracker.startTrackingService()
    }

    private fun initStepLibConfig() {
        StepTracker.notificationConfig = StepTracker.Config(
            App.context.getString(R.string.step_notification_title),
            App.context.getString(R.string.step_notification_message)
        )
    }

    private fun isMainProcess(): Boolean {
        val manager = App.context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = manager.runningAppProcesses.find {
            it.pid == Process.myPid()
        }
        val isMainProcess = info?.processName == App.context.packageName

        if (App.DEBUG) {
            log("进程：${info?.processName} 是否主进程，$isMainProcess")
        }
        return isMainProcess
    }

    fun postAdDismissEvent(adIndex: Int) {
        CoroutineHelper.MAIN.launch {
            delay(0)
            EventBus.getDefault().post(AdDismissEvent(adIndex))
        }
    }
}