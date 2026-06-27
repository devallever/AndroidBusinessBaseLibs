package com.clean.wood.utils

import android.util.Log
import android.widget.Toast
import com.clean.wood.WoodApp

private val TAG = "WoodApp"

fun log(msg: String) {
    Log.d(TAG, msg)
}

fun loge(msg: String) {
    Log.e(TAG, msg)
}

fun toast(msg: String) {
    Toast.makeText(WoodApp.context, msg, Toast.LENGTH_SHORT).show()
}