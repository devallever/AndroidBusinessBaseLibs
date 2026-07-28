package com.alsg.bakericon.local

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.CoroutineHelper
import app.allever.android.lib.core.helper.GsonHelper
import com.alsg.bakericon.Constant
import com.alsg.bakericon.network.response.PackLocalResponse
import com.alsg.bakericon.network.response.ResponseData
import com.alsg.bakericon.network.response.TopLocalResponse
import com.alsg.bakericon.ui.adapter.data.PackItem
import com.alsg.bakericon.ui.adapter.data.SingleItem
import com.alsg.bakericon.util.AssetsHelper
import com.alsg.bakericon.util.MMKVHelper
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
object LocalRepo {
    private val localIconPackItemList = mutableListOf<PackItem>()
    private val localIconPathList = mutableListOf<String>()
    private val localRandomIconPathList = mutableListOf<String>()

    private val localStickerPackItemList = mutableListOf<PackItem>()
    private val localStickerPathList = mutableListOf<String>()
    private val localRandomStickerPathList = mutableListOf<String>()

    private val localTopItemList = mutableListOf<SingleItem>()

    private const val key_icon_cache = "key_icon_cache"


    fun loadAll() {
        CoroutineHelper.IO.launch {
            fetchIconData()
            fetchStickerData()
            fetchTopData()
        }
    }

    suspend fun fetchIconData(forceLoad: Boolean = false) = withContext(Dispatchers.IO) {
        if (localIconPackItemList.isNotEmpty() && !forceLoad) {
            return@withContext localIconPackItemList
        }

        if (forceLoad) {
            localIconPackItemList.clear()
            localIconPathList.clear()
            localRandomIconPathList.clear()
        }

        val json = AssetsHelper.getJson(App.context, "si_icon.json")
        val localResponse = GsonHelper.fromJson(json, PackLocalResponse::class.java)
        val responseData = localResponse?.data
        responseData?.map {
            val item = PackItem()
            item.name = it.topic
            //拼接封面路径 file:///android_asset/si_icon/1/cover.jpg
            val cover = "${Constant.ACCEPT_FILE}/si_icon${it.path}"
//                log("local cover = $cover")
            item.cover = cover
            val path = it.path.substring(0, it.path.lastIndexOf("/"))
            //拼接每张图片 file:///android_asset/si_icon/1/1.png
            val imageList = mutableListOf<String>()
            for (i in 1..it.count) {
                val imageUrl = "${Constant.ACCEPT_FILE}/si_icon${path}/${i}.png"
//                    log("local imageUrl = $imageUrl")
                imageList.add(imageUrl)
                localIconPathList.add(imageUrl)
            }
            item.imageList = imageList
            localIconPackItemList.add(item)
        }


        //随机200
        localRandomIconPathList.addAll(randomDistinctElements(localIconPathList, 200))

        localIconPackItemList
    }

    suspend fun fetchRandomIconList() = withContext(Dispatchers.IO) {
        if (localRandomIconPathList.isNotEmpty()) {
            return@withContext localRandomIconPathList
        } else {
            fetchIconData(true)
            localRandomIconPathList
        }
    }

    suspend fun fetchStickerData(forceLoad: Boolean = false) = withContext(Dispatchers.IO) {
        if (localStickerPackItemList.isNotEmpty() && !forceLoad) {
            return@withContext localStickerPackItemList
        }

        if (forceLoad) {
            localRandomStickerPathList.clear()
            localStickerPathList.clear()
            localStickerPackItemList.clear()
        }

        //本地数据
        val json = AssetsHelper.getJson(App.context, "si_sticker.json")
        val localResponse = GsonHelper.fromJson(json, PackLocalResponse::class.java)
        val responseData = localResponse?.data
        responseData?.map {
            val item = PackItem()
            item.name = it.topic
            //拼接封面路径 file:///android_asset/si_icon/1/cover.jpg
            val cover = "${Constant.ACCEPT_FILE}/si_sticker${it.path}"
//                log("local cover = $cover")
            item.cover = cover
            val path = it.path.substring(0, it.path.lastIndexOf("/"))
            //拼接每张图片 file:///android_asset/si_icon/1/1.png
            val imageList = mutableListOf<String>()
            for (i in 1..it.count) {
                val imageUrl = "${Constant.ACCEPT_FILE}/si_sticker${path}/${i}.png"
//                    log("local imageUrl = $imageUrl")
                imageList.add(imageUrl)
                localStickerPathList.add(imageUrl)
            }
            item.imageList = imageList
            localStickerPackItemList.add(item)
        }

        localRandomStickerPathList.addAll(randomDistinctElements(localStickerPathList, 200))
        localStickerPackItemList
    }

    suspend fun fetchRandomStickerList() = withContext(Dispatchers.IO) {
        if (localRandomStickerPathList.isNotEmpty()) {
            return@withContext localRandomStickerPathList
        } else {
            fetchStickerData(true)
            localRandomStickerPathList
        }
    }

    suspend fun fetchTopData(forceLoad: Boolean = false) = withContext(Dispatchers.IO) {
        if (localTopItemList.isNotEmpty() && !forceLoad) {
            return@withContext localTopItemList
        }

        if (forceLoad) {
            localTopItemList.clear()
        }

        //本地数据
        val json = AssetsHelper.getJson(App.context, "si_top.json")
        log("local si_top.json = ${json}")
        val localResponse = GsonHelper.fromJson(json, TopLocalResponse::class.java)
        log("localResponse = ${localResponse?.toJson()} ")
        val localCount = localResponse?.count ?: 0
        if (localCount > 0) {
            for (i in 1..localCount) {
                val item = SingleItem()
                val imageUrl = "${Constant.ACCEPT_FILE}/si_top/${i}.png"
//                    log("local si_top imageUrl = $imageUrl")
                item.url = imageUrl
                localTopItemList.add(item)
            }
        }
        localTopItemList
    }

    suspend fun cacheIconData(iconJson: String?) = withContext(Dispatchers.IO) {
        log("cacheIconData = $iconJson")
        MMKVHelper.putString(key_icon_cache, iconJson)
    }

    suspend fun iconDataCache() = withContext(Dispatchers.IO) {
        val list = mutableListOf<ResponseData>()
        val json = MMKVHelper.getString(key_icon_cache)
        log("iconDataCache = $json")
        val responseData = toList(json)
        responseData.map {
            log("iconDataCache path = ${it.path}")
        }
        list.addAll(responseData)
        return@withContext list
    }

    /**
     * 随机n个不重复数据
     */
    private fun <T> randomDistinctElements(list: List<T>, n: Int): List<T> {
        val result = mutableListOf<T>()

        // 创建一个包含所有元素的集合
        val allElementsSet = list.toHashSet()

        while (result.size < n) {
            // 在集合中随机选择一个元素并添加到结果列表中
            if (!allElementsSet.isEmpty()) {
                val randomIndex = Random().nextInt(allElementsSet.size)
                val element = allElementsSet.elementAtOrNull(randomIndex) ?: continue

                result.add(element)
                allElementsSet.remove(element)
            } else {
                break
            }
        }

        return result
    }

    private fun toList(json: String): MutableList<ResponseData> {
        val list = mutableListOf<ResponseData>()
        try {
            val personList: List<ResponseData> = GsonHelper.getGson()
                .fromJson(json, object : TypeToken<List<ResponseData>>() {}.type)
            list.addAll(personList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

}