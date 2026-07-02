package com.funny.gif.memes.util

import com.funny.gif.memes.bean.LikedItem
import com.allever.app.gif.memes.ui.adapter.bean.GifItem
import com.google.gson.Gson
import org.litepal.LitePal

object DBHelper {

    fun getAllLikeItem(): MutableList<LikedItem> {
        return LitePal.findAll(LikedItem::class.java)
    }

    fun getLikeItem(gifId: String): LikedItem? {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        if (data.isNotEmpty()) {
            return data[0]
        }
        return null
    }

    fun getLikedList(): MutableList<GifItem> {
        val list = LitePal.findAll(LikedItem::class.java)
        val result = mutableListOf<GifItem>()
        val gson = Gson()
        list.map {
            val dataBean = gson.fromJson(it.data, GifItem::class.java)
            result.add(dataBean)
        }
        return result
    }

    fun liked(gifId: String, dataBean: GifItem) {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        if (data.isEmpty()) {
            val newLike = LikedItem()
            newLike.gifId = gifId
            newLike.data = Gson().toJson(dataBean)
            newLike.save()
        }
    }

    fun liked(gifId: String, dataBeanStr: String) {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        if (data.isEmpty()) {
            val newLike = LikedItem()
            newLike.gifId = gifId
            newLike.data = dataBeanStr
            newLike.save()
        }
    }

    fun unLiked(gifId: String) {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        data.map {
            it.delete()
        }
    }

    fun isLiked(gifId: String): Boolean {
        val data = LitePal.where("gifId = ?", gifId).find<LikedItem>(LikedItem::class.java)
        return data.isNotEmpty()
    }
}