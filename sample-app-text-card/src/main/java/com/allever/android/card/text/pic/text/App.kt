package com.allever.android.card.text.pic.text

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import app.allever.android.lib.recommend.data.RecommendHelper
import com.allever.android.card.text.pic.text.ad.AdConfig
import com.allever.android.card.text.pic.text.model.TemplateManager
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.util.StoreManager
import com.allever.android.lib.admob.AdManager

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        fun getColor(colorResId: Int): Int {
            return ContextCompat.getColor(context, colorResId)
        }
    }

    override fun onCreate() {
        context = this
        super.onCreate()
        AdManager.init(AdConfig(), this)
        handleFirstOpen()
        TemplateManager.initData()
        RecommendHelper.init(this)
    }

    private fun handleFirstOpen() {
        val firstOpen = StoreManager.getBoolean("first open", true)
        if (firstOpen) {
            TextCardCore.cardData.title = getString(R.string.default_title)
            TextCardCore.cardData.text = getString(R.string.default_text)
            TextCardCore.cardData.author = getString(R.string.default_author)
            TextCardCore.saveCardData()
            StoreManager.putBoolean("first open", false)
        }
    }
}