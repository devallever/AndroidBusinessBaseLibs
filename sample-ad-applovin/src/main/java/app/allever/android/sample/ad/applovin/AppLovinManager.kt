package app.allever.android.sample.ad.applovin

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxSegment
import com.applovin.mediation.MaxSegmentCollection
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppLovinManager {

    private var isInit = false
    private var mIAdConfig: IAdConfig = TestAdConfig()
    private val SDK_KEY = "05TMDQ5tZabpXQ45_UTbmEGNUtVAzSTzT6KmWQc5_CuWdzccS4DCITZoL3yIWUG3bbq60QC_d4WF28tUC4gVTF"

    private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    fun init(iAdConfig: IAdConfig, block: () -> Unit) {
        mIAdConfig = iAdConfig
        if (isInit)  {
            log("AppLovin SDK already initialized")
            block()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Create the initialization configuration
            val initConfig = AppLovinSdkInitializationConfiguration.builder(SDK_KEY)
                .setMediationProvider(AppLovinMediationProvider.MAX)
                .setSegmentCollection(
                    MaxSegmentCollection.builder()
                    .addSegment(MaxSegment(849, listOf(1, 3)))
                    .build()
                )
                .build()

            // Configure the SDK settings if needed before or after SDK initialization.
//            val settings = AppLovinSdk.getInstance(App.context).settings
//            settings.userIdentifier = "«user-ID»"
//            settings.setExtraParameter("uid2_token", "«token-value»")
//            settings.termsAndPrivacyPolicyFlowSettings.apply {
//                isEnabled = true
//                privacyPolicyUri = Uri.parse("«https://your-company-name.com/privacy-policy»")
//                termsOfServiceUri = Uri.parse("«https://your-company-name.com/terms-of-service»")
//            }

            // Initialize the SDK with the configuration
            AppLovinSdk.getInstance(App.context).initialize(initConfig) { sdkConfig ->
                // Start loading ads
                log("AppLovin SDK initialized")

                // Initialize Adjust SDK
                val config = AdjustConfig(App.context, "{YourAppToken}", AdjustConfig.ENVIRONMENT_SANDBOX)
                Adjust.initSdk(config)

                App.app.registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())

                block()

                isInit = true
            }
        }
    }


}