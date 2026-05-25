plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.ad.applovin"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
    implementation(libs.applovin.sdk)
    implementation("com.applovin.mediation:bigoads-adapter:5.9.0.0")
//    implementation("com.applovin.mediation:google-adapter:25.3.0.0")
    implementation("com.applovin.mediation:bytedance-adapter:8.0.0.5.0")
    implementation(libs.play.services.ads.identifier)
    implementation(libs.adjust.android)
}
