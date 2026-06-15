package com.example.charge.data

import androidx.annotation.Keep

@Keep
data class Withdraw(val limit: Int, val showLineUpNum: Int) {
}