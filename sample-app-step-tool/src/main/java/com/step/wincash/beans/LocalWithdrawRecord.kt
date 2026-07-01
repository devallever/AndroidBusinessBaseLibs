package com.step.wincash.beans

import androidx.annotation.Keep

@Keep
data class LocalWithdrawRecord(val list: MutableList<WithdrawRecord> = mutableListOf()) {

}