package app.allever.android.sample.im

import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.engine.okhttp.OkHttpConfig
import app.allever.android.lib.network.engine.okhttp.OkHttpEngine
import app.allever.android.sample.im.http.response.BaseResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object IMGlobal {

    // 建议全局复用 OkHttpClient，不要每次创建
    val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    fun initNetwork() {
        NetCore.init {
            // 使用公开测试 API
            baseUrl(IMConfig.getHttpBaseUrl())
            // 设置统一业务响应类型
            responseClass(BaseResponse::class.java)

//            engine(OkHttpEngine.ENGINE_NAME) {
//                // OkHttp 专属配置
//                (this as? OkHttpConfig)?.apply {
////                    connectionPool(5, 5, java.util.concurrent.TimeUnit.MINUTES)
////                    retryOnConnectionFailure(true)
////                    addInterceptor("LoggingInterceptor")
////                    addNetworkInterceptor("LoggingInterceptor")
//                }
//            }
        }
    }
}