package com.funny.gif.memes.app

import com.funny.gif.memes.func.maker.GifMakeHelper
import com.funny.gif.memes.func.media.FolderBean
import com.funny.gif.memes.func.media.MediaHelper
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.util.ImageLoader
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litepal.LitePal


object GifSearch {
    private var isInit = false
    fun initThreadPackage() {

        if (isInit) {
            return
        }


        LitePal.initialize(App.context)


        GlobalScope.launch {
//            DataStore.init(App.context)
//            val response = NetRepository.initGifFun(Store.getToken(), Store.getUserId().toString()) {
//                logE(it)
//            }
//            response.data?.let {
//                val token = it.token
//                if (token.isNotEmpty()) {
//                    Store.saveToken(it.token)
//                    log("初始化成功： ${it.token}")
//                }
//            }

            val folderInfo = MediaHelper.getAllFolder(App.context, MediaHelper.TYPE_VIDEO)
            folderInfo.add(FolderBean())
            folderInfo.map {
                log(it.dir)
                Store

                val mediaItemList = MediaHelper.getVideoMedia(App.context, it.dir, 0)
                mediaItemList.map {
                    log("视频：${it.path}")
                }
            }

            val result = MediaHelper.getImageMedia(App.context, GifMakeHelper.gifDir)
            result.map {
                log("Gif: ${it.path}")
            }
        }

        isInit = true

    }


    fun onLowMemory() {
        ImageLoader.onLowMemory()
    }

    fun onTerminate() {
        ImageLoader.clearMemoryCache()
    }

    fun onTrimMemory(level: Int) {
        ImageLoader.onTrimMemroy(level)
    }
}