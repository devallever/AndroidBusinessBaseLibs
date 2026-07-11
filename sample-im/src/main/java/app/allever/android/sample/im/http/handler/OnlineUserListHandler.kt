package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.database.UserRepository
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.response.UserInfoData
import fi.iki.elonen.NanoHTTPD

class OnlineUserListHandler : HttpRequestHandler {
    override val path: String = "/api/user/onlineList"

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.GET) {
            return LocalHttpServer.buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = 405,
                msg = "仅支持 GET 请求",
                data = null
            )
        }

        val userList = UserRepository.getOnlineUserList()
        val dataList = userList.map {
            UserInfoData(
                userId = it.id,
                username = it.username,
                online = it.online,
                createTime = it.createTime
            )
        }
        return LocalHttpServer.buildSuccessResponse(dataList)
    }
}