package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.database.UserRepository
import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.parseJsonBody
import app.allever.android.sample.im.http.request.UserInfoRequest

import app.allever.android.sample.im.http.response.UserInfoData
import fi.iki.elonen.NanoHTTPD

class UserInfoHandler : HttpRequestHandler {
    override val path = API.USER_INFO

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = LocalHttpServer.BizCode.METHOD_NOT_ALLOWED,
                msg = "仅支持 POST 请求",
                data = null
            )
        }

        val request = session.parseJsonBody<UserInfoRequest>()
            ?: return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = LocalHttpServer.BizCode.BAD_REQUEST,
                msg = "请求体必须为合法 JSON",
                data = null
            )

        val user = UserRepository.getUserByUsername(request.username)
        if (user == null) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = LocalHttpServer.BizCode.USER_QUERY_NOT_FOUND,
                msg = "查不到用户",
                data = null
            )
        }
        val result = UserInfoData(
            userId = user.id,
            username = user.username,
            online = user.online,
            createTime = user.createTime
        )
        return LocalHttpServer.buildSuccessResponse(result)
    }
}