package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.parseJsonBody
import app.allever.android.sample.im.http.request.UserInfoRequest

import app.allever.android.sample.im.http.response.UserInfoData
import fi.iki.elonen.NanoHTTPD

class UserInfoHandler : HttpRequestHandler {
    override val path: String = "/api/user"

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = 405,
                msg = "仅支持 POST 请求",
                data = null
            )
        }

        val request = session.parseJsonBody<UserInfoRequest>()
            ?: return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = 400,
                msg = "请求体必须为合法 JSON",
                data = null
            )

        val result = UserInfoData(
            userId = request.userId,
            nickname = "用户_${request.userId}",
            level = 1
        )
        return LocalHttpServer.buildSuccessResponse(result)
    }
}