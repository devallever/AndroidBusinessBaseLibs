package app.flash.tunnel.vpn.helper

import android.annotation.SuppressLint
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.lib.common.util.runInIoDispatcher
//import com.google.firebase.FirebaseApp
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
//
object FirebaseHelper {

    private const val FIREBASE_CFG_KEY = "ftfrcfg"

    @SuppressLint("StaticFieldLeak")
//    private lateinit var mRemoteConfig: FirebaseRemoteConfig

    private val mFetchIntervalInSeconds =
        if (App.DEBUG || !TunnelHelper.hasLocalConfigCache()) 3 else (5 + 1) * 3600L

    fun init() {
//        FirebaseApp.initializeApp(TunnelApp.context)
//        mRemoteConfig = FirebaseRemoteConfig.getInstance()
//        mRemoteConfig.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().apply {
//            setMinimumFetchIntervalInSeconds(mFetchIntervalInSeconds)
//        }.build())
    }

    fun uploadException(throwable: Throwable?) {
        if (throwable == null) {
            return
        }

//        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun fetchConfig(block: (string: String?) -> Unit) {
//        mRemoteConfig.fetchAndActivate().addOnCompleteListener {
//            if (!it.isSuccessful) {
//                block(null)
//                log("fetch firebase fail $")
//                return@addOnCompleteListener
//            }
//
//            val content = mRemoteConfig.getString(FIREBASE_CFG_KEY)
//            log("fetch firebase success: $content")
//            runInIoDispatcher {
//                try {
//                    block(content)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
    }
}