package com.clean.wood.data.model

import com.google.gson.annotations.SerializedName

data class AdPositionLimitedRecord(
    @SerializedName("sawd")
    var showLimited: Int = 0,

    @SerializedName("tapd")
    var clickLimited: Int = 0
)