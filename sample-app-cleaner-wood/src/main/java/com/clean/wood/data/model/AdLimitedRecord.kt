package com.clean.wood.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class AdLimitedRecord(
    @SerializedName("fuliter1")
    var inter1: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("fuliter2")
    var inter2: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("fuliter3")
    var inter3: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("fuliter4")
    var inter4: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("fuliter5")
    var inter5: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("scrnav1")
    var native1: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("scrnav2")
    var native2: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("scrnav3")
    var native3: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("scrnav4")
    var native4: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("whateverignorebak")
    var bakPlaceHoler: AdPositionLimitedRecord = AdPositionLimitedRecord(),

    @SerializedName("allsawd")
    var allShowLimited: Int = 0,

    @SerializedName("alltapd")
    var allClickLimited: Int = 0,

    @SerializedName("recordDate")
    var recordDate: LocalDate = LocalDate.now()
)