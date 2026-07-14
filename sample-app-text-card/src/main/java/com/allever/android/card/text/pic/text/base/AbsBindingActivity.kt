package com.allever.android.card.text.pic.text.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import app.allever.android.lib.core.base.AbstractActivity

abstract class AbsBindingActivity<VB : ViewBinding> : AbstractActivity() {

    protected val mBinding by lazy {
        inflate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        init()
    }

    abstract fun inflate(): VB

    abstract fun init()
}