package com.example.charge.vm

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.charge.ChargeApp

object VMHelper {

    val hitMoleViewModel by lazy {
        ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(ChargeApp.instance)
        )[HitMoleViewModel::class.java]
    }

    val receiveCoinViewModel by lazy {
        ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(ChargeApp.instance)
        )[ReceiveCoinViewModel::class.java]
    }

    val taskViewModel by lazy {
        ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(ChargeApp.instance)
        )[TaskViewModel::class.java]
    }
}