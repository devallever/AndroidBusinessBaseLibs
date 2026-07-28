package com.alsg.bakericon.util

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 *@Description
 *@author: zq
 *@date: 2024/1/15
 */
object MMKVHelper {
    fun init(context: Context) {
        MMKV.initialize(context)
    }

    private val mmkv by lazy {
        MMKV.defaultMMKV()
    }

    fun putString(key: String, value: String?) {
        mmkv.encode(key, value)
    }

    fun getString(key: String, default: String = ""):String {
        return mmkv.getString(key, default)?:""
    }
}