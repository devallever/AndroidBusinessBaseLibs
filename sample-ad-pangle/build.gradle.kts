plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.ad.pangle"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
    implementation(libs.play.services.ads.identifier)
    implementation("com.pangle.global:pag-sdk:8.0.0.4") {
        exclude(module = "tiktok-business-android-sdk-comp")
    }
    implementation(libs.glide)
}
