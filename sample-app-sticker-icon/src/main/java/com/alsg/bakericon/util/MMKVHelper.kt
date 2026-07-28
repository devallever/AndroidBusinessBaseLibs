package com.alsg.bakericon.util

import android.content.Context
import app.allever.android.lib.core.store.StoreCore

/**
 *@Description
 *@author: zq
 *@date: 2024/1/15
 */
object MMKVHelper {
    fun init(context: Context) {

    }

    fun putString(key: String, value: String?) {
        StoreCore.putString(key, value)
    }

    fun getString(key: String, default: String = ""):String {
        return StoreCore.getString(key, default)?:""
    }
}