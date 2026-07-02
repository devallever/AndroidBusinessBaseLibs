package com.funny.gif.memes.func.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GifFunServiceCreator {

    //http://116.196.106.84/login/fetch_verify_code
    private var BASE_URL = "http://api.quxianggif.com"

    private val mRetrofit by lazy {

        val builder = OkHttpClient.Builder()
        //日志拦截
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        //请求头拦截
//        builder.addInterceptor(HttpHeaderInterceptor())
        Retrofit.Builder()
            .client(builder.build())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> create(apiServiceClass: Class<T>): T = mRetrofit.create(apiServiceClass)

}