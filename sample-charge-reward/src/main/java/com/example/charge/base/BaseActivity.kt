package com.example.charge.base

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import app.allever.android.lib.core.app.App
import com.example.charge.utils.LocaleManager
import com.example.charge.utils.dp2px
import com.example.charge.utils.getStatusBarHeight
import com.example.charge.utils.log
import org.greenrobot.eventbus.EventBus

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    lateinit var binding: T
    var isShow = false

    companion object {
        inline fun <reified A : BaseActivity<*>> goTo(context: Context) {
            val intent = Intent(context, A::class.java)
            context.startActivity(intent)
        }

        // 如果需要传递额外参数
        inline fun <reified A : BaseActivity<*>> goTo(
            context: Context,
            block: Intent.() -> Unit
        ) {
            val intent = Intent(context, A::class.java)
            intent.block()
            context.startActivity(intent)
        }
    }

    abstract fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): T

    fun setSystemBarsPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (systemBars.top == 0) {
                v.setPadding(0, 138, 0, systemBars.bottom)
            } else {
                v.setPadding(0, systemBars.top + 20, 0, systemBars.bottom)
            }
            insets
        }
    }

    fun setNavigationBarsPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets: WindowInsetsCompat ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(
                navigationBars.left,
                navigationBars.top,
                navigationBars.right,
                navigationBars.bottom
            )
            insets
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (App.DEBUG) {
            log("${this.javaClass.simpleName}->${this.hashCode()}：onCreate()")
        }
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        hideSystemUI()
        fullActivity()
        binding = getBinding(LayoutInflater.from(this), null)
//        setSystemBarsPadding(binding.root)
        setContentView(binding.root)
        setBackGround()
        if (enableEventBus()) {
            registerEventbus()
        }
        initView()
    }

    fun setBackGround() {
//        binding.root.setBackgroundResource(R.drawable.ic_bg)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.let { LocaleManager.wrap(it) }
        )
    }

    override fun onResume() {
        super.onResume()
        isShow = true
        if (App.DEBUG) {
            log("${this.javaClass.simpleName}->${this.hashCode()}：onResume()")
        }
    }

    override fun onPause() {
        super.onPause()
        isShow = false
        if (App.DEBUG) {
            log("${this.javaClass.simpleName}->${this.hashCode()}：onPause()")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterEventbus()
        if (App.DEBUG) {
            log("${this.javaClass.simpleName}->${this.hashCode()}：onDestroy()")
        }
    }

    protected fun registerEventbus() {
        EventBus.getDefault().register(this)
    }

    protected fun unregisterEventbus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    protected fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    protected fun fullActivity() {
        val window = window
        val lp = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.addFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES or WindowManager.LayoutParams.FLAG_FULLSCREEN)
            // 如果是 Android 9.0，则需要对刘海屏进行适配，否则也会导致 WindowManager 移动不到刘海屏的位置上面
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            getWindow().decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    protected fun fixStatusBar(targetView: View, appendDp: Int = 0) {
        targetView.post {
            val statusBarHeight = getStatusBarHeight(this)
            val lp = targetView.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = statusBarHeight + dp2px(appendDp.toFloat())
            targetView.layoutParams = lp
        }
    }

    protected open fun enableEventBus() = false

    abstract fun initView()

}