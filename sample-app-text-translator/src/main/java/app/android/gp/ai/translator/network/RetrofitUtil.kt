package app.android.gp.ai.translator.network

import app.android.gp.ai.translator.bean.TranslationBean
import app.android.gp.ai.translator.util.MD5
import app.woejt.wwzdndgl.lib.util.log
import app.woejt.wwzdndgl.lib.util.logRandomString
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Allever on 2017/1/15.
 */

object RetrofitUtil {
    private var BASE_URL = ""
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService

    fun init(baseUrl: String) {
        logRandomString()
        BASE_URL = baseUrl
        logRandomString()
        val client = OkHttpClient.Builder()
            .connectTimeout(5000, TimeUnit.SECONDS)
            .build()
        logRandomString()
        retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        logRandomString()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    fun translate(
        subscriber: Subscriber<TranslationBean>,
        content: String,
        sl: String,
        tl: String
    ) {
        logRandomString()
        retrofitService.translate(content, sl = sl, tl = tl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }

    fun translateBaidu(
        subscriber: Subscriber<TranslationBean>,
        content: String,
        sl: String,
        tl: String
    ) {

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
        logRandomString()
        val slat = System.currentTimeMillis().toString()
        val secert = "rJwc7ZcutAnfe14Cjfrd"
        logRandomString()
        val signString = "${appid}${content}${slat}${secert}"
        log("signString = $signString")
        logRandomString()
        val sign = MD5.getMD5StrToLowerCase(signString)
        retrofitService.translateBaidu(
            q = content,
            from = sl,
            to = tl,
            appid = appid,
            slat = slat,
            sign = sign
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(subscriber)
    }

    fun requestTTS(content: String, tl: String, callback: Callback<ResponseBody>) {
        val call =
            retrofitService.requestTTS(q = content, tl = tl, textlen = content.length.toString())
        call.enqueue(callback)
    }
}
