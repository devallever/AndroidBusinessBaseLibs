package app.allever.android.sample.im.http


import fi.iki.elonen.NanoHTTPD

interface HttpRequestHandler {
    /** 匹配的请求路径 */
    val path: String

    /** 处理请求并返回响应 */
    fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response
}