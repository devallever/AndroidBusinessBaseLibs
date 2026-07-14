package com.allever.android.card.text.pic.text.util

import android.util.Log
import app.allever.android.lib.core.app.App

fun log(msg: String) {
    if (App.DEBUG) {
        Log.d("TextCardApp", msg)
    }
}