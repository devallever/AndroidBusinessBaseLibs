package app.android.gp.ai.translator.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.util.BarUtils
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.mvp.BaseMvpActivity
import app.android.gp.ai.translator.app.mvp.BasePresenter

abstract class AppMvpActivity<V, P : BasePresenter<V>> : BaseMvpActivity<V, P>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        initData()
    }

    abstract fun getContentView(): Any
    abstract fun initView()
    abstract fun initData()

    protected fun addStatusBar(rootLayout: ViewGroup, toolBar: View) {
        val statusBarView = View(this)
        log("woow9520s.xje0w")
        statusBarView.id = statusBarView.hashCode()
        log("woow9520s.xje0w")
        statusBarView.setBackgroundResource(R.drawable.bg_top_bar)
        log("woow9520s.xje0w")
        val statusBarHeight = BarUtils.getStatusBarHeight()
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