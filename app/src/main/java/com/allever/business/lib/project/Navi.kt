package com.allever.business.lib.project

import com.therouter.TheRouter

object Navi {
    fun navigateTo(path: String) {
        TheRouter.build(path).navigation()
    }
}