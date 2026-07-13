package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.response.ImageData
import fi.iki.elonen.NanoHTTPD
import java.io.File

class ImageListHandler : HttpRequestHandler {
    override val path = API.IMAGE_LIST

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.GET) {
            return LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = LocalHttpServer.BizCode.METHOD_NOT_ALLOWED,
                msg = "仅支持 GET 请求",
                data = null
            )
        }

        val imageDir = File(LocalHttpServer.getFilesDir(), "images")
        if (!imageDir.exists() || !imageDir.isDirectory) {
            return LocalHttpServer.buildSuccessResponse(emptyList<ImageData>())
        }

        val images = mutableListOf<ImageData>()
        imageDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val url = "${API.IMAGE}/${file.name}"
                images.add(
                    ImageData(
                        filename = file.name,
                        url = url,
                        size = file.length(),
                    )
                )
            }
        }

        return LocalHttpServer.buildSuccessResponse(images)
    }
}