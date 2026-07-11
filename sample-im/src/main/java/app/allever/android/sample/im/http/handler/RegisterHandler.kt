package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.database.UserRepository
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.request.AuthRequest
import app.allever.android.sample.im.http.response.UserInfoData
import app.allever.android.sample.im.http.parseJsonBody
import fi.iki.elonen.NanoHTTPD

class RegisterHandler : HttpRequestHandler {
    override val path: String = "/api/user/register"

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = 405,
                msg = "仅支持 POST 请求",
                data = null
            )
        }

        val req = session.parseJsonBody<AuthRequest>()
            ?: return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = 400,
                msg = "参数格式错误",
                data = null
            )

        val userId = UserRepository.register(req.username, req.password)
        return if (userId != null) {
            val data = UserInfoData(
                userId = userId,
                username = req.username,
                online = 0,
                createTime = System.currentTimeMillis()
            )
            LocalHttpServer.buildSuccessResponse(data)
        } else {
            LocalHttpServer.buildJsonResponse(
                bizCode = 1001,
                msg = "注册失败：用户名已存在或参数不合法",
                data = null
            )
        }
    }
}