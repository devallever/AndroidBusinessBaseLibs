package com.funny.gif.memes.func.store

import app.allever.android.lib.core.store.StoreCore


object Store {

    fun saveToken(token: String) {
        StoreCore.putString(CacheKey.TOKEN, token)
    }

    fun getToken(): String = StoreCore.getString(CacheKey.TOKEN)?:""

    fun saveUserId(userId: Int) {
        StoreCore.putInt(CacheKey.USER_ID, userId)
    }

    fun savePhone(token: String) {
        StoreCore.putString(CacheKey.PHONE, token)
    }

    fun getPhone(): String = StoreCore.getString(CacheKey.PHONE)?:""

    fun getUserId(): Int = StoreCore.getInt(CacheKey.USER_ID, 0)

    fun saveVersion(version: Int) {
        StoreCore.putInt("version", version)
    }

    fun getVersion() = Version.INTERNATIONAL

}