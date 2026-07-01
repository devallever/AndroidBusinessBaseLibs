package app.flash.tunnel.vpn.lib.common.util

import app.flash.tunnel.vpn.lib.common.store.PreferenceStore

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