package com.clean.wood.data.model

import com.google.gson.annotations.SerializedName

data class AdPositionConfig(
    @SerializedName("onlyu")
    val adId: String = "",
    @SerializedName("sawd")
    val showLimited: Int = 0,
    @SerializedName("tapd")
    val clickLimited: Int = 0,
    @SerializedName("sours")
    val adSwitch: String = ""
)