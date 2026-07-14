package com.allever.android.card.text.pic.text.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class AppActivity<VB : ViewBinding, VM : ViewModel> : AbsVMActivity<VB, VM>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        super.onCreate(savedInstanceState)
    }
}