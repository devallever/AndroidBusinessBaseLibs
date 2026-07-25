package app.allever.android.ai.qr.scanner.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message

import androidx.lifecycle.lifecycleScope
import app.allever.android.ai.qr.scanner.AppActivity
import com.allever.app.qr.code.scaner.R
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_activity_splash)

        NotchCompat.adaptNotchWithFullScreen(window)

        lifecycleScope.launch {
            delay(2000)
            jumpToMain()
        }
    }

    private fun jumpToMain() {
        jumpToMainRunnable.run()
    }

    override fun onDestroy() {
        super.onDestroy()

        this.application?.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        this.handler.removeMessages(0)
    }

    private val jumpToMainRunnable = object : Runnable {
        override fun run() {
            val i = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private var handler = Handler(Looper.getMainLooper(), object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            this@SplashActivity.jumpToMainRunnable.run()
            return true
        }
    })

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityStarted(activity: Activity) {
            if (activity != null) {
                this@SplashActivity.handler.removeMessages(0)
            }
        }

        override fun onActivityDestroyed(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }
    }

}
