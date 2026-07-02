package com.funny.gif.memes.app

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.allever.app.gif.memes.R
import com.allever.lib.common.util.SystemUtils
import com.allever.lib.common.util.toast
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModel
import com.xm.lib.base.ui.BaseDataActivityKt

abstract class BaseDataActivity2<DB : ViewDataBinding, T : BaseViewModel<IBaseView>> :
    BaseDataActivityKt<DB, T>() {
//    override fun isStatusBarDark(): Boolean = true

    //使用了这个就不用重写initTopView
    override fun isPaddingTop(): Boolean = true
    override fun statusColor(): Int = R.color.color_11_white

    open fun getToolbarMenu(): Int = 0

    protected open fun initToolbar(toolbar: Toolbar, title: String?, menuId: Int = 0) {
        toolbar.title = title
        initToolbar(toolbar, menuId)
    }

    protected open fun initToolbar(toolbar: Toolbar, strId: Int, menuId: Int) {
        toolbar.setTitle(strId)
        initToolbar(toolbar, menuId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuId = getToolbarMenu()
        if (menuId != 0) {
            val inflater = menuInflater
            inflater.inflate(menuId, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    protected open fun initToolbar(toolbar: Toolbar, menuId: Int = 0) {
        if (menuId != 0) {
            toolbar.inflateMenu(menuId)
        }
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private var firstPressedBackTime = 0L
    protected fun checkExit(runnable: Runnable? = null) {
        if (System.currentTimeMillis() - firstPressedBackTime < 2000) {
            runnable?.run()
            super.onBackPressed()
        } else {
            toast(getString(com.allever.lib.common.R.string.common_click_again_to_exit))
            firstPressedBackTime = System.currentTimeMillis()
        }
    }

    protected fun setVisibility(view: View, show: Boolean) {
        if (show) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
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