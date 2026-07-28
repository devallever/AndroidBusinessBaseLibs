package com.alsg.bakericon.db.entity

import androidx.annotation.Keep
import org.litepal.crud.LitePalSupport

/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
@Keep
class Favourite : LitePalSupport() {
    var path: String = ""
    var create: Long = 0L
}