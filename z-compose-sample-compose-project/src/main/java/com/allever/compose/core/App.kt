package com.allever.compose.core

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper

/**
 *@Description
 *@author: zq
 *@date: 2024/1/4
 */
abstract class App: Application() {
    override fun onCreate() {
        super.onCreate()

        init(this)

        init()
    }

    abstract fun init()

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var app: Application

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var mainHandler: Handler

        fun init(context: Context) {
            Companion.context = context.applicationContext
            mainHandler = Handler(Looper.getMainLooper())
            app = context as Application
        }
    }
}