package app.allever.android.sample.im.http.handler

import app.allever.android.sample.im.http.HttpRequestHandler
import app.allever.android.sample.im.http.LocalHttpServer
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class ImageUploadHandler : HttpRequestHandler {
    override val path: String = "/api/image/upload"

    override fun handle(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)

            if (files.isEmpty()) {
                return LocalHttpServer.buildJsonResponse<Any?>(
                    httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                    bizCode = 400,
                    msg = "请选择图片文件",
                    data = null
                )
            }

            val tempFilePath = files.values.firstOrNull()
            if (tempFilePath.isNullOrEmpty()) {
                return LocalHttpServer.buildJsonResponse<Any?>(
                    httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                    bizCode = 400,
                    msg = "请选择图片文件",
                    data = null
                )
            }

            val tempFile = File(tempFilePath)
            if (!tempFile.exists()) {
                return LocalHttpServer.buildJsonResponse<Any?>(
                    httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                    bizCode = 400,
                    msg = "文件不存在",
                    data = null
                )
            }

            val uri = session.uri
            val filename = extractFilename(uri) ?: "${UUID.randomUUID()}.jpg"
            val savePath = getImageSavePath(filename)

            FileInputStream(tempFile).use { inputStream ->
                FileOutputStream(savePath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val url = "${LocalHttpServer.getServerUrl()}/api/image/$filename"
            LocalHttpServer.log("图片上传成功: $url")

            LocalHttpServer.buildSuccessResponse(mapOf("url" to url, "filename" to filename))
        } catch (e: Exception) {
            LocalHttpServer.logE("图片上传失败: ${e.message}")
            LocalHttpServer.buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR,
                bizCode = 500,
                msg = "图片上传失败",
                data = null
            )
        }
    }

    private fun extractFilename(uri: String): String? {
        return try {
            val query = uri.substringAfter("?", "")
            val params = query.split("&")
            params.forEach { param ->
                if (param.startsWith("filename=")) {
                    return param.substringAfter("filename=")
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun getImageSavePath(filename: String): String {
            val dir = File(LocalHttpServer.getFilesDir(), "images").apply {
                if (!exists()) mkdirs()
            }
            return File(dir, filename).absolutePath
        }

        fun getImageFile(filename: String): File? {
            val file = File(File(LocalHttpServer.getFilesDir(), "images"), filename)
            return if (file.exists()) file else null
        }
    }
}