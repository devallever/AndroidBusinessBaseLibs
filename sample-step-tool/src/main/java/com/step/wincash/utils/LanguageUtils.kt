package com.step.wincash.utils

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

object LanguageUtils {
    private const val LANGUAGE_KEY = "app_language_key"
    private const val DEFAULT_LANGUAGE = ""

    /**
     * 保存语言设置
     */
    fun saveLanguage(context: Context, language: String) {
        SpUtil.put(LANGUAGE_KEY, language)
    }

    /**
     * 获取保存的语言设置
     */
    fun getSavedLanguage(): String {
        return SpUtil.get(LANGUAGE_KEY, DEFAULT_LANGUAGE)
    }

    /**
     * 设置应用语言
     */
    fun setAppLanguage(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    /**
     * 初始化语言设置
     */
    fun initLanguage(context: Context): Context {
        val savedLanguage = getSavedLanguage()
        return if (savedLanguage.isNotEmpty()) {
            updateResources(context, savedLanguage)
        } else {
            context
        }
    }

    /**
     * 更新Resources配置
     */
    private fun updateResources(context: Context, language: String): Context {
        val locale = when {
            language.isEmpty() -> {
                // 使用系统默认语言
                Locale.getDefault()
            }
            language.contains("-") -> {
                // 处理带地区的语言代码，如zh-CN
                val parts = language.split("-")
                if (parts.size >= 2) {
                    Locale(parts[0], parts[1])
                } else {
                    Locale(language)
                }
            }
            else -> {
                // 处理只有语言代码的情况，如en
                Locale(language)
            }
        }

        // 设置为默认Locale，这很重要
        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesNewApi(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
    }

    /**
     * Android N及以上版本设置语言
     */
    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesNewApi(context: Context, locale: Locale): Context {
        // 获取当前配置并创建副本
        val configuration = Configuration(context.resources.configuration)
        
        // 设置Locale - 针对不同Android版本的兼容处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        // 设置LocaleList并设为默认
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        
        // 创建新的Context以应用配置更改
        val contextWithLocale = context.createConfigurationContext(configuration)
        
        // 额外的兼容处理：更新应用全局Resources
        if (context is Application) {
            try {
                val resources = context.resources
                val configurationField = Resources::class.java.getDeclaredField("mConfiguration")
                configurationField.isAccessible = true
                configurationField.set(resources, configuration)
                
                // 更新Resources的displayMetrics
                resources.updateConfiguration(configuration, resources.displayMetrics)
            } catch (e: Exception) {
                // 如果反射失败，至少我们已经创建了正确的Context
            }
        }
        
        return contextWithLocale
    }

    /**
     * Android N以下版本设置语言
     */
    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        // 获取当前配置
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        // 设置Locale（旧版本API）
        configuration.locale = locale
        
        // 关键步骤：更新Resources配置
        resources.updateConfiguration(configuration, resources.displayMetrics)
        
        // 如果是Application上下文，尝试更新全局配置
        if (context is Application) {
            try {
                // 尝试通过反射获取并更新Application的Resources
                val appResources = context.resources
                appResources.updateConfiguration(configuration, appResources.displayMetrics)
            } catch (e: Exception) {
                // 忽略异常
            }
        }
        
        return context
    }

    const val EN = "en"
    const val VN = "vi-VN"
    const val TH = "th-TH"
    const val ID = "id-ID"
    const val TR = "tr-TR"
    const val KR = "ko-KR"
    const val BR = "pt-BR"
    const val PH = "tl-PH"
    const val JP = "ja-JP"
    const val CN = "zh-CN"
    const val ES = "es-ES"
    const val ZH_TW = "zh-TW"
    const val ZH_HK = "zh-HK"
    const val ZH_MO = "zh-MO"
    const val ZH_SG = "zh-SG"
    const val ZH_CN = "zh-CN"
    const val BD = "bn-BD"
    const val SYSTEM = ""
    //PK
    const val PK = "ur-PK"


    /**
     * 获取支持的语言列表
     */
    fun getSupportedLanguages(): Map<String, String> {
        return mapOf(
            "" to "跟随系统",
            "en" to "English",
            "vi-VN" to "Tiếng Việt",
            "th-TH" to "ไทย",
            "id-ID" to "Bahasa Indonesia",
            "tr-TR" to "Türkçe",
            "ko-KR" to "한국어",
            "pt-BR" to "Português (Brasil)",
            "tl-PH" to "Tagalog"
        )
    }
}