package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.response.EchoData
import fi.iki.elonen.NanoHTTPD

class EchoHandler : HttpRequestHandler {
    override val path = API.ECHO

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val text = session.parms["text"] ?: "空内容"
        return LocalHttpServer.buildSuccessResponse(EchoData("你发送了: $text"))
    }
}