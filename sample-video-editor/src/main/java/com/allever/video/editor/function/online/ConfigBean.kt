package com.allever.video.editor.function.online

import androidx.annotation.Keep
import com.android.absbase.helper.log.DLog
import com.google.gson.Gson
import com.allever.video.editor.utils.Base64
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

@Keep
class ConfigBean {
    companion object {

        private val TAG = ConfigBean::class.java.simpleName

        @JvmStatic
        public fun parse(path: String): ConfigBean? {
            val targetFile = File(path)
            val bufferReader = BufferedReader(InputStreamReader(FileInputStream(targetFile)))
            val stringBuilder = StringBuilder()
            var line: String? = null
            do {
                line = bufferReader.readLine()
                if (line != null) {
                    stringBuilder.append("$line")
                } else {
                    break
                }
            } while (true)

            return try {
                val result = stringBuilder.toString()
                DLog.d(TAG, result)
                val subResult = result.substring(2, result.length - 2)

//                val decodeResult = EncryptConstant.decodeBase64(subResult)
                val decodeResult = String(Base64.decode(subResult))
                DLog.d(TAG, decodeResult)
                Gson().fromJson(decodeResult, ConfigBean::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }

    /***
     * 版本号
     */
    val version: String? = null

    /***
     *
     */
    val packageName: String? = null

    /***
     * 刷新间隔，单位分钟
     */
    val time: Int = 0

    /***
     * effect_info地址
     */
    val url: String? = null

    /***
     * effect_info修改时间戳
     */
    val lastUpdateTime: Long = 0

    /***
     * 扩展
     */
    val child: List<ConfigBean>? = null
}
