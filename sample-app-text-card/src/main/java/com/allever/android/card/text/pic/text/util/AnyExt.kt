package com.allever.android.card.text.pic.text.util


fun Any.toJson(): String {
    return GsonHelper.toJson(this)
}