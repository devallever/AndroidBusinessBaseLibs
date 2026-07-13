package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.response.StatusData
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import fi.iki.elonen.NanoHTTPD
class StatusHandler : HttpRequestHandler {
    override val path = API.USER_STATUS

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val data = StatusData(
            port = LocalHttpServer.port,
            online_client = IMWebSocketServer.getOnlineCount(),
            timestamp = System.currentTimeMillis()
        )
        return LocalHttpServer.buildSuccessResponse(data)
    }
}