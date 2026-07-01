package org.xm.secret.photo.album.ui

import android.os.Bundle
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.helper.ActivityHelper
import org.xm.secret.photo.album.R
import org.xm.secret.photo.album.function.password.PasswordConfig

class SplashActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sa_activity_splash)

        NotchCompat.adaptNotchWithFullScreen(window)

        mHandler.postDelayed({
            PasswordConfig.secretCheckPass = false
            ActivityHelper.startActivity<MainActivity>()
            finish()
        }, 500)
    }
}