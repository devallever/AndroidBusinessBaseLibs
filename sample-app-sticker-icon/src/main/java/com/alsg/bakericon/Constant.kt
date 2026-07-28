package com.alsg.bakericon

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
object Constant {
    const val PRIVACY_URL = "https://www.baidu.com"
    const val FAQ_URL = "https://www.baidu.com"

    const val BASE_URL = "https://baker.app-lessfunc.uk/baker/sticker/icon/"
    const val ICON_URL = "icons.json"
    const val STICKER_URL = "stickers.json"
    const val TOP_URL = "top.json"

    const val ICON_PATH = "${BASE_URL}icons"
    const val STICKER_PATH = "${BASE_URL}stickers"
    const val TOP_PATH = "${BASE_URL}top"

    const val ACCEPT_FILE = "file:///android_asset"

    const val PRODUCT_WEEKLY = "gif_memes_weekly"//测试，对应包名 app.funny.tech.gif.memes
    const val PRODUCT_YEARLY = "gif_memes_yearly"//测试

    val PRODUCT_ID_LIST = mutableListOf(PRODUCT_WEEKLY, PRODUCT_YEARLY)

    const val DOWNLOAD_COUNT_EVERY_DAY = 3
}