package app.allever.android.sample.im.http.handler

import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.CoroutineHelper
import app.allever.android.sample.im.database.UserRepository
import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.request.AuthRequest
import app.allever.android.sample.im.http.parseJsonBody
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.launch

class LogoutHandler : HttpRequestHandler {
    override val path = API.LOGOUT

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = LocalHttpServer.BizCode.METHOD_NOT_ALLOWED,
                msg = "仅支持 POST 请求",
                data = null
            )
        }

        val req = session.parseJsonBody<AuthRequest>()
            ?: return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = LocalHttpServer.BizCode.BAD_REQUEST,
                msg = "参数格式错误",
                data = null
            )

        if (req.username.isEmpty()) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = LocalHttpServer.BizCode.BAD_REQUEST,
                msg = "参数错误",
                data = null
            )
        }

        if (!UserRepository.isUserExists(req.username)) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = LocalHttpServer.BizCode.USER_NOT_FOUND,
                msg = "用户不存在",
                data = null
            )
        }

        // 断开该用户的 WebSocket 连接
        IMWebSocketServer.disconnectUser(req.username)
        return LocalHttpServer.buildSuccessResponse(mapOf("username" to req.username))
    }
}