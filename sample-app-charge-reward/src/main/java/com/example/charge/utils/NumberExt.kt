package com.example.charge.utils

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.LocaleList
import android.text.TextUtils
import com.example.charge.init.InitManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

//float的扩展函数，作用保留小数点后两位
fun Float.format2f(): Float {
    val df = DecimalFormat("#.##")
    df.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    return df.format(this).toFloat()
}

//float的扩展函数，作用保留小数点后4位
fun Float.format4f(): Float {
    val df = DecimalFormat("#.####")
    df.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    return df.format(this).toFloat()
}

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
    return if (result.isSuccess) result.getOrNull() else null
}

fun Double.roundToDecimalPlaces(scale: Int = 2): Double {
    return BigDecimal(this)
        .setScale(scale, RoundingMode.HALF_UP)
        .toDouble()
}

//用于去掉无效的小数点  比如12.0 13.00
fun Double.formatDouble(): String {
    return formatForCountry(LocaleList.getDefault()[0], this, false)
//    val format = DecimalFormat("#.##")
//    return format.format(this)
}

fun Float.formatFloat(): String {
    return formatForCountry(LocaleList.getDefault()[0], this, false)
}

fun Float.formatFloat(maximumFractionDigits: Int): Float {
    return formatForCountry(
        LocaleList.getDefault()[0],
        this,
        false,
        maximumFractionDigits
    ).toFloat()
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
private fun formatForCountry(
    locale: Locale,
    number: Number,
    isGroupingUsed: Boolean,
    maximumFractionDigits: Int = 2
): String {
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

/**
 * 获取 [from, to] 闭区间的随机 Float
 * 参考 Random.nextInt(from, to) 的实现
 * returns: 随机生成的 Float 值（保留两位小数）
 */
fun Random.nextFloat(from: Float, to: Float): Float {
    require(from <= to) { "from ($from) must be less than or equal to to ($to)." }

    return when {
        from == to -> roundToTwoDecimals(from)
        from.isInfinite() || to.isInfinite() -> roundToTwoDecimals(handleInfiniteRange(from, to))
        from.isNaN() || to.isNaN() -> Float.NaN
        else -> {
            // 基础实现：生成 [0, 1) 的随机数并映射到 [from, to]
            val randomValue = nextFloat()
            val value = from + randomValue * (to - from)
            roundToTwoDecimals(value).coerceIn(from, to)
        }
    }
}

/**
 * 处理无限范围的随机数生成
 */
private fun Random.handleInfiniteRange(from: Float, to: Float): Float {
    return when {
        from.isInfinite() && to.isInfinite() -> {
            when {
                from == Float.NEGATIVE_INFINITY && to == Float.POSITIVE_INFINITY -> {
                    // [-∞, +∞] 返回有限范围内的随机数
                    nextFloat(-Float.MAX_VALUE, Float.MAX_VALUE)
                }
                from == Float.POSITIVE_INFINITY && to == Float.POSITIVE_INFINITY -> Float.POSITIVE_INFINITY
                from == Float.NEGATIVE_INFINITY && to == Float.NEGATIVE_INFINITY -> Float.NEGATIVE_INFINITY
                else -> throw IllegalArgumentException("Invalid infinite range: $from to $to")
            }
        }
        from.isInfinite() -> {
            if (from == Float.NEGATIVE_INFINITY) {
                // [-∞, to]
                nextFloat(-Float.MAX_VALUE, to)
            } else {
                // [+∞, to] - 只有在 to 也是 +∞ 时有效
                if (to == Float.POSITIVE_INFINITY) Float.POSITIVE_INFINITY
                else throw IllegalArgumentException("Invalid range: $from to $to")
            }
        }
        to.isInfinite() -> {
            if (to == Float.POSITIVE_INFINITY) {
                // [from, +∞]
                nextFloat(from, Float.MAX_VALUE)
            } else {
                // [from, -∞] - 只有在 from 也是 -∞ 时有效
                if (from == Float.NEGATIVE_INFINITY) Float.NEGATIVE_INFINITY
                else throw IllegalArgumentException("Invalid range: $from to $to")
            }
        }
        else -> throw IllegalStateException("Unexpected infinite range handling")
    }
}

/**
 * 将浮点数四舍五入到一位小数
 */
private fun roundToOneDecimal(value: Float): Float {
    return (value * 10).roundToInt() / 10.0f
}

/**
 * 将浮点数四舍五入到两位小数
 */
private fun roundToTwoDecimals(value: Float): Float {
    return (value * 100).roundToInt() / 100.0f
}


