package com.allever.android.card.text.pic.text.util

import android.util.Log
import com.allever.android.card.text.pic.text.BuildConfig

fun log(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d("TextCardApp", msg)
    }
}