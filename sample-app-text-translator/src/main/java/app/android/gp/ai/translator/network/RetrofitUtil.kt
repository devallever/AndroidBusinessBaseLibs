package app.android.gp.ai.translator.network

import app.android.gp.ai.translator.bean.TranslationBean
import app.android.gp.ai.translator.util.MD5
import app.allever.android.lib.core.ext.log

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Allever on 2017/1/15.
 */

object RetrofitUtil {
    private var BASE_URL = ""
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    fun init(baseUrl: String) {
        
        BASE_URL = baseUrl
        
        val client = OkHttpClient.Builder()
            .connectTimeout(5000, TimeUnit.SECONDS)
            .build()
        
        retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    suspend fun translate(
        content: String,
        sl: String,
        tl: String
    ): TranslationBean{
        
        return retrofitService.translate(content, sl = sl, tl = tl)
    }

    suspend fun translateBaidu(
        content: String,
        sl: String,
        tl: String
    ): TranslationBean {

        /**
        q=apple
        from=en
        to=zh
        appid=2015063000000001（请替换为您的appid）
        salt=1435660288（随机码）
        平台分配的密钥: 12345678

         * 生成签名sign：
        Step1. 拼接字符串1：
        拼接appid=2015063000000001+q=apple+salt=1435660288+密钥=12345678得到字符串1：“2015063000000001apple143566028812345678”
        Step2. 计算签名：（对字符串1做MD5加密）
        sign=MD5(2015063000000001apple143566028812345678)，得到sign=f89f9594663708c1605f3d736d01d2d4
         */
        val appid = "20220204001074352"
        
        val slat = System.currentTimeMillis().toString()
        val secert = "rJwc7ZcutAnfe14Cjfrd"
        
        val signString = "${appid}${content}${slat}${secert}"
        log("signString = $signString")
        
        val sign = MD5.getMD5StrToLowerCase(signString)
        return retrofitService.translateBaidu(
            q = content,
            from = sl,
            to = tl,
            appid = appid,
            slat = slat,
            sign = sign
        )
    }

    fun requestTTS(content: String, tl: String, callback: Callback<ResponseBody>) {
        val call =
            retrofitService.requestTTS(q = content, tl = tl, textlen = content.length.toString())
        call.enqueue(callback)
    }
}
