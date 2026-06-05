package deprecated.app.allever.android.lib.network.interceptor

import app.allever.android.lib.core.ext.logE
import okhttp3.logging.HttpLoggingInterceptor

class LoggerInterceptor : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        logE("LoggerInterceptor", message)
    }
}