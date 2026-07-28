package com.alsg.bakericon.db.entity

import androidx.annotation.Keep
import org.litepal.crud.LitePalSupport

/**
 *@Description
 *@author: zq
 *@date: 2024/1/18
 */
@Keep
class DownloadRecord : LitePalSupport() {
    var create: Long = 0L
}