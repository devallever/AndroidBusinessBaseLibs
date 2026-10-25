package app.funnyappptech.android.fake.call.ui.mvp.presenter

import android.app.Activity
import app.funnyappptech.android.fake.call.mvp.BasePresenter
import app.funnyappptech.android.fake.call.ui.mvp.view.HomeView

class HomePresenter : BasePresenter<HomeView>() {
    fun requestPermission(activity: Activity, task: Runnable? = null) {
        task?.run()
    }
}