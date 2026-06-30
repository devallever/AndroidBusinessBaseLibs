package com.plinkopro.wincash.init

import android.content.Context
import app.allever.android.lib.core.app.App
import com.google.gson.Gson
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.beans.StepConfig
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.isStrictEmptyJsonObject
import java.io.IOException

object FpManger {

    private val gson = Gson()
    var stepConfig = StepConfig()
        private set


    private var initFromRemote = false
    init {
        if (!initFromRemote) {
            initConfig()
        }
    }

    fun getStepSetting(): String {
        var settingJson = SpUtil.get(SpKey.STEP_SETTING, "")
        if (settingJson.isEmpty()) {
            settingJson = readJsonFile(BaseApplication.instance, "LocalStepConfig.json")!!
            if (App.DEBUG) {
                LogUtil.fp("getStepSetting 没缓存，从本地读取 = $settingJson")
            }
        } else {
            if (App.DEBUG) {
                LogUtil.fp("getStepSetting 缓存 = $settingJson")
            }
        }
        if (App.DEBUG) {
            LogUtil.fp("getStepSetting = $settingJson")
        }
        return settingJson
    }

    fun saveStepSetting(settingJson: String) {
        if (App.DEBUG) {
            LogUtil.fp("saveStepSetting = $settingJson")
        }
        if (settingJson.isStrictEmptyJsonObject()) {
            if (App.DEBUG) {
                LogUtil.fp("saveStepSetting 空，不保存")
            }
            return
        }
        initConfig(settingJson) {
            if (it) {
                initFromRemote = true
                SpUtil.put(SpKey.STEP_SETTING, settingJson)
                if (App.DEBUG) {
                    LogUtil.fp("saveStepSetting 保存成功 $settingJson")
                }
            }
        }
    }

    private fun initConfig(configJson: String = "", finish: (success: Boolean) -> Unit =  {}) {
        val config = configJson.ifEmpty {
            getStepSetting()
        }
        try {
            stepConfig = gson.fromJson(config, StepConfig::class.java)
            WithdrawBusiness.updateConfig()

            if (App.DEBUG){
                LogUtil.fp("chargeConfig = $stepConfig")
            }
            finish.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
            finish.invoke(false)
        }
    }

    /**
     * 读取 JSON 文件并转换为字符串
     */
    fun readJsonFile(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: IOException) {
            null
        }
    }
}