package com.allever.business.lib.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppSplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setupFullscreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_splash)
        enableEdgeToEdge()
        lifecycleScope.launch {
            delay(300)
            startActivity(Intent(this@AppSplashActivity, MainActivity::class.java))
            finish()
        }

    }

    private fun setupFullscreen() {
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 隐藏状态栏和导航栏
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        // 隐藏系统 UI（沉浸式体验）
        hideSystemUI()
    }


    /**
     * 隐藏系统 UI（状态栏、导航栏等）
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }
}