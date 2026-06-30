package app.android.allever.gp.quick.project

import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.GsonHelper
import app.allever.android.lib.core.store.StoreCore
import app.android.allever.gp.quick.project.core.ServerData
import pk.farimarwat.speedtest.models.STServer

object Config {

    private const val KEY_SERVER_LIST = "KEY_SERVER_LIST"
    private const val KEY_LAST_SUCCESS_SERVER = "KEY_LAST_SUCCESS_SERVER"
    private const val KEY_MY_RANK = "KEY_MY_RANK"
    fun saveServerList(list: List<STServer>) {
        val json = ServerData(list).toJson()
        log("saveServerList: $json")
        StoreCore.putString(KEY_SERVER_LIST, json)
    }

    fun getServerList(): List<STServer> {
        val json = StoreCore.getString(KEY_SERVER_LIST)
        val data = json?.let { GsonHelper.fromJson(it, ServerData::class.java) }
        return data?.list ?: mutableListOf()
    }

    fun saveSuccessServer(url: String) {
        log("saveSuccessServer: $url")
        StoreCore.putString(KEY_LAST_SUCCESS_SERVER, url)
    }

    fun getLastSuccessServer(): String {
        return StoreCore.getString(KEY_LAST_SUCCESS_SERVER)?:""
    }

    fun getMyRank(): String {
        val rank = StoreCore.getString(KEY_MY_RANK)
        if (rank?.isEmpty() == true) {
            StoreCore.putString(KEY_MY_RANK, "${(100..200).random()}万")
        }

        return StoreCore.getString(KEY_MY_RANK)?:""
    }
}