package deprecated.app.allever.android.lib.network.demo

import deprecated.app.allever.android.lib.network.demo.reponse.BannerData
import deprecated.app.allever.android.lib.network.ApiService
import deprecated.app.allever.android.lib.network.HttpHelper
import deprecated.app.allever.android.lib.network.ResponseCallback
import deprecated.app.allever.android.lib.network.RetrofitCallback
import deprecated.app.allever.android.lib.network.cache.ResponseCache
import kotlinx.coroutines.delay

object AppRepository {
    private val wanAndroidApi by lazy {
        ApiService.get(WanAndroidApi::class.java)
    }

    @Deprecated("")
    suspend fun getBannerForJava() = HttpHelper.requestForJava {
        wanAndroidApi.getBannerForJava()
    }

    @Deprecated("")
    suspend fun getBanner() = HttpHelper.request {
        wanAndroidApi.getBanner()
    }

    /**
     * Java回调方式
     */
    fun getBannerCall(
        responseCache: ResponseCache<*>? = null,
        callback: ResponseCallback<List<BannerData>>? = null
    ) {
        HttpHelper.enqueue(responseCache, callback) {
            wanAndroidApi.getBannerCall().enqueue(RetrofitCallback(responseCache, callback))
        }
    }

    /**
     * kotlin协程
     */
    suspend fun getBanner(responseCache: ResponseCache<*>? = null) =
        HttpHelper.request(responseCache) {
            wanAndroidApi.getBanner()
        }

    /**
     * kotlin协程 + LiveData 方式一
     */
    suspend fun getBannerForLiveData(responseCache: ResponseCache<*>? = null) =
        HttpHelper.requestLiveData(responseCache) {
            wanAndroidApi.getBanner()
        }

    /**
     * kotlin协程 + LiveData 方式一
     */
    fun getBannerWithLiveData(responseCache: ResponseCache<*>? = null) =
        HttpHelper.requestLiveData2(responseCache) {
            wanAndroidApi.getBanner()
        }

    suspend fun test(): String {
        delay(1000)
        return "hello"

    }

    @Deprecated("不使用缓存")
    fun getBannerCall(callback: ResponseCallback<List<BannerData>>) {
        wanAndroidApi.getBannerCall().enqueue(RetrofitCallback(callback))
    }

    suspend fun getHomePageArticleList(page: Int) = HttpHelper.request {
        wanAndroidApi.getHomePageList(page)
    }
}