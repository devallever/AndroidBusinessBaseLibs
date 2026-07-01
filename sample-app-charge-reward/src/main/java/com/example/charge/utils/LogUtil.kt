package com.example.charge.utils

import android.util.Log

object LogUtil {

    fun ad(message: String) {
        Log.d("ad_sdk", message)
    }

    fun local(message: String) {
        Log.d("local_data", message)
    }

    fun fp(message: String) {
        Log.d("fp", message)
    }

    fun fpError(message: String) {
        Log.e("fp", message)
    }

    fun hitMole(message: String) {
        Log.d("hitMole", message)
    }

    fun receiveCoin(message: String) {
        Log.d("receiveCoin", message)
    }

    fun  showInterAd(message: String){
        Log.d("InterAdCD", message)
    }

}