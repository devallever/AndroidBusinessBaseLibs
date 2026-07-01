package app.android.allever.gp.quick.project

import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.android.allever.gp.quick.project.core.RankItem
import app.android.allever.gp.quick.project.core.Record
import org.litepal.LitePal


object SpeedTest {
    var record: Record? = null

    fun saveRecord(record: Record) {
        record.save()
    }

    fun getAllRecord(): List<Record> {
        //默认是asc 升序，即越来越大
        val list = LitePal.order("time desc").find(Record::class.java)
        list.map {
            log("getAllRecord: ${it.toJson()}")
        }
        return list
    }

    fun getAllByOrderTime(isAsc: Boolean = true): List<Record>  {
        val orderString = if (isAsc) {
            "time asc"
        } else {
            "time desc"
        }

        val list = LitePal.order(orderString).find(Record::class.java)
        list.map {
            log("getAllByOrderTime: ${it.toJson()}")
        }
        return list
    }

    fun getAllByOrderNetworkType(isAsc: Boolean = true): List<Record>  {
        val orderString = if (isAsc) {
            "networkType asc"
        } else {
            "networkType desc"
        }

        val list = LitePal.order(orderString).find(Record::class.java)
        list.map {
            log("getAllByOrderNetworkType: ${it.toJson()}")
        }
        return list
    }

    fun getAllByOrderDownloadSpeed(isAsc: Boolean = true): List<Record>  {
        val orderString = if (isAsc) {
            "downloadSpeed asc"
        } else {
            "downloadSpeed desc"
        }

        val list = LitePal.order(orderString).find(Record::class.java)
        list.map {
            log("getAllByOrderDownloadSpeed: ${it.toJson()}")
        }
        return list
    }

    fun getAllByOrderUploadSpeed(isAsc: Boolean = true): List<Record>  {
        val orderString = if (isAsc) {
            "uploadSpeed asc"
        } else {
            "uploadSpeed desc"
        }

        val list = LitePal.order(orderString).find(Record::class.java)
        list.map {
            log("getAllByOrderUploadSpeed: ${it.toJson()}")
        }
        return list
    }

    fun deleteAllRecord() {
        LitePal.deleteAll(Record::class.java)
    }


    val rankData = mutableListOf<RankItem>()
    private val OPERATOR_LIST = listOf("联通", "电信", "移动")
    private val NETWORK_TYPE_LIST = listOf("WIFI", "5G", "4G")
    private val MODEL_LIST = listOf(
        "iPhone 15", "iPhone 15 Pro", "iPhone 15 Pro Max", "iPhone 15 mini",
        "iPhone 14", "iPhone 14 Pro", "iPhone 14 Pro Max", "iPhone 14 mini",
        "iPhone 13", "iPhone 13 Pro", "iPhone 13 Pro Max", "iPhone 13 mini",
        "iPhone 12", "iPhone 12 Pro", "iPhone 12 Pro Max", "iPhone 12 mini",
        "iPhone 11", "iPhone X", "iPhone 8", "iPhone 8 Plus", "iPhone7", "iPhone 7 Plus",
        "XIAOMI 14", "XIAOMI 14 Pro", "XIAOMI 13", "XIAOMI 13 Pro", "XIAOMI 12", "XIAOMI 10",
        "RedMi K60", "RedMi K60 Pro",  "RedMi K50", "RedMi K50 Pro",  "RedMi K40", "RedMi K40 Pro",  "RedMi K40", "RedMi K40 Pro",
        "Samsung Galaxy S20", "Samsung Galaxy S21", "Samsung Galaxy S10",
        "Huawei P60","Huawei P50","Huawei P60","Huawei P40","Huawei P30","Huawei P20",
        "Huawei Mate 40", "Huawei Mate 30", "Huawei Mate 20", "Huawei Mate 10",
        "OPPO FindX3", "OPPO FindX2", "OPPO FindX1","OPPO Reno6","OPPO Reno5", "OPPO Reno4", "OPPO Reno3", "OPPO Reno2",
        "VIVO X60", "VIVO X50", "VIVO X40", "VIVO X30")
    fun createRankData() {
        for (i in  0 .. 19) {
            val downloadSpeed = (1900..2200).random().toDouble()
            val uploadSpeed = (60..350).random().toDouble()
            val networkType = "${OPERATOR_LIST.random()} ${NETWORK_TYPE_LIST.random()}"
            val model = MODEL_LIST.random()
            val time = if (System.currentTimeMillis() % 2 == 0L) {
                "${(2..99).random()}月前"
            } else {
                "${(1..30).random()} 天前"
            }
            rankData.add(RankItem(downloadSpeed, uploadSpeed, networkType, model, time))
        }

        rankData.sortBy {
            it.downloadSpeed
        }

        rankData.reverse()

        rankData.map {
            log("rankItem: ${it.toJson()}")
        }
    }


}