package com.alsg.bakericon.network.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
@Keep
data class ResponseData(
    @SerializedName("t") val topic: String,
    @SerializedName("p") val path: String,
    @SerializedName("c") val count: Int
) {
}