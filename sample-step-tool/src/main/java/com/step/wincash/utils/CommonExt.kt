package com.step.wincash.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.step.wincash.base.BaseApplication
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern

fun String.regexEmail(): Boolean {
    return Pattern.matches("^\\w+(-+.\\w+)*@\\w+(-.\\w+)*.\\w+(-.\\w+)*\$", this)
}

fun String.isAlphanumeric(): Boolean {
    return Pattern.matches("^[a-zA-Z0-9]*\$", this)
}

fun Activity?.isAlive(): Boolean {
    return this != null && !this.isFinishing && !this.isDestroyed
}

fun Fragment.isAlive(): Boolean {
    return activity.isAlive() && isAdded && !isDetached
}

fun Activity?.isSelfClass(): Boolean = this?.javaClass?.name?.startsWith("com.step.wincash")==true


fun openUrl(context: Context, url: String?) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        val activityInfo = intent.resolveActivityInfo(context.packageManager, 0)
        if (activityInfo.exported) {
            intent.setData(Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }
    } catch (e: Exception) {
    }
}

fun openBrowser(context: Context, url: String?) {
    try {
        if (isAppInstalled(context, "com.android.chrome")) {
            // 创建一个 Intent，指定 ACTION_VIEW 动作和 URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            // 指定要使用的浏览器的包名
            intent.setPackage("com.android.chrome") // Chrome 浏览器的包名
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 启动 Chrome 浏览器来处理链接
            context.startActivity(intent)
        } else {
            // 如果没有安装 Chrome 浏览器，使用系统默认浏览器打开链接
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 启动默认浏览器来处理链接
            context.startActivity(intent)
        }
    } catch (e: Exception) {
    }
}

private fun Context.openMarket(marketPkg: String?, marketPath: String?): Boolean {
    if (marketPkg.isNullOrEmpty() || marketPath.isNullOrEmpty() || !marketPath.startsWith("market://details?")) {
        return false
    }

    return try {
        val intent = Intent(Intent.ACTION_VIEW, marketPath.toUri()).apply {
            setPackage(marketPkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun openIntent(url: String, context: Context): Boolean {
    if (url.startsWith("market:")
        || url.startsWith("https://play.google.com/store")
        || url.startsWith("http://play.google.com/store")
    ) {
        HandlerUtil.main().post({
            try {
                if (url.startsWith("market://details?id=")) {
                    if (!context.openMarket("com.android.vending", url)){
                        openBrowser(context, url.replace("market://details", "https://play.google.com/store/apps/details"))
                    }
                }else {
                    openBrowser(context, url)
                }
            } catch (e: Exception) {
            }
        })

        return true
    } else if (url.contains("lz_open_browser=1") || url.endsWith(".apk")) {
        HandlerUtil.main().post({ openBrowser(context,url) })
        return true
    } else if (!url.startsWith("http://")
        && !url.startsWith("https://")
    ) {
        HandlerUtil.main().post({ openUrl(context,url) })
        return true
    }
    return false
}

fun String.getHostSafe(): String {
    return try {
        URL(this).host
    } catch (e: MalformedURLException) {
        //println("无效的 URL: $this，错误: ${e.message}")
        this
    }
}

fun isAppInstalled(context: Context, packageName: String): Boolean {
    if (packageName.isEmpty()) {
        return false
    }
    try {
        val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
        return applicationInfo.enabled // 检查应用是否启用
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }
}


/**
 * 获取国家
 */
fun getCountry(context: Context): String {
    return try {
        var country = context.resources.configuration.locales[0].country
        if (TextUtils.isEmpty(country)) {
            country = Locale.getDefault().country
        }
        country
    } catch (e: Throwable) {
        "UNKNOWN"
    }
}


fun generateId(): String {
    val uppercaseLetters = ('A'..'Z').toList()
    val digits = ('0'..'9').toList()

    val letters = (1..3).map { uppercaseLetters.random() }
    val numbers = (1..3).map { digits.random() }

    return (letters + numbers).joinToString("")
}

fun readJsonFromRaw(resourceId: Int): String {
    return try {
        val inputStream = BaseApplication.instance.resources.openRawResource(resourceId)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer, Charsets.UTF_8)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
/**
 * 网络是否连接
 * */
fun isNetworkAvailable() : Boolean {
    var result = false
    val connectivityManager = BaseApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    result = when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
    return result

}

/**
 * 严格版本：检查是否为有效的空JSON对象
 */
fun String?.isStrictEmptyJsonObject(): Boolean {
    if (this.isNullOrEmpty()) return false

    val trimmed = this.trim()
    return trimmed == "{}" && trimmed.length == 2
}







