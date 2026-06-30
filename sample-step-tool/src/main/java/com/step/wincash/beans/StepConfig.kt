package com.step.wincash.beans

import androidx.annotation.Keep

@Keep
data class StepConfig(
    val smallRankWaiting: Int = 30,
    val countryList: MutableList<CountryConfig> = mutableListOf()
) {
}

@Keep
data class CountryConfig(
    val countryCode: String = "US",
    val largeAmountLevel1: Int = 50,
    val largeAmountLevel2: Int = 100,
    val largeGoldLevel1: Int = 5000,
    val largeGoldLevel2: Int = 10000,
    val largeGreenLevel1: Int = 50000,
    val largeGreenLevel2: Int = 100000,
    val smallAmount: Float = 0.1f,
    val smallGold: Int = 10,
    val smallVideoCount: Int = 10
)