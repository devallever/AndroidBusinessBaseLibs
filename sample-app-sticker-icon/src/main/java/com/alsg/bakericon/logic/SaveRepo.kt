package com.alsg.bakericon.logic

import android.os.Environment
import com.allever.lib.base.app.App
import com.allever.lib.base.ext.toast
import com.allever.lib.base.helper.CoroutineHelper
import com.allever.lib.base.util.FileIOUtils
import com.allever.lib.base.util.FileUtils
import com.allever.lib.base.util.MD5
import com.alsg.bakericon.Constant.ACCEPT_FILE
import com.alsg.bakericon.R
import com.alsg.bakericon.db.DBRepo
import com.alsg.bakericon.util.AssetsHelper
import com.alsg.bakericon.util.MediaHelper
import com.alsg.bakericon.util.copyToAlbum
import com.alsg.bakericon.util.saveToAlbum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
object SaveRepo {
    private val DOWNLOAD_DIR = App.context.cacheDir.absolutePath

    private val SAVE_DIR =
        Environment.getExternalStoragePublicDirectory(MediaHelper.ALBUM_DIR).absolutePath

    private val mOkHttpClient: OkHttpClient by lazy {
        OkHttpClient()
    }

    suspend fun save(path: String, successCallback: () -> Unit = {}) = withContext(Dispatchers.IO) {
        val fileName = "${MD5.getMD5Str(path)}.png"
        val savePath = "$SAVE_DIR${File.separator}${
            App.context.getString(
                R.string.app_name
            )
        }${File.separator}${fileName}"
        if (FileUtils.checkExist(savePath)) {
            toast("already exist")
            successCallback.invoke()
            return@withContext
        }

//        log("下载路径 = $DOWNLOAD_DIR")
        if (path.startsWith("http", true)) {
            //下载图片
            val requests = Request.Builder()
                .url(path)
                .build()

//            log("下载图片：$path")
            mOkHttpClient.newCall(requests).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
//                    log("下载成功: $path")
                    val downloadPath = "$DOWNLOAD_DIR${File.separator}${MD5.getMD5Str(path)}.png"
                    val result = FileIOUtils.writeFileFromBytesByStream(
                        downloadPath,
                        response.body?.byteStream()?.readBytes()
                    )
                    val downloadFile = if (result) {
//                        log("保存成功: $path -> $downloadPath")
//                        toast("save to $downloadPath")
                        File(downloadPath)
                    } else {
//                        logE("保存失败: $path")
//                        toast("save failed")
                        null
                    }

                    val success = downloadFile?.copyToAlbum(
                        App.context, fileName, App.context.getString(
                            R.string.app_name
                        )
                    )

                    if (success != null) {
//                        log("保存成功: $savePath")
                        toast("save to: $savePath")
                        CoroutineHelper.DEFAULT.launch {
                            DBRepo.saveDownloadRecord()
                        }
                        successCallback.invoke()
                    } else {
                        toast("save fail")
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
//                    logE("下载失败: $path")
                    toast("save failed")
                }
            })
        } else {
            //保存 asset
            val assetsPath = path.replace("${ACCEPT_FILE}/", "")
            val success = AssetsHelper.toInputStream(assetsPath)?.saveToAlbum(
                App.context, fileName, App.context.getString(
                    R.string.app_name
                )
            )

            if (success != null) {
//                log("保存成功: $savePath")
                toast("save to: $savePath")
                CoroutineHelper.DEFAULT.launch {
                    DBRepo.saveDownloadRecord()
                }
                successCallback.invoke()
            } else {
                toast("save fail")
            }
        }
    }
}