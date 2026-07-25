package app.allever.android.ai.qr.scanner

import android.content.Context
import android.os.StrictMode
import androidx.multidex.MultiDex
import app.allever.android.lib.recommend.data.RecommendHelper
import app.android.base.lib.App
import com.allever.android.lib.admob.AdDevManager
import com.allever.android.lib.admob.AdManager

class QRCodeApp : App() {
    override fun onCreate() {
        super.onCreate()
        com.android.absbase.App.setContext(this)
        handler()
        AdManager.init(AdConfig(), this)
        AdDevManager.init(this)
        RecommendHelper.init(this)
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}