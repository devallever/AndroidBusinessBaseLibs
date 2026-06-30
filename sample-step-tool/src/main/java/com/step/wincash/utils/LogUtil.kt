package com.step.wincash.utils

import android.util.Log

object LogUtil {

    fun ad(message : String){
        Log.d("ad_sdk",message)
    }

    fun local(message : String){
        Log.d("local_data",message)
    }

    fun fp(message : String){
         Log.d("fp",message)
    }

    fun fpError(message : String){
        Log.e("fp",message)
    }
}