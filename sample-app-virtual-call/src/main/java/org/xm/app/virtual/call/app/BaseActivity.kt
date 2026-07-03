package org.xm.app.virtual.call.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.util.BarUtils
import com.allever.app.virtual.call.R
import org.xm.app.virtual.call.mvp.BaseMvpActivity
import org.xm.app.virtual.call.mvp.BasePresenter

abstract class BaseActivity<V, P : BasePresenter<V>> : BaseMvpActivity<V, P>() {

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

        //全屏显示并适配刘海屏
        NotchCompat.adaptNotchWithFullScreen(window)

        initView()
        initData()
    }

    abstract fun getContentView(): Any
    abstract fun initView()
    abstract fun initData()

    protected fun addStatusBar(rootLayout: ViewGroup): View {
        val statusBarView = View(this)
        statusBarView.id = statusBarView.hashCode()
        statusBarView.setBackgroundResource(R.drawable.vc_top_bar_bg)
        val statusBarHeight = BarUtils.getStatusBarHeight()
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)

        if (rootLayout is RelativeLayout) {
            rootLayout.addView(statusBarView, lp)
        } else if (rootLayout is LinearLayout) {
            rootLayout.addView(statusBarView, 0, lp)
        }
        return statusBarView
    }

    protected fun checkNotch(runnable: Runnable?) {
        NotchCompat.hasNotch(window, runnable)
    }

}