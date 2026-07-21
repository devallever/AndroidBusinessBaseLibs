plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.ad.pangle"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
    implementation(libs.play.services.ads.identifier)
    implementation(libs.pangle.sdk) {
        exclude(module = "tiktok-business-android-sdk-comp")
    }
    implementation(libs.glide)
}
