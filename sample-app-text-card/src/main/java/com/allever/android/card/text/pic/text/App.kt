package com.allever.android.card.text.pic.text

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import app.allever.android.lib.core.app.App
import com.allever.android.card.text.pic.text.model.TemplateManager
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.util.StoreManager

@SuppressLint("StaticFieldLeak")
object App{
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context

    private var isInit = false

    fun getColor(colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }

    fun onCreate() {
        if (isInit) {
            return
        }
        context = App.context
        handleFirstOpen()
        TemplateManager.initData()
        isInit = true
    }

    private fun handleFirstOpen() {
        val firstOpen = StoreManager.getBoolean("first open", true)
        if (firstOpen) {
            TextCardCore.cardData.title = context.getString(R.string.tc_default_title)
            TextCardCore.cardData.text = context.getString(R.string.tc_default_text)
            TextCardCore.cardData.author = context.getString(R.string.tc_default_author)
            TextCardCore.saveCardData()
            StoreManager.putBoolean("first open", false)
        }
    }
}