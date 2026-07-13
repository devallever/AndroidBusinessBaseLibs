package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.response.MessageData
import fi.iki.elonen.NanoHTTPD

class RootHandler : HttpRequestHandler {
    override val path = API.ROOT

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return LocalHttpServer.buildSuccessResponse(
            MessageData("Android Local HTTP Server is running.")
        )
    }
}