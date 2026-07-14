package com.allever.android.card.text.pic.text.util

import app.allever.android.lib.core.store.StoreCore

object StoreManager {

    fun putString(key: String, value: String) {
        StoreCore.putString(key, value)
    }

    fun getString(key: String, default: String = ""): String {
        return StoreCore.getString(key, default)?:""
    }

    fun putBoolean(key: String, value: Boolean) {
        StoreCore.putBoolean(key, value)
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return StoreCore.getBoolean(key, default)
    }

    fun putInt(key: String, value: Int) {
        StoreCore.putInt(key, value)
    }

    fun getInt(key: String, default: Int = 0): Int {
        return StoreCore.getInt(key, default)
    }
}