package com.clean.wood.data

import android.content.Context
import com.clean.wood.utils.Constant

class ReferManager private constructor() {
    companion object {
        val ins by lazy {
            ReferManager()
        }
    }

    fun isReferUser(): Boolean {
        return true
    }

    fun checkRefer() {

    }
}