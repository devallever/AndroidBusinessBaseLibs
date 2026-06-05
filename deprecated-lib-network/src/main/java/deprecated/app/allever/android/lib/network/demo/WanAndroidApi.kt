package deprecated.app.allever.android.lib.network.demo

import deprecated.app.allever.android.lib.network.demo.reponse.BannerData
import deprecated.app.allever.android.lib.network.demo.reponse.BaseResponse
import deprecated.app.allever.android.lib.network.demo.reponse.PageData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WanAndroidApi {
    @GET("banner/json")
    suspend fun getBanner(): BaseResponse<List<BannerData>>

    @GET("banner/json")
    suspend fun getBannerForJava(): BaseResponse<*>

    @GET("banner/json")
    fun getBannerCall(): Call<BaseResponse<List<BannerData>>>

    @GET("article/list/{page}/json")
    suspend fun getHomePageList(
        @Path("page") page: Int
    ): BaseResponse<PageData>

    @GET("project/list/{page}/json")
    suspend fun getProjectPageList(
        @Path("page") page: Int,
        @Query("cid") cid: Int
    ): BaseResponse<PageData>

}