package app.android.gp.ai.translator.network

import app.android.gp.ai.translator.util.FileUtils
import app.android.gp.ai.translator.util.MD5
import app.woejt.wwzdndgl.lib.app.App
import app.woejt.wwzdndgl.lib.util.log
import app.woejt.wwzdndgl.lib.util.logRandomString
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object NetworkHelper {

    fun requestTTS(content: String, tl: String, callback: TTSRequestCallback?) {
        logRandomString()
        RetrofitUtil.requestTTS(content, tl, object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                logRandomString()
                log("Request tts success")
                val url = call.request().url().toString()
                log("url：$url")
                logRandomString()

                val dir = App.context.cacheDir.absolutePath
                val fileName = MD5.getMD5StrToLowerCase("$content$tl") + ".mp3"
                logRandomString()
                val file = File(dir, fileName)
                logRandomString()
                FileUtils.saveByteArray2File(response.body()?.bytes(), dir, fileName)
                logRandomString()
                if (file.exists()) {
                    callback?.onSuccess(file.absolutePath)
                } else {
                    callback?.onFail("Save file fail")
                    log("File not exist")
                }
                logRandomString()

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                log("Request tts fail")
                val url = call.request().url().toString()
                log("url：$url")
            }
        })
    }
}

public interface TTSRequestCallback {
    fun onSuccess(ttsPath: String)
    fun onFail(msg: String = "")
}