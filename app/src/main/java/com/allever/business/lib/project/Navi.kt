package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.Router

object Navi {
    fun navigateTo(path: String) {
        Router.build(path).navigation(ActivityHelper.getTopActivity()?:App.context)
    }
}