//package app.flash.tunnel.vpn.lib.common.util
//
//import app.flash.tunnel.vpn.lib.common.Common
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.logging.HttpLoggingInterceptor
//import java.util.concurrent.TimeUnit
//
//object NetManager {
//
//    private lateinit var mOkHttpClient: OkHttpClient
//
//    fun init(
//        networkInterceptors: MutableList<Interceptor>? = null,
//        interceptors: MutableList<Interceptor>? = null
//    ) {
//        OkHttpClient.Builder().apply {
//            if (Common.DEBUG) {
//                val loggerInterceptor = HttpLoggingInterceptor()
//                loggerInterceptor.level = HttpLoggingInterceptor.Level.BODY
//                addNetworkInterceptor(loggerInterceptor)
//            }
//
//            networkInterceptors?.map {
//                addNetworkInterceptor(it)
//            }
//            interceptors?.map {
//                addInterceptor(it)
//            }
//            connectTimeout(20, TimeUnit.SECONDS)
//            mOkHttpClient = build()
//        }
//
//    }
//
//    suspend fun getString(url: String) = withContext(Dispatchers.IO) {
//        //response not contain 'Content-Encoding', 'gzip'
////        val unzipString = GzipManager.unGzipString(getByteArray(url)?: return@withContext null)
//
//        //response contain 'Content-Encoding', 'gzip' okhttp handle
//        return@withContext String(getByteArray(url) ?: return@withContext null)
//    }
//
//    suspend fun getByteArray(url: String) = withContext(Dispatchers.IO) {
//        try {
//            val builder = Request.Builder().apply {
//                url(url)
//            }
//            val call = mOkHttpClient.newCall(builder.build())
//            val response = call.execute()
//            val body = response.body
//            return@withContext body?.bytes()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return@withContext null
//    }
//
//}