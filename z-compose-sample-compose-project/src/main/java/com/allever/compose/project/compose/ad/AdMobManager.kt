package com.allever.compose.project.compose.ad

import android.content.Context
import com.google.android.gms.ads.MobileAds

object AdMobManager {
    private var isInit = false
    fun init(context: Context) {
        if (isInit) return
        MobileAds.initialize(context) {
        }
        isInit = true
    }
}