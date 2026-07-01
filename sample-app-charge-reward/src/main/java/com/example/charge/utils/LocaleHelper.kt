@file:Suppress("DEPRECATION")

package com.example.charge.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * AppLanguage：目前两种语言，如需扩展再加枚举即可
 */
enum class AppLanguage(val tag: String) {
    EN("en"),
    PT_BR("pt-BR");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            when (tag) {
                PT_BR.tag -> PT_BR
                EN.tag -> EN
                else -> EN
            }
    }
}

object LocaleManager {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANG = "key_lang"

    fun setToEnglish(activity: Activity) {
        setLanguage(activity, AppLanguage.EN)
    }

    fun setToBrazilianPortuguese(activity: Activity) {
        setLanguage(activity, AppLanguage.PT_BR)
    }

    // —— 常用辅助 —— //
    fun getSavedLanguage(context: Context): AppLanguage {
        val sp = prefs(context)
        return AppLanguage.fromTag(sp.getString(KEY_LANG, AppLanguage.EN.tag))
    }

    /**
     * 在 Activity.attachBaseContext(newBase) 里调用：
     *   super.attachBaseContext(LocaleManager.wrap(newBase))
     * 以在 Pre-Android 13 设备上让已保存语言在启动时生效。
     */
    fun wrap(base: Context): Context {
        val lang = getSavedLanguage(base)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+：使用官方 API 管理，不必额外 wrap，但保持返回 base 以简化调用方
            base
        } else {
            val locale = toLocale(lang)
            val res = base.resources
            val config = res.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            base.createConfigurationContext(config)
        }
    }

    // —— 内部实现 —— //

    private fun setLanguage(activity: Activity, lang: AppLanguage) {
        saveLanguage(activity, lang)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 2a) Android 13+：官方 per-app locale
            val list = LocaleListCompat.forLanguageTags(lang.tag)
            AppCompatDelegate.setApplicationLocales(list)
            // 通常系统会自动重建当前可见 Activity；为了更“立刻”，我们仍手动刷新当前页面
            forceRecreate(activity)

        } else {
            // 2b) Android 7.0–12：手动更新
            val locale = toLocale(lang)
            Locale.setDefault(locale)

            // 更新 ApplicationContext（供后续新建组件用）
            val appCtx = activity.applicationContext
            val appConfig = appCtx.resources.configuration
            appConfig.setLocale(locale)
            appConfig.setLayoutDirection(locale)
            @Suppress("UnusedPrivateMember")
            val _ignored = appCtx.createConfigurationContext(appConfig)

            // 3) 立刻刷新当前 Activity
            forceRecreate(activity)
        }
    }

    private fun saveLanguage(context: Context, lang: AppLanguage) {
        prefs(context).edit().putString(KEY_LANG, lang.tag).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun toLocale(lang: AppLanguage): Locale =
        when (lang) {
            AppLanguage.EN -> Locale("en")
            AppLanguage.PT_BR -> Locale("pt", "BR")
        }

    /**
     * 可靠的刷新当前 Activity：
     * - recreate() 很快，但个别场景（带 singleTask/SingleTop、深嵌套 Fragment）
     *   可能不足以清空旧上下文；这里结合 finish + 启动原 Intent，更稳。
     */
    private fun forceRecreate(activity: Activity) {
        val intent = Intent(activity, activity::class.java).apply {
            putExtras(activity.intent ?: Intent())
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.finish()
        activity.startActivity(intent)
    }
}
