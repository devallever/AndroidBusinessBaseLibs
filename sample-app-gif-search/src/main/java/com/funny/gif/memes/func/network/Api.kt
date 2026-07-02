package com.funny.gif.memes.func.network

import com.funny.gif.memes.bean.RandomResponse
import com.funny.gif.memes.bean.SearchResponse
import com.funny.gif.memes.bean.TranslateResponse
import com.funny.gif.memes.bean.TrendingResponse
import com.funny.gif.memes.func.network.response.BannerData
import com.funny.gif.memes.func.network.response.PageData
import com.xm.netmodel.impl.HttpRequestImpl
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
//import rx.Observable

interface Api {

    companion object {
        private val api by lazy {
            HttpRequestImpl.getRequest().createApi(Api::class.java)
        }

        fun getApi(vararg classes: Class<*>?): Api = if (classes.isEmpty()) {
            api
        } else {
            HttpRequestImpl.getRequest().createApi(Api::class.java, *classes)
        }
    }

    private val API_KEY: String
        get() = "gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8"

    //https://api.giphy.com/v1/gifs/random?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8
    @GET("gifs/random?")
    suspend fun getRandomGif(
        @Query("api_key") api_key: String = API_KEY
    ): RandomResponse

    //https://api.giphy.com/v1/gifs/random?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8
    @GET("stickers/random?")
    suspend fun getRandomSticker(
        @Query("api_key") api_key: String = API_KEY
    ): RandomResponse


    //https://api.giphy.com/v1/gifs/search?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8&q=cat&offset=0&limit=1
    @GET("gifs/search?")
    suspend fun searchGif(
        @Query("q") q: String,
        @Query("api_key") api_key: String = API_KEY,
        @Query("offset") offset: String = "0",
        @Query("limit") limit: String = "10"
    ): SearchResponse


    //https://api.giphy.com/v1/gifs/trending?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8&q=cat&offset=0&limit=10
    @GET("gifs/trending?")
    suspend fun trendingGif(
        @Query("api_key") api_key: String = API_KEY,
        @Query("offset") offset: String = "0",
        @Query("limit") limit: String = "10"
    ): TrendingResponse


    //https://api.giphy.com/v1/gifs/translate?api_key=gdsqQrQq2mEOPm7Q5ZQ2jHRNX91kwYG8&s=cat
    @GET("gifs/translate?")
    suspend fun translateGif(
        @Query("s") s: String,
        @Query("api_key") api_key: String = API_KEY
    ): TranslateResponse



    @GET("article/list/{page}/json")
    suspend fun getHomePageList(
        @Path("page") page: Int
    ): BaseResponse<PageData>?

    @GET("project/list/{page}/json")
    suspend fun getProjectPageList(
        @Path("page") page: Int,
        @Query("cid") cid: Int
    ): BaseResponse<PageData>?


    @GET("banner/json")
    suspend fun getBanner(): BaseResponse<List<BannerData>>?

    @GET("project/tree/json")
    suspend fun getProjectSort(): BaseResponse<List<ProjectSortData>>?

//
//    @GET("project/tree/json")
//    suspend fun getSortData1(): Response<BaseResponse<List<SortData>>>?

    @GET("project/tree/json")
    suspend fun getSortData3(): BaseResponse<List<ProjectSortData>>?
}