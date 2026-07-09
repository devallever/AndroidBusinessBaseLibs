package app.android.gp.ai.translator.network

import app.android.gp.ai.translator.bean.TranslationBean
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Allever on 2017/1/15.
 */

interface RetrofitService {
//    @GET("translate_a/single?client=gtx&dt=t&dt=bd&dt=rm&dj=1&ie=UTF-8&oe=UTF-8&sl=auto&tl=zh-CN&hl=zh-CN&tk=&q=")
//    fun translate(@Query("q") content: String): Observable<TranslationBean>

    @GET("translate_a/single")
    suspend fun translate(
        @Query("q") q: String,
        @Query("client") content: String = "gtx",
        @Query("dt") dt: String = "t",
        @Query("dt") dt1: String = "bd",
        @Query("dt") dt2: String = "rm",
        @Query("dj") dj: String = "1",
        @Query("ie") ie: String = "UTF-8",
        @Query("oe") oe: String = "UTF-8",
        @Query("sl") sl: String = "auto",
        @Query("tl") tl: String = "en",
        @Query("hl") hl: String = "zh-CN",
        @Query("tk") tk: String = ""

    ): TranslationBean

    /**
     * q=apple&from=en&to=zh&appid=2015063000000001&salt=1435660288&sign=f89f9594663708c1605f3d736d01d2d4
     */
    @GET("api/trans/vip/translate")
    suspend fun translateBaidu(
        @Query("q") q: String,
        @Query("from") from: String = "auto",
        @Query("to") to: String = "en",
        @Query("appid") appid: String,
        @Query("salt") slat: String = "",
        @Query("sign") sign: String = ""

    ): TranslationBean

    //https://translate.google.cn/translate_tts?client=gtx&ie=UTF-8&tl=zh-CN&total=1&idx=0&textlen=2&tk=&q=设置
    @GET("translate_tts")
    fun requestTTS(
        @Query("q") q: String,
        @Query("client") content: String = "gtx",
        @Query("ie") ie: String = "UTF-8",
        @Query("tl") tl: String = "en",
        @Query("hl") hl: String = "zh-CN",
        @Query("total") total: String = "1",
        @Query("idx") idx: String = "0",
        @Query("textlen") textlen: String = "0",
        @Query("tk") tk: String = ""
    ): Call<ResponseBody>

}
