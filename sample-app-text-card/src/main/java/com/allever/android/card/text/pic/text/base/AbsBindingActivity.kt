package com.allever.android.card.text.pic.text.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class AbsBindingActivity<VB : ViewBinding> : AppCompatActivity() {

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