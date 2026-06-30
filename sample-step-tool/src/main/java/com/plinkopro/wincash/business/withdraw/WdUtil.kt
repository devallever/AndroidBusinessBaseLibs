package com.plinkopro.wincash.business.withdraw

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.business.withdraw.api.WdRecordApi
import com.plinkopro.wincash.business.withdraw.bean.WdRecordsResult
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpKey.WITHDRAW_RECORD
import com.plinkopro.wincash.utils.SpUtil

object WdUtil {

    //保存上次选择的支付方式
    fun saveLastSelectPaymentName(@PaymentName paymentName: String) {
        SpUtil.put(SpKey.LAST_SELECT_PAYMENT_NAME, paymentName)
    }

    //获取上次选择的支付方式
    fun getLastSelectPaymentName(): String {
        return SpUtil.get(SpKey.LAST_SELECT_PAYMENT_NAME, "")
    }

    //保存提现申请状态(是否发起过一次提现)
    fun saveApplyWithdrawState(state: Boolean) {
        SpUtil.put(SpKey.APPLY_WITHDRAW_STATE, state)
    }

    //获取提现申请成功的提现记录id
    fun getWithholdWithDrawRecordIds(): MutableList<Long> {
        val recordIdsJson = SpUtil.get(SpKey.WITHHOLD_WITHDRAW_RECORD_IDS, "")
        return if (recordIdsJson.isNotEmpty()) {
            Gson().fromJson<List<Long>>(
                recordIdsJson,
                object : TypeToken<List<Long>>() {}.type
            ).toMutableList()
        } else mutableListOf()
    }

    //添加发起提现成功后，提现记录的id列表，这部分就是已经扣除金币的，需要在提现记录拉取下来后判断是否需要返回金币
    fun addWithholdWithDrawRecordIds(recordId: Long) {
        val records = getWithholdWithDrawRecordIds()
        records.add(recordId)
        saveWithholdWithDrawRecordIds(records)
    }

    //保存提现申请成功的提现记录id
    fun saveWithholdWithDrawRecordIds(records: List<Long>) {
        SpUtil.put(SpKey.WITHHOLD_WITHDRAW_RECORD_IDS, Gson().toJson(records))
    }

    fun matchWdMsgByFailCode(context: Context, failCode: String): String {
        return when (failCode) {
            "500", "509", "510", "511", "512", "513", "514", "515", "516", "517", "525", "526", "527", "528", "529", "538", "539", "541", "603",
            "OTHER_ERROR", "TRANSACTION_MINIMUM_LIMIT", "PROVIDER_REFUSED_PROCESS", "PROVIDER_FAILED_PROCESS", "PAYMENT_FAILED",
            "PAYEE_MONTHLY_AMOUNT_EXCEED_LIMIT", "PAYEE_AMOUNT_EXCEED_LIMIT", "c_error", "ACCOUNT_BLOCKED"
                -> context.getString(R.string.wd_error_msg_1)       // 系统检测到异常，提现未通过。
            "505" -> context.getString(R.string.wd_error_msg_2)
            "507" -> context.getString(R.string.wd_error_msg_3)
            "520", "521", "530", "531", "532", "533", "534", "537" -> context.getString(R.string.wd_error_msg_4)
            "522", "523", "524", "535", "536" -> context.getString(R.string.wd_error_msg_5)
            "REJECTED", "PARAMS_INVALID", "NONEXISTENT_ACCOUNT", "INVALID_ACCOUNT", "invalid parameters" -> context.getString(
                R.string.wd_error_msg_6
            )

            "INVALID_IDENTIFICATION", "invalid document_id" -> context.getString(R.string.wd_error_msg_7)
            "INVALID_EMAIL" -> context.getString(R.string.wd_error_msg_8)
            "invalid pix account" -> context.getString(R.string.wd_error_msg_9)
            "INVALID_PAYEE_NAME" -> context.getString(R.string.wd_error_msg_10)
            else -> context.getString(R.string.wd_error_msg_1)
        }
    }

}
























