package com.clean.wood.data.model

import com.google.gson.annotations.SerializedName

data class AdConfig(
    @SerializedName("fuliter1")
    val inter1: AdPositionConfig = AdPositionConfig(),
    @SerializedName("fuliter2")
    val inter2: AdPositionConfig = AdPositionConfig(),
    @SerializedName("fuliter3")
    val inter3: AdPositionConfig = AdPositionConfig(),
    @SerializedName("fuliter4")
    val inter4: AdPositionConfig = AdPositionConfig(),
    @SerializedName("fuliter5")
    val inter5: AdPositionConfig = AdPositionConfig(),
    @SerializedName("fuliterbak")
    val interBak: AdPositionConfig = AdPositionConfig(),
    @SerializedName("scrnav1")
    val native1: AdPositionConfig = AdPositionConfig(),
    @SerializedName("scrnav2")
    val native2: AdPositionConfig = AdPositionConfig(),
    @SerializedName("scrnav3")
    val native3: AdPositionConfig = AdPositionConfig(),
    @SerializedName("scrnav4")
    val native4: AdPositionConfig = AdPositionConfig(),
    @SerializedName("scrnavbak")
    val nativeBak: AdPositionConfig = AdPositionConfig(),
    @SerializedName("genbu")
    val allLimits: GlobalLimitConfig = GlobalLimitConfig()
)