package app.allever.android.ai.qr.scanner

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.allever.app.qr.code.scaner.R
import app.android.base.lib.base.BaseActivity
import app.android.base.lib.util.SystemUtils
import app.android.base.lib.notchcompat.NotchCompat

abstract class AppActivity: BaseActivity() {

    protected fun checkNotch(runnable: Runnable?) {
        NotchCompat.hasNotch(window, runnable)
    }

    protected fun addStatusBar(rootLayout: ViewGroup, toolBar: View) {
        val statusBarView = View(this)
        statusBarView.id = statusBarView.hashCode()
        statusBarView.setBackgroundResource(R.drawable.top_bar_bg)
        val statusBarHeight = SystemUtils.getStatusBarHeight(this)
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)

        if (rootLayout is RelativeLayout) {
            rootLayout.addView(statusBarView, lp)
            val topBarLp = toolBar.layoutParams as? RelativeLayout.LayoutParams
            topBarLp?.addRule(RelativeLayout.BELOW, statusBarView.id)
        } else if (rootLayout is LinearLayout) {
            rootLayout.addView(statusBarView, 0, lp)
        }
    }
}