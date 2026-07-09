package app.android.gp.ai.translator.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import app.android.gp.ai.translator.R
import app.woejt.wwzdndgl.lib.mvp.BaseMvpActivity
import app.woejt.wwzdndgl.lib.mvp.BasePresenter
import app.woejt.wwzdndgl.lib.util.SystemUtils
import app.weong.ajkojt.notch.compat.notchcompat.NotchCompat
import app.woejt.wwzdndgl.lib.util.log

abstract class AppMvpActivity<V, P : BasePresenter<V>> : BaseMvpActivity<V, P>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        log("woow9520s.xje0w")
        // 透明状态栏
        NotchCompat.adaptNotchWithImmersive(window)
        log("woow9520s.xje0w")
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
        log("woow9520s.xje0w")
        initView()
        log("woow9520s.xje0w")
        initData()
    }

    abstract fun getContentView(): Any
    abstract fun initView()
    abstract fun initData()

    protected fun checkNotch(runnable: Runnable?) {
        NotchCompat.hasNotch(window, runnable)
    }

    protected fun addStatusBar(rootLayout: ViewGroup, toolBar: View) {
        val statusBarView = View(this)
        log("woow9520s.xje0w")
        statusBarView.id = statusBarView.hashCode()
        log("woow9520s.xje0w")
        statusBarView.setBackgroundResource(R.drawable.bg_top_bar)
        log("woow9520s.xje0w")
        val statusBarHeight = SystemUtils.getStatusBarHeight(this)
        log("woow9520s.xje0w")
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)

        if (rootLayout is RelativeLayout) {
            rootLayout.addView(statusBarView, lp)
            log("woow9520s.xje0w")
            val topBarLp = toolBar.layoutParams as? RelativeLayout.LayoutParams
            topBarLp?.addRule(RelativeLayout.BELOW, statusBarView.id)
        } else if (rootLayout is LinearLayout) {
            rootLayout.addView(statusBarView, 0, lp)
        }
        log("woow9520s.xje0w")
    }
}