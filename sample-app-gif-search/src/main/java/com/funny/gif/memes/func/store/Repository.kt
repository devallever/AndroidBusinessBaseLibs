package com.funny.gif.memes.func.store

import com.funny.gif.memes.app.Global
import com.funny.gif.memes.func.maker.GifMakeHelper
import com.funny.gif.memes.func.network.NetRepository
import com.allever.app.gif.memes.ui.adapter.bean.GifItem
import app.allever.android.lib.core.ext.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object Repository {

    suspend fun getGifItemList(last: String): MutableList<GifItem> {
        val gifItemList = mutableListOf<GifItem>()
        if (Store.getVersion() == Version.INTERNAL) {
            //国内版本
            val response = NetRepository.fetchWorld(Store.getToken(), Store.getUserId().toString(), last)

            response.let {
                it.data?.feeds?.let {
                    it.map {
                        log("feedId: ${it.feedId} -> ${it.gif}")
                        val gifItem = GifItem()
                        gifItem.id = it.feedId.toString()
                        gifItem.type = Version.INTERNAL
                        gifItem.avatar = it.avatar
                        gifItem.nickname = it.nickname
                        gifItem.title = it.content
                        gifItem.size = it.fsize
                        gifItem.tempUrl = it.gif
                        gifItem.cover = it.cover
                        gifItemList.add(gifItem)
                    }
                }
            }
        } else {
            //国外版本
            val response = NetRepository.getTrendList(last.toInt(), Global.SHOW_COUNT)
            //成功
            response.data?.let {
                val data = it.data
                data?.map {
                    log("trending = ${it.images.original.url}")
                    val gifItem = GifItem()
                    gifItem.id = it.id
                    gifItem.type = Version.INTERNATIONAL
                    gifItem.avatar = it?.user?.avatar_url ?: ""
                    gifItem.nickname = it?.user?.display_name ?: ""
                    gifItem.title = it.title ?: ""
                    gifItem.size = it.images.fixed_height.size.toLong()
                    gifItem.url = it.images.fixed_height.url
                    gifItemList.add(gifItem)
                }
            }
        }
        return gifItemList
    }

    suspend fun search(keyword: String, last: String): MutableList<GifItem> {
        val gifItemList = mutableListOf<GifItem>()
        if (Store.getVersion() == Version.INTERNAL) {
            //国内版本
            val response = NetRepository.search(keyword, Store.getToken(), Store.getUserId().toString(), last)
            response.let {
                it.data?.data?.let {
                    it.map {
                        log("feedId: ${it.feedId} -> ${it.gif}")
                        val gifItem = GifItem()
                        gifItem.id = it.feedId.toString()
                        gifItem.type = Version.INTERNAL
                        gifItem.avatar = it.avatar
                        gifItem.nickname = it.nickname
                        gifItem.title = it.content
                        gifItem.size = it.fsize
                        gifItem.tempUrl = it.gif
                        gifItem.cover = it.cover
                        gifItemList.add(gifItem)
                    }
                }
            }
        } else {
            val response = NetRepository.search(keyword, last.toInt(), Global.SHOW_COUNT)
            response.data?.let {
                val data = it.data
                data?.map {
                    log("trending = ${it.images.original.url}")
                    val gifItem = GifItem()
                    gifItem.id = it.id
                    gifItem.type = Version.INTERNATIONAL
                    gifItem.avatar = it?.user?.avatar_url ?: ""
                    gifItem.nickname = it?.user?.display_name ?: ""
                    gifItem.title = it.title ?: ""
                    gifItem.size = it.images.fixed_height.size.toLong()
                    gifItem.url = it.images.fixed_height.url
                    gifItemList.add(gifItem)
                }
            }
        }

        return gifItemList
    }

    suspend fun getMyGif() = withContext(Dispatchers.IO) {
        val gifItemList = mutableListOf<GifItem>()
        val dirFile = File(GifMakeHelper.gifDir)
        val list = dirFile.listFiles()
        list.map {
            if (it.path.endsWith("gif")) {
                val gifItem = GifItem()
                gifItem.url = it.path
                gifItemList.add(gifItem)
            }
        }
        gifItemList
    }
}