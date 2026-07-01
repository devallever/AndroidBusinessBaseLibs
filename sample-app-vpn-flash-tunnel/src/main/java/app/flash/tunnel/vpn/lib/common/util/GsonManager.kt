package app.flash.tunnel.vpn.lib.common.util

import com.google.gson.Gson

object GsonManager {
    private val mGson = Gson()

    fun <T> toObj(json: String, clz: Class<T>): T? {
        try {
            val obj = mGson.fromJson(json, clz)
            return obj
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun toJson(obj: Any): String = mGson.toJson(obj)
}

fun Any.toJson() = GsonManager.toJson(this)