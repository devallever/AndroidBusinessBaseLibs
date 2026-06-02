plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.ad.provider.applovin"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":lib-ad-core"))
    implementation(libs.applovin.sdk)
//    implementation("com.applovin.mediation:bigoads-adapter:5.9.0.0")
//    implementation("com.applovin.mediation:google-adapter:25.3.0.0")
//    implementation("com.applovin.mediation:bytedance-adapter:8.0.0.5.0")
    implementation(libs.play.services.ads.identifier)
}
