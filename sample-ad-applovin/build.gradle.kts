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
    implementation(libs.play.services.ads.identifier)
    implementation(libs.adjust.android)
}
