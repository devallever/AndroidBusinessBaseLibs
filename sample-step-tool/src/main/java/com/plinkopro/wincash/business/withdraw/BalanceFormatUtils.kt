package com.plinkopro.wincash.business.withdraw

import android.os.LocaleList
import android.text.TextUtils
import com.plinkopro.wincash.init.InitManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object BalanceFormatUtils {
    fun getFormatBalance(balance: Float): String {
//        return formatForCountry(local, balance)
        return formatForCountry(balance)
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

            return LocaleList.getDefault()[0]
        }

    private fun formatForCountry(locale: Locale, number: Float): String {
        val symbols = DecimalFormatSymbols(locale)
        val decimalFormat = DecimalFormat()
        decimalFormat.decimalFormatSymbols = symbols
        decimalFormat.maximumFractionDigits = 2
        return decimalFormat.format(number)
    }

    private fun formatForCountry(number: Float): String {
        val symbols = DecimalFormatSymbols(LocaleList.getDefault()[0])
        val decimalFormat = DecimalFormat()
        decimalFormat.decimalFormatSymbols = symbols
        decimalFormat.maximumFractionDigits = 2
        return decimalFormat.format(number)
    }
}
