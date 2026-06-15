package com.example.charge.utils

import android.util.Log


private val TAG = "ChargeEarning"
fun log(message: String) {
    log(TAG, message)
}

fun log(tag: String, message: String) {
    Log.d(tag, message)
}
