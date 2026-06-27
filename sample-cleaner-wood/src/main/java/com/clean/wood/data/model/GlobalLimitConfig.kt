package com.clean.wood.data.model

import com.google.gson.annotations.SerializedName

data class GlobalLimitConfig(
    @SerializedName("sawd")
    val allShowLimited: Int = 0,
    @SerializedName("tapd")
    val allClickLimited: Int = 0
)