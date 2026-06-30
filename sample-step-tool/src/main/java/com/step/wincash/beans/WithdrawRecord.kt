package com.step.wincash.beans

import androidx.annotation.Keep
import com.step.wincash.business.withdraw.WithdrawBusiness

@Keep
data class WithdrawRecord(
    var rank: Int,
    val time: Long,
    val amount: Int,
    val countryCode: String,
    val currencyType: Int,
    val level: Int,
    val paymentName: String,
    val endRank: Int = WithdrawBusiness.createRankEndPoint(),
    var finish: Boolean = false
) {

}