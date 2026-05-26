plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.ad.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))

    // AdMob (Google Mobile Ads)
    api(libs.play.services.ads)

    // Pangle (穿山甲/字节跳动)
    api(libs.pangle.sdk)

    // Bigo Ads
    api(libs.bigo.ads)
}
