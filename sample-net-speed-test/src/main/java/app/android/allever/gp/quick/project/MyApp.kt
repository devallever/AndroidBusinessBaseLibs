package app.android.allever.gp.quick.project

import android.annotation.SuppressLint
import android.content.Context
import app.allever.android.lib.core.app.App
import org.litepal.LitePal

/**
 *@Description
 *@author: zq
 *@date: 2024/1/20
 */
@SuppressLint("StaticFieldLeak")
object MyApp {
    private var isInit = false
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
    fun init() {
        if (isInit) {
            return
        }
        context = App.context
        LitePal.initialize(context)
        SpeedTest.createRankData()
        isInit = true
    }
}