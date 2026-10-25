package org.xm.app.virtual.call.ui.mvp.presenter

import android.app.Activity
import org.xm.app.virtual.call.mvp.BasePresenter
import org.xm.app.virtual.call.ui.mvp.view.HomeView

class HomePresenter : BasePresenter<HomeView>() {
    fun requestPermission(activity: Activity, task: Runnable? = null) {
        task?.run()
    }
}