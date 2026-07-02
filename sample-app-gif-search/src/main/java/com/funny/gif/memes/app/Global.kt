package com.funny.gif.memes.app

import android.os.Environment
import com.funny.gif.memes.func.store.Store
import com.allever.app.gif.memes.ui.adapter.bean.GifItem
import com.allever.lib.common.app.App
import com.allever.lib.common.util.FileUtil
import com.allever.lib.common.util.FileUtils
import java.io.File

object Global {

    //备份
    val backFileDir =
        App.context.cacheDir.absolutePath + File.separator + "backup"

    val backupFilePath = backFileDir + File.separator + "data.json"

    const val SAVE_ALBUM = "GifMemes"

    //保存路径 /sdcard/GifFunny/XXX.gif
    val saveDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + File.separator + SAVE_ALBUM

    //缓存下载完成后复制到的目录 /sdcard/{packageName}/temp/XXX
    val tempDir = App.context.cacheDir.absolutePath + File.separator + ".temp"

    //缓存时的路径,浏览时自动下载 /data/data/cache/gif/XXX
    val cacheDir = App.context.cacheDir.absolutePath + File.separator + "gif"


    const val SP_TYPE = "type"
    const val SP_KEYWORD = "keyword"
    const val SP_OFFSET = "offset"
    const val SP_SEARCH_OFFSET = "search_offset"
    const val SP_COUNT = "count"

    const val SHOW_COUNT = 5

    fun init() {

    }

    fun createDir() {
        createDir(cacheDir)
        createDir(tempDir)
        createDir(saveDir)
        createDir(backFileDir)
    }

    fun getIndex(gifId: String, dataBeanList: MutableList<GifItem>): Int {
        var position = -1
        dataBeanList.mapIndexed { index, dataBean ->
            if (dataBean.id.toString() == gifId) {
                position = index
                return@mapIndexed
            }
        }
        return position
    }

    fun checkLogin(): Boolean = Store.getToken().isNotEmpty() && Store.getUserId() != 0

    fun logout() {
        Store.saveUserId(0)
        Store.saveToken("")
    }

    private fun createDir(dir: String) {
        if (!FileUtils.checkExist(dir)) {
            FileUtil.createDir(dir)
        }
    }

}