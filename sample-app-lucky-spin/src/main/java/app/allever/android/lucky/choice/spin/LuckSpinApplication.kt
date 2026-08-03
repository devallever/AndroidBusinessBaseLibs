package app.allever.android.lucky.choice.spin

import android.app.Application
import android.content.Context
import app.allever.android.lucky.choice.spin.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LuckSpinApplication : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        startKoin {
            androidLogger()
            androidContext(this@LuckSpinApplication)
            modules(appModule)
        }
    }
}