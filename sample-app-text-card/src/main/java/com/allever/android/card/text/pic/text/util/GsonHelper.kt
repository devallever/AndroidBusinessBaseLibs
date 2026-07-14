package com.allever.android.card.text.pic.text.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object GsonHelper {
    private val mGson = Gson()
    fun toJson(obj: Any?): String {
        return try {
            mGson.toJson(obj)
        } catch (e: Exception) {
            e.message ?: ""
        }
    }

    fun <T> fromJson(json: String) {
        val listType: Type = object : TypeToken<List<T?>?>() {}.type
        val people: List<T> = mGson.fromJson(json, listType)

    }

    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        try {
            return mGson.fromJson(json, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getGson(): Gson {
        return mGson
    }
}