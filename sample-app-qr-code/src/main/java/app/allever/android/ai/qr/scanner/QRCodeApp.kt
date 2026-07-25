package app.allever.android.ai.qr.scanner

import android.os.StrictMode
import app.allever.android.lib.core.app.App

object QRCodeApp {
    private var isInit = false
    fun onCreate() {
        if (isInit) {
            return
        }
        com.android.absbase.App.setContext(App.context)
        handler()
        isInit = true
    }

    private fun handler() {
        // 解决高版本手机出现以下问题:
        // android.os.FileUriExposedException: file:///storage/emulated/0/photo.jpeg exposed beyond app through ClipData.Item.getUri()
        // 重现路径, 分享联系人后EncodeActivity出现联系人二维码,点击分享
        try {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            builder.detectFileUriExposure()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}