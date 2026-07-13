package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import fi.iki.elonen.NanoHTTPD
import java.io.File

class ImageDeleteHandler : HttpRequestHandler {
    override val path = API.IMAGE_DELETE

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.DELETE) {
            return LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = LocalHttpServer.BizCode.METHOD_NOT_ALLOWED,
                msg = "仅支持 DELETE 请求",
                data = null
            )
        }

        session.parseBody(mapOf())
        val params = session.parameters
        val filename = params["filename"]?.firstOrNull()

        if (filename.isNullOrEmpty()) {
            return LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = LocalHttpServer.BizCode.BAD_REQUEST,
                msg = "缺少文件名参数",
                data = null
            )
        }

        val file = ImageUploadHandler.getImageFile(filename!!)
        if (file == null || !file.exists()) {
            return LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.NOT_FOUND,
                bizCode = LocalHttpServer.BizCode.IMAGE_NOT_FOUND,
                msg = "图片不存在",
                data = null
            )
        }

        val deleted = file.delete()
        return if (deleted) {
            LocalHttpServer.log("图片删除成功: ${file.name}")
            LocalHttpServer.buildSuccessResponse(mapOf("filename" to file.name))
        } else {
            LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR,
                bizCode = LocalHttpServer.BizCode.IMAGE_DELETE_FAILED,
                msg = "删除失败",
                data = null
            )
        }
    }
}