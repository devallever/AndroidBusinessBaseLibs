package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class ImageHandler : HttpRequestHandler {
    override val path: String = "/api/image"

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val fullUri = session.uri
        val filename = fullUri.substringAfter("/api/image/", missingDelimiterValue = "")
        if (filename.isEmpty()) {
            return LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = 400,
                msg = "缺少文件名",
                data = null
            )
        }

        val file = ImageUploadHandler.getImageFile(filename)
        if (file == null || !file.exists()) {
            return LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.NOT_FOUND,
                bizCode = 404,
                msg = "图片不存在",
                data = null
            )
        }

        val inputStream = FileInputStream(file)
        val mimeType = getMimeType(filename)

        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            mimeType,
            inputStream,
            file.length()
        ).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Cache-Control", "max-age=3600")
        }
    }

    private fun getMimeType(filename: String): String {
        return when {
            filename.lowercase().endsWith(".png") -> "image/png"
            filename.lowercase().endsWith(".jpg") || filename.lowercase().endsWith(".jpeg") -> "image/jpeg"
            filename.lowercase().endsWith(".webp") -> "image/webp"
            filename.lowercase().endsWith(".gif") -> "image/gif"
            else -> "image/jpeg"
        }
    }
}