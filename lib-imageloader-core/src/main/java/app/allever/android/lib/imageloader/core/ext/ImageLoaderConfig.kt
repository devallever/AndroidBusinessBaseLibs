package app.allever.android.lib.imageloader.core.ext

import android.content.Context
import app.allever.android.lib.imageloader.core.cache.MemoryCache
import app.allever.android.lib.imageloader.core.engine.HttpEngine
import app.allever.android.lib.imageloader.core.engine.ImageExecutor
import app.allever.android.lib.imageloader.core.impl.DefaultImageLoader
import app.allever.android.lib.imageloader.core.request.ImageLoader

/**
 * 全局配置入口
 *
 * 在 Application.onCreate() 中调用进行初始化和全局配置。
 *
 * 用法：
 * ```
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         ImageLoaderConfig.init(this) {
 *             memoryCacheSize(50 * 1024 * 1024)   // 50MB 内存缓存
 *             diskCacheSize(200 * 1024 * 1024)     // 200MB 磁盘缓存
 *             threadPoolSize(4)                    // 并发线程数
 *             connectTimeout(10_000)               // 连接超时 10s
 *             readTimeout(15_000)                  // 读取超时 15s
 *             // loader = GlideImageLoader()       // 取消注释切换到 Glide
 *         }
 *     }
 * }
 * ```
 */
object ImageLoaderConfig {

    /**
     * 初始化图片加载器（使用默认 DefaultImageLoader）
     *
     * @param context Application Context
     * @param block 配置 DSL
     */
    fun init(context: Context, block: (Config.() -> Unit)? = null) {
        val config = Config().apply { block?.invoke(this) }

        val memoryCache = config.memoryCacheSize?.let { MemoryCache(it) } ?: MemoryCache()

        val loader = DefaultImageLoader(
            memoryCache = memoryCache,
            networkEngine = HttpEngine.apply {
                connectTimeout = config.connectTimeout ?: HttpEngine.DEFAULT_CONNECT_TIMEOUT
                readTimeout = config.readTimeout ?: HttpEngine.DEFAULT_READ_TIMEOUT
            }
        )

        // 初始化磁盘缓存
        loader.initDiskCache(context)

        // 配置线程池
        config.threadPoolSize?.let { ImageExecutor.corePoolSize = it }

        // 设置全局实例
        if (config.loader != null) {
            ImageLoader.setInstance(config.loader!!)
        } else {
            ImageLoader.setInstance(loader)
        }
    }

    /**
     * 配置参数容器
     */
    class Config {
        /** 内存缓存大小（字节），默认使用 MemoryCache 默认值 */
        var memoryCacheSize: Int? = null

        /** 磁盘缓存大小（字节），默认 100MB */
        var diskCacheSize: Long? = null

        /** 线程池核心线程数，默认 CPU 核心数（上限 4） */
        var threadPoolSize: Int? = null

        /** HTTP 连接超时（毫秒），默认 10 秒 */
        var connectTimeout: Int? = null

        /** HTTP 读取超时（毫秒），默认 15 秒 */
        var readTimeout: Int? = null

        /** 自定义 ImageLoader 实现（设置后将忽略其他配置） */
        var loader: ImageLoader? = null

        fun memoryCacheSize(bytes: Int) { memoryCacheSize = bytes }
        fun diskCacheSize(bytes: Long) { diskCacheSize = bytes }
        fun threadPoolSize(size: Int) { threadPoolSize = size.coerceIn(1, 8) }
        fun connectTimeout(ms: Int) { connectTimeout = ms.coerceAtLeast(1000) }
        fun readTimeout(ms: Int) { readTimeout = ms.coerceAtLeast(1000) }
    }
}
