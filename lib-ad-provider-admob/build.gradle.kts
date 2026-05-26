plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.ad.provider.admob"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":lib-ad-core"))

    // AdMob (Google Mobile Ads)
    api(libs.play.services.ads)
}
