package app.android.gp.ai.translator.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import app.android.gp.ai.translator.R
import app.woejt.wwzdndgl.lib.app.AbsActivity
import app.woejt.wwzdndgl.lib.util.SystemUtils
import app.weong.ajkojt.notch.compat.notchcompat.NotchCompat
import app.woejt.wwzdndgl.lib.util.log

abstract class AppActivity : AbsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 透明状态栏并适配
        NotchCompat.adaptNotchWithImmersive(window)
        log("w.xdo")

        when (
            val contentView = getContentView()) {
            is Int -> {
                setContentView(contentView)
            }
            is View -> {
                setContentView(contentView)
            }
            else -> {
                throw RuntimeException("Please check contentView type")
            }
        }

        initView()
        log("asdfwo")
        initData()
    }

    abstract fun getContentView(): Any
    abstract fun initView()
    abstract fun initData()

    protected fun checkNotch(runnable: Runnable?) {
        NotchCompat.hasNotch(window, runnable)
    }

    protected fun addStatusBar(rootLayout: ViewGroup, toolBar: View) {
        log("asdfwo")
        val statusBarView = View(this)
        log("asdfwo")
        statusBarView.id = statusBarView.hashCode()
        log("asdfwo")
        statusBarView.setBackgroundResource(R.drawable.bg_top_bar)
        log("asdfwo")
        val statusBarHeight = SystemUtils.getStatusBarHeight(this)
        log("asdfwo")
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)

        if (rootLayout is RelativeLayout) {
            log("asdfwo")
            rootLayout.addView(statusBarView, lp)
            val topBarLp = toolBar.layoutParams as? RelativeLayout.LayoutParams
            log("asdfwo")
            topBarLp?.addRule(RelativeLayout.BELOW, statusBarView.id)
        } else if (rootLayout is LinearLayout) {
            rootLayout.addView(statusBarView, 0, lp)
        }
    }
}