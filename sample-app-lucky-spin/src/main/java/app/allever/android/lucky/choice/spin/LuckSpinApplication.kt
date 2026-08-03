package app.allever.android.lucky.choice.spin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import app.allever.android.lib.core.app.App
import app.allever.android.lucky.choice.spin.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@SuppressLint("StaticFieldLeak")
object LuckSpinApplication {

    val context = App.context

    private var isInit = false

    fun onCreate() {
        if (isInit) {
            return
        }
        startKoin {
            androidLogger()
            androidContext(context)
            modules(appModule)
        }
        isInit = true
    }
}