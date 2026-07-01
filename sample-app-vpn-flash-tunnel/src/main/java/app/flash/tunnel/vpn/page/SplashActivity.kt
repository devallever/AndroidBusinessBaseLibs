package app.flash.tunnel.vpn.page

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.databinding.ActivitySplashBinding
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.lib.common.util.ActivityManager
import kotlin.concurrent.thread

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun inflate() = ActivitySplashBinding.inflate(layoutInflater)

    override fun init() {

        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            return
        }
        thread {
            Thread.sleep(Constants.SPLASH_DELAY)
            runOnUiThread {
                ActivityManager.start(this@SplashActivity, HomeActivity::class.java)
                finish()
            }
        }

        onBackPressedDispatcher.addCallback {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //fix: 安装器打开APP后，点击home键返回桌面。再点击桌面图标打开APP后是重新启动。
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
    }
}