package com.plinkopro.wincash.utils

import android.util.Log

private const val TAG = "StepTool"
fun log(msg: String) {
    log(TAG, msg)
}

fun log(tag: String, msg: String) {
    Log.d(tag, msg)
}

