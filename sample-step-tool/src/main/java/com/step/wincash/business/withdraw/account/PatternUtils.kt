package com.step.wincash.business.withdraw.account

import com.step.wincash.business.withdraw.CountryUtil
import com.step.wincash.init.InitManager
import java.util.regex.Pattern

//判断账户是否可用
object PatternUtils {
    private const val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")

    private const val NUMBER_11_REGEX = "^\\d{11}$"

    private const val PHONE_REGEX = "^(9\\d{9}|[1-9]\\d{10})$"


    //是否是email
    fun isEmail(email: String?): Boolean {
        if (email == null || email.isEmpty()) {
            return false
        }
        if (email.length > 64) {
            return false
        }
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    //是否是9开头的10-11位手机号
    fun isPhoneNumber(phone: String?): Boolean {
        if (phone == null || phone.isEmpty()) {
            return false
        }
        val pattern = Pattern.compile(PHONE_REGEX)
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }

    private const val EVP_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
    private const val EVP_REGEX2 = "^[a-zA-Z0-9]{32}$"
    fun isValidEvp(evp: String?): Boolean {
        if (evp == null || evp.isEmpty()) {
            return false
        }
        val pattern = Pattern.compile(EVP_REGEX)
        val pattern2 = Pattern.compile(EVP_REGEX2)
        val matcher = pattern.matcher(evp)
        val matcher2 = pattern2.matcher(evp)
        return matcher.matches() || matcher2.matches()
    }

    //是否是11位数字
    fun isValidCpf(cpf: String?): Boolean {
        if (cpf == null || cpf.isEmpty()) {
            return false
        }
        if (cpf.length != 11) {
            return false
        }
        val pattern = Pattern.compile(NUMBER_11_REGEX)
        val matcher = pattern.matcher(cpf)
        return matcher.matches()
    }

    //是否是10位数字的papara账户
    private const val NUMBER_10_REGEX = "^\\d{10}$"

    fun isPaparaAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        if (content.length != 10) {
            return false
        }
        val pattern = Pattern.compile(NUMBER_10_REGEX)
        val matcher = pattern.matcher(content)
        return matcher.matches()
    }

    //是否是paypal
    fun isPayaplAccount(content: String?): Boolean {
        return isEmail(content)
    }

    //08开头9~14位数字
    private const val NUMBER_START_08_9_14_REGEX = "^08\\d{7,12}$"

    fun isDadaAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        if (content.length < 9 || content.length > 14) {
            return false
        }
        val pattern = Pattern.compile(NUMBER_START_08_9_14_REGEX)
        val matcher = pattern.matcher(content)
        return matcher.matches()
    }

    //邮箱或者09开头的11位数字
    private const val NUMBER_START_00_11_REGEX = "^09\\d{9}$"

    fun isLazadaAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        val email = isEmail(content)
        if (email) {
            return true
        }
        val pattern = Pattern.compile(NUMBER_START_00_11_REGEX)
        val matcher = pattern.matcher(content)
        return matcher.matches()
    }

    //0开头的10位数字
    private const val NUMBER_START_00_10_REGEX = "^0\\d{9}$"

    fun isTrueMoneyAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        val email = isEmail(content)
        if (email) {
            return true
        }
        val pattern = Pattern.compile(NUMBER_START_00_10_REGEX)
        val matcher = pattern.matcher(content)
        return matcher.matches()
    }

    //08开头，最长15位数字
    private const val NUMBER_START_08_max_15_REGEX = "^08\\d{0,13}$"

    fun isShoppeayAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        val email = isEmail(content)
        if (email) {
            return true
        }
        val pattern = Pattern.compile(NUMBER_START_08_max_15_REGEX)
        val matcher = pattern.matcher(content)
        return matcher.matches()
    }

    //84开头11位数字
    private const val NUMBER_START_84_11_REGEX = "^84\\d{9}$"
    fun isZalopayAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        val email = isEmail(content)
        if (email) {
            return true
        }
        val pattern = Pattern.compile(NUMBER_START_84_11_REGEX)
        val matcher = pattern.matcher(content)
        return matcher.matches()
    }

    fun isBankCardAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        if (content.length > 50) {
            return false
        }
        for (c in content.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false
            }
        }
        return true
    }

    fun isBkashAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        if (!content.startsWith("01")) {
            return false
        }
        return content.length == 11
    }

    fun isEasyPaisaAccount(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        if (!content.startsWith("03")) {
            return false
        }
        return content.length == 11
    }

    fun isPhoneFeeAccount(content: String?): Boolean{
        if (content.isNullOrEmpty()) {
            return false
        }

        return when(InitManager.getCountryCode()){
            CountryUtil.NG -> content.length == 10
            CountryUtil.UZ ,CountryUtil.ZA -> content.length == 9
            else -> isBkashAccount(content)
        }

    }
}
