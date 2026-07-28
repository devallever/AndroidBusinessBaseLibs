package com.alsg.bakericon

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.CoroutineHelper
import com.alsg.bakericon.db.DBRepo
import com.alsg.bakericon.local.LocalRepo
import kotlinx.coroutines.launch
import org.litepal.LitePal

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
object MyApp {
    private var isInit = false
    fun init() {
        if (isInit) {
            return
        }
        LocalRepo.loadAll()
        LitePal.initialize(App.context)
        CoroutineHelper.DEFAULT.launch {
            DBRepo.clearOutDateDownload()
        }
        isInit = true
    }
}