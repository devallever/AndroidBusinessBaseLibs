package com.funny.gif.memes.util

import com.google.gson.Gson


object JsonHelper {
    /**
     * 将字符串转换为 对象
     *
     * @param json
     * @param type
     * @return
     */
    fun <T> json2Object(json: String, type: Class<T>): T? {
        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }


//    fun <T> json2ObjectArray(json: String, type: Class<T>): MutableList<T>? {
//        return try {
//            Gson().fromJson(
//                json,
//                object :
//                    TypeToken<MutableList<T>>() {}.type
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
}