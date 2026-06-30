package com.step.wincash.business.withdraw.account

import android.text.TextUtils
import com.google.gson.Gson
import com.step.wincash.business.withdraw.PaymentName
import com.step.wincash.utils.SpUtil

object AccountManager {
    // 通过支付方式获取存储在本地的账户信息
    private val accountsMap = mutableMapOf<String, AccountBean>()
    private val sGson = Gson()

    /**
     * 获取账户对象
     */
    fun findAccountsBean(@PaymentName payment: String): AccountBean? {
        return accountsMap[payment] ?: readFromSp(payment)?.also {
            accountsMap[payment] = it
        }
    }

    fun saveAccountsBean(@PaymentName payment: String, accountsBean: AccountBean?) {
        if (TextUtils.isEmpty(payment) || accountsBean == null) {
            return
        }
        accountsMap[payment] = accountsBean
        saveForSp(payment, accountsBean)
    }

    private fun saveForSp(payment: String, accountsBean: AccountBean?) {
        val key = "account_$payment"
        if (accountsBean == null) {
            SpUtil.put(key, "")
        } else {
            SpUtil.put(key, sGson.toJson(accountsBean))
        }
    }

    private fun readFromSp(payment: String): AccountBean? {
        val key = "account_$payment"
        val value = SpUtil.get(key, "")

        if (!TextUtils.isEmpty(value)) {
            return try {
                val accountsBean = sGson.fromJson(value, AccountBean::class.java)
                // 判断账户id如果是空 说明不合法 置空
                if (accountsBean == null || TextUtils.isEmpty(accountsBean.account)) {
                    saveForSp(payment, null)
                    null
                } else {
                    accountsBean
                }
            } catch (e: Exception) {
                // 处理JSON解析异常
                saveForSp(payment, null)
                null
            }
        }
        return null
    }

    /**
     * 清除指定支付方式的账户信息
     */
    fun clearAccount(payment: String) {
        accountsMap.remove(payment)
        saveForSp(payment, null)
    }

    /**
     * 清除所有缓存的账户信息
     */
    fun clearAllCache() {
        accountsMap.clear()
    }

    /**
     * 检查是否有账户信息
     */
    fun hasAccount(payment: String): Boolean {
        return findAccountsBean(payment) != null
    }
}