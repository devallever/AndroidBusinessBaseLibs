package com.plinkopro.wincash.utils

import android.os.LocaleList
import android.text.TextUtils
import com.plinkopro.wincash.init.InitManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


fun Number.formThousand(): String {
    return formatForCountry(LocaleList.getDefault()[0], this, true)
//    val format = DecimalFormat("#,###.##")
//    return format.format(this)
}

fun String.parseInt(): Int? {
    val result = runCatching {
        this.toIntOrNull()
    }
    return if (result.isSuccess) result.getOrNull() else null
}

fun String.parseLong(): Long? {
    val result = runCatching {
        this.toLongOrNull()
    }
    return if (result.isSuccess) result.getOrNull() else null
}

fun String.parseDouble(): Double? {
    val result = runCatching {
        this.toDouble()
    }
    return if (result.isSuccess) result.getOrNull() else null
}

fun String.parseFloat(): Float? {
    val result = runCatching {
        this.toFloat()
    }
    return if(result.isSuccess) result.getOrNull() else null
}

fun Double.roundToDecimalPlaces(scale:Int = 2): Double {
    return BigDecimal(this)
        .setScale(scale, RoundingMode.HALF_UP)
        .toDouble()
}

//用于去掉无效的小数点  比如12.0 13.00
fun Double.formatDouble(): String{
    return formatForCountry(LocaleList.getDefault()[0], this, false)
//    val format = DecimalFormat("#.##")
//    return format.format(this)
}

fun Float.formatFloat(): Float{
    return formatForCountry(LocaleList.getDefault()[0], this, false).toFloat()
}

fun Float.formatFloat(maximumFractionDigits : Int): Float{
    return formatForCountry(LocaleList.getDefault()[0], this, false,maximumFractionDigits).toFloat()
}




private val local: Locale
    get() {
        val countryCode = InitManager.getCountryCode()
        /* if (TextUtils.equals("US", countryCode)) {
             return Locale.US
         }*/
        if (TextUtils.equals("VN", countryCode)) {
            return Locale("vi", "VN")
        }
        if (TextUtils.equals("TH", countryCode)) {
            return Locale("th", "TH")
        }

        if (TextUtils.equals("ID", countryCode)) {
            return Locale("id", "ID")
        }

        if (TextUtils.equals("TR", countryCode)) {
            return Locale("tr", "TR")
        }

        if (TextUtils.equals("KR", countryCode)) {
            return Locale("ko", "KR")
        }

        /* if (TextUtils.equals("BD", countryCode)) {
             return Locale("bn", "BD")
         }*/

        if (TextUtils.equals("BR", countryCode)) {
            return Locale("pt", "BR")
        }

        if (TextUtils.equals("PH", countryCode)) {
            return Locale("tl", "PH")
        }

        return Locale.US
    }

/**
 * @param isGroupingUsed  是否开启千位分隔符  true:开启  false:禁用
 * */
private fun formatForCountry(locale: Locale, number: Number, isGroupingUsed : Boolean,maximumFractionDigits: Int = 2): String {
    val decimalFormat = DecimalFormat()
    // 使用Locale.US的DecimalFormatSymbols确保使用阿拉伯数字(0-9)
    val symbols = DecimalFormatSymbols(Locale.US)
    
    // 保留原始locale的小数点和千位分隔符，但使用阿拉伯数字
    if (locale != Locale.US) {
        val originalSymbols = DecimalFormatSymbols(locale)
        // 保留原始地区的小数点和千位分隔符
        symbols.decimalSeparator = originalSymbols.decimalSeparator
        symbols.groupingSeparator = originalSymbols.groupingSeparator
    }
    
    decimalFormat.decimalFormatSymbols = symbols  // 设置格式化使用的符号（如小数点、千位分隔符等）
    decimalFormat.isGroupingUsed = isGroupingUsed
    decimalFormat.maximumFractionDigits = maximumFractionDigits  //设置最大小数位数为2
    decimalFormat.isDecimalSeparatorAlwaysShown = false //当小数部分为0时，不显示小数点和小数位,当小数部分不为0时，正常显示
    return decimalFormat.format(number)
}

