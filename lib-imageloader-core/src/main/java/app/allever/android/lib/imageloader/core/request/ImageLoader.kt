package app.allever.android.lib.imageloader.core.request

import app.allever.android.lib.imageloader.core.impl.DefaultImageLoader
import app.allever.android.lib.imageloader.core.target.ImageTarget

/**
 * 图片加载器接口 - 唯一顶层抽象
 *
 * 所有图片加载操作都通过此接口执行。
 * 切换底层实现只需替换实例，业务代码无需任何改动。
 *
 * 使用方式：
 * ```
 * ImageLoader.getInstance().load(request)
 * ```
 *
 * 替换实现：
 * ```
 * ImageLoader.setInstance(GlideImageLoader())
 * // 或在 Application 中通过 DI 注入
 * ```
 */
interface ImageLoader {

    /**
     * 执行图片加载请求
     */
    fun load(request: ImageRequest)

    /**
     * 取消指定目标的加载请求
     */
    fun cancel(target: ImageTarget)

    /**
     * 清除所有内存缓存
     * 应在主线程调用
     */
    fun clearMemoryCache()

    /**
     * 清除所有磁盘缓存
     * 应在后台线程调用
     */
    fun clearDiskCache()

    companion object {

        @Volatile
        private var instance: ImageLoader = DefaultImageLoader()

        /**
         * 获取当前 ImageLoader 实例
         */
        fun getInstance(): ImageLoader = instance

        /**
         * 替换 ImageLoader 实现
         * 建议在 Application.onCreate() 中调用
         */
        fun setInstance(loader: ImageLoader) {
            synchronized(this) {
                instance = loader
            }
        }
    }
}
