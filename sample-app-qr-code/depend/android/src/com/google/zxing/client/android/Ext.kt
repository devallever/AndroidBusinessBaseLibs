package com.google.zxing.client.android

import android.util.Log

fun DLog(msg: String) {
    Log.d("qrlogger", "DLog: ")
}

fun DLog(tag: String, msg: String) {
    Log.d(tag, "DLog: ")
}