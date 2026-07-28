package com.alsg.bakericon.network

import com.alsg.bakericon.Constant
import com.alsg.bakericon.network.response.BaseResponse
import com.alsg.bakericon.network.response.ResponseData
import retrofit2.http.GET


interface Api {
    @GET(Constant.ICON_URL)
    suspend fun iconData(): BaseResponse<MutableList<ResponseData>>

    @GET(Constant.STICKER_URL)
    suspend fun stickerData(): BaseResponse<MutableList<ResponseData>>

    @GET(Constant.TOP_URL)
    suspend fun topData(): BaseResponse<Int>
}