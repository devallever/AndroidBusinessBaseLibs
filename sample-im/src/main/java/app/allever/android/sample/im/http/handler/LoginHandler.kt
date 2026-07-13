package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.database.UserRepository
import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.request.AuthRequest
import app.allever.android.sample.im.http.response.UserInfoData
import app.allever.android.sample.im.http.parseJsonBody
import fi.iki.elonen.NanoHTTPD

class LoginHandler : HttpRequestHandler {
    override val path = API.LOGIN

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

        val user = UserRepository.login(req.username, req.password)
        return if (user != null) {
            val data = UserInfoData(
                userId = user.id,
                username = user.username,
                online = user.online,
                createTime = user.createTime
            )
            LocalHttpServer.buildSuccessResponse(data)
        } else {
            LocalHttpServer.buildJsonResponse(
                bizCode = LocalHttpServer.BizCode.LOGIN_FAILED,
                msg = "用户名或密码错误",
                data = null
            )
        }
    }
}