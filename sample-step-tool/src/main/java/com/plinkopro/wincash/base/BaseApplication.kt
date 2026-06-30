package com.plinkopro.wincash.base

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.Network
import android.os.Build
import android.os.Looper
import android.os.Process
import android.webkit.WebView
import com.carefree.steplib.utils.StepTracker
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
import com.plinkopro.wincash.init.FpManger
import com.plinkopro.wincash.utils.LanguageUtils
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.utils.NetworkHelper
import com.plinkopro.wincash.utils.TimerUtil
import com.plinkopro.wincash.utils.log
import com.tencent.mmkv.MMKV

class BaseApplication: Application() {

    companion object {
        lateinit var instance: BaseApplication
        val timer by lazy { TimerUtil() }
    }

    override fun attachBaseContext(base: Context) {
        if (BuildConfig.LOG_OUTPUT) {
            SpUtil.init(base)
            // 初始化语言设置
            val context = LanguageUtils.initLanguage(base)
            LanguageUtils.setAppLanguage(context, LanguageUtils.SYSTEM) //手动修改语言
            return super.attachBaseContext(context)
        } else {
            super.attachBaseContext(base)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 为不同进程设置不同的WebView数据目录，避免多进程冲突
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (processName != null && processName != applicationContext.packageName) {
//                log("非主进程")
                //设置数据目录后缀：为非主进程的WebView调用WebView.setDataDirectorySuffix(processName)，将当前进程名设置为其数据目录的后缀
                WebView.setDataDirectorySuffix(processName.replace(":", ""))
            }
        }

        MMKV.initialize(this)
        if (isMainProcess()){
            if (BuildConfig.LOG_OUTPUT){
                log("主进程 初始化sdk")
            }
            instance = this
            SpUtil.init(this)
            InitManager.init(this)
            registerActivityLifecycleCallbacks(AppLifecycleCallback())
            NetworkHelper.setupNetworkCallback()
            initStepLib()
            timer.setTickCallback {
                WithdrawBusiness.updateRank()
            }
            FpManger.stepConfig
        } else {
            initStepLibConfig ()
        }
    }

    private fun initStepLib() {
        StepTracker.initialize(
            this,
            StepTracker.Config(getString(R.string.step_notification_title), getString(R.string.step_notification_message)),
            BuildConfig.LOG_OUTPUT
        )
        // 启动计步服务
        StepTracker.startTrackingService()
    }

    private fun initStepLibConfig() {
        StepTracker.notificationConfig =StepTracker.Config(getString(R.string.step_notification_title), getString(R.string.step_notification_message))
    }

    private fun isMainProcess(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = manager.runningAppProcesses.find {
            it.pid == Process.myPid()
        }
        val isMainProcess = info?.processName == packageName

        if (BuildConfig.LOG_OUTPUT) {
            log("进程：${info?.processName} 是否主进程，$isMainProcess")
        }
        return isMainProcess
    }
}