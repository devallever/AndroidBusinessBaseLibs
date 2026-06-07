package app.allever.android.lib.imageloader.core.engine

/**
 * 网络加载引擎接口
 *
 * 抽象网络图片下载能力，内置实现使用 HttpURLConnection，
 * 也可替换为 OkHttp 或其他 HTTP 客户端。
 */
interface NetworkEngine {

    /**
     * 从指定 URL 下载数据
     * @param url 图片 URL
     * @return 图片原始字节数组
     * @throws Exception 网络错误、HTTP 错误码等
     */
    fun load(url: String): ByteArray
}
