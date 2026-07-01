package com.example.charge.utils

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.net.toUri
import app.allever.android.lib.core.app.App

/**
 * @classDes:
 * @author: 稻谷
 * @create date: 2025/8/7 15:37
 */
class WebJsOK(val context: Context, val call: ((String) -> Unit) = {}) {

    private val isHw by lazy { "huawei".equals(Build.MANUFACTURER, ignoreCase = true) }

    @JavascriptInterface
    fun openBrowser(url: String) {
        val intent = try {
            when {
                url.startsWith("intent") -> Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                else -> Intent(Intent.ACTION_VIEW, url.toUri())
            }.apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                setComponent(null)
                flags = FLAG_ACTIVITY_NEW_TASK
                if (isHw) {
                    setPackage(getDefaultBrowser())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // 可选：记录日志
            if (App.DEBUG) {
                Log.d("OKSPIN_LOG", "广告回调 openBrowser intent failed")
            }
            call.invoke("0")
            return
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            if (App.DEBUG) {
                Log.d("OKSPIN_LOG", "广告回调 openBrowser startActivity failed")
            }
            call.invoke("0")
        }
    }


    @JavascriptInterface
    fun close() {
        if (App.DEBUG) {
            Log.d("OKSPIN_LOG", "广告回调 close")
        }
        call.invoke("1")
    }

    private fun getDefaultBrowser(): String? {
        val browserIntent = Intent("android.intent.action.VIEW", "https://".toUri())
        // 1. 先尝试获取默认浏览器
        val resolveInfo = context.packageManager.resolveActivity(
            browserIntent, PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfo?.activityInfo?.packageName != null && resolveInfo.activityInfo.packageName != "android") {
            return resolveInfo.activityInfo.packageName
        }

        // 2. 查询所有浏览器，按优先级返回（系统浏览器 > 用户浏览器）
        val resolveInfos = context.packageManager.queryIntentActivities(browserIntent, 0)
        var systemBrowser: String? = null
        var userBrowser: String? = null

        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            if (info.activityInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                systemBrowser = packageName
            } else {
                userBrowser = packageName
            }
        }

        return systemBrowser ?: userBrowser
    }

}