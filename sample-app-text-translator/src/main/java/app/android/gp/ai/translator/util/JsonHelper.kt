package app.android.gp.ai.translator.util

import app.woejt.wwzdndgl.lib.util.logRandomString
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
        logRandomString()
        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}