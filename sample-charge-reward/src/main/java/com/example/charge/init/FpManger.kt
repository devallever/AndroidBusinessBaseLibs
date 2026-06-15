package com.example.charge.init

import android.content.Context
import com.example.charge.ChargeApp
import app.allever.android.lib.core.app.App
import com.example.charge.data.ChargeConfig
import com.example.charge.utils.LogUtil
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import com.example.charge.utils.isStrictEmptyJsonObject
import com.example.charge.utils.log
import com.google.gson.Gson
import java.io.IOException

object FpManger {

    val gson = Gson()
    var chargeConfig = ChargeConfig()
        private set


    private var initFromRemote = false
    init {
        if (!initFromRemote) {
            initConfig()
        }
    }

    fun getWithdrawSetting(): String {
        var settingJson = SpUtil.get(SpKey.WITHDRAW_SETTING, "")
        if (settingJson.isEmpty()) {
            settingJson = readJsonFile(ChargeApp.instance, "withdraw_config.json")!!
        }
        if (App.DEBUG) {
            log("getWithdrawSetting = $settingJson")
        }
        return settingJson
    }

    fun saveWithdrawSetting(settingJson: String) {
        if (App.DEBUG) {
            log("saveWithdrawSetting = $settingJson")
        }
        if (settingJson.isStrictEmptyJsonObject()) {
            if (App.DEBUG) {
                log("saveWithdrawSetting 空，不保存")
            }
            return
        }
        initConfig(settingJson) {
            if (it) {
                initFromRemote = true
                SpUtil.put(SpKey.WITHDRAW_SETTING, settingJson)
            }
        }
    }

    private fun initConfig(configJson: String = "", finish: (success: Boolean) -> Unit =  {}) {
        val config = configJson.ifEmpty {
            getWithdrawSetting()
        }
        try {
            chargeConfig = gson.fromJson(config, ChargeConfig::class.java)

            if (App.DEBUG){
                LogUtil.fp("chargeConfig = $chargeConfig")
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