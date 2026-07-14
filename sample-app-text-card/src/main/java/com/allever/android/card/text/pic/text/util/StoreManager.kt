package com.allever.android.card.text.pic.text.util

object StoreManager {

    fun putString(key: String, value: String) {
        PreferenceStore.putData(key, value)
    }

    fun getString(key: String, default: String = ""): String {
        return PreferenceStore.getData(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        PreferenceStore.putData(key, value)
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return PreferenceStore.getData(key, default)
    }

    fun putInt(key: String, value: Int) {
        PreferenceStore.putData(key, value)
    }

    fun getInt(key: String, default: Int = 0): Int {
        return PreferenceStore.getData(key, default)
    }
}