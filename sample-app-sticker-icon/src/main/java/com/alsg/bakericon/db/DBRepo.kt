package com.alsg.bakericon.db

import android.text.format.DateUtils.isToday
import com.alsg.bakericon.db.entity.DownloadRecord
import com.alsg.bakericon.db.entity.Favourite
import com.alsg.bakericon.ui.adapter.data.SingleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.findAll


/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
object DBRepo {

    suspend fun fetchFavouriteData() = withContext(Dispatchers.IO) {
        val favouriteSingleItem = mutableListOf<SingleItem>()
        val result = LitePal.findAll<Favourite>()
        result.map {
//            log("favourites = ${it.toJson()}")
            val item = SingleItem()
            item.url = it.path
            favouriteSingleItem.add(item)
        }
        return@withContext favouriteSingleItem
    }

    suspend fun like(path: String) = withContext(Dispatchers.IO) {
        val favourite = Favourite()
        favourite.path = path
        favourite.create = System.currentTimeMillis()
        return@withContext favourite.save()
    }


    suspend fun disLike(path: String) = withContext(Dispatchers.IO) {
        val result = LitePal.where("path = ?", path).find<Favourite>()
        var success = false
        result.map {
            it.delete()
            success = true
        }
        success
    }

    suspend fun isLike(path: String) = withContext(Dispatchers.IO) {
        val result = LitePal.where("path = ?", path).find<Favourite>()
        return@withContext result.isNotEmpty()
    }

    /**
     * 记录下载
     */
    suspend fun saveDownloadRecord() = withContext(Dispatchers.IO) {
        val downloadRecord = DownloadRecord()
        downloadRecord.create = System.currentTimeMillis()
        val result = downloadRecord.save()
//        if (result) {
//            log("保存下载记录成功")
//        } else {
//            logE("保存下载记录失败")
//        }
    }

    /**
     * 每次程序启动时清除非今天的数据
     */
    suspend fun clearOutDateDownload() = withContext(Dispatchers.IO) {
        val result = LitePal.findAll(DownloadRecord::class.java)
//        if (result.isEmpty()) {
//            logE("没有下载记录")
//        }
        result.map {
//            log("下载记录：${it.toJson()}")
            val createDate = it.create
            if (!isToday(createDate)) {
                it.delete()
            }
        }
    }

    /**
     * 获取当天下载次数
     */
    suspend fun getTodayDownloadCount() = withContext(Dispatchers.IO) {
        val result = LitePal.findAll(DownloadRecord::class.java)
//        if (result.isEmpty()) {
//            logE("没有下载记录")
//        }
        return@withContext result.size
    }

//    private fun isToday(timestamp: Long): Boolean {
//        val currentTime = Date() // 获取当前日期和时间
//
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd") // 设置日期格式
//        val formattedCurrentTime = dateFormat.format(currentTime) // 格式化当前日期
//
//        val timestampDate = Date(timestamp * 1000L) // 将时间戳转换成Date对象
//        val formattedTimestampDate = dateFormat.format(timestampDate) // 格式化时间戳所表示的日期
//
//        return formattedCurrentTime == formattedTimestampDate // 比较两个日期字符串是否相等
//    }

}