package com.alsg.bakericon.logic

import com.alsg.bakericon.Constant
import com.alsg.bakericon.network.response.ResponseData
import com.alsg.bakericon.ui.adapter.data.PackItem

/**
 *@Description
 *@author: zq
 *@date: 2024/1/18
 */
object DataParserRepo {
    fun parseIconResponseData(data: List<ResponseData>?): MutableList<PackItem> {
        val list = mutableListOf<PackItem>()
        data?.map {
            val item = PackItem()
            item.name = it.topic
            val path = "${Constant.ICON_PATH}/${it.path}"
            //拼接封面路径 https://baker.app-lessfunc.uk/baker/icons/SocialMedia/cover.jpg
            item.cover = "${path}/cover.jpg"
//                log("cover = ${item.cover}")
            //拼接每张图片 https://baker.app-lessfunc.uk/baker/icons/SocialMedia/1.png
            val imageList = mutableListOf<String>()
            for (i in 1..it.count) {
                val imageUrl = "${path}/${i}.png"
//                    log("imageUrl = $imageUrl")
                imageList.add(imageUrl)
            }
            item.imageList = imageList
            list.add(item)
        }
        return list
    }

    fun parseStickerResponseData(data: List<ResponseData>?): MutableList<PackItem> {
        val list = mutableListOf<PackItem>()
        data?.map {
            val item = PackItem()
            item.name = it.topic
            val path = "${Constant.STICKER_PATH}/${it.path}"
            //拼接封面路径 https://baker.app-lessfunc.uk/baker/icons/SocialMedia/cover.jpg
            item.cover = "${path}/cover.jpg"
//                log("cover = ${item.cover}")
            //拼接每张图片 https://baker.app-lessfunc.uk/baker/icons/SocialMedia/1.png
            val imageList = mutableListOf<String>()
            for (i in 1..it.count) {
                val imageUrl = "${path}/${i}.png"
//                    log("imageUrl = $imageUrl")
                imageList.add(imageUrl)
            }
            item.imageList = imageList
            list.add(item)
        }
        return list
    }
}