package com.alsg.bakericon.network

import com.allever.lib.base.function.network.ApiService
import com.allever.lib.base.function.network.internal.HttpHelper

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
object NetRepo {
    private val api by lazy {
        ApiService.get(Api::class.java)
    }

    suspend fun iconData() = HttpHelper.request {
        api.iconData()
    }

    suspend fun stickerData() = HttpHelper.request { api.stickerData() }

    suspend fun topData() = HttpHelper.request { api.topData() }


}