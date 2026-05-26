plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.ad.provider.bigo"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":lib-ad-core"))

    // Bigo Ads
    api(libs.bigo.ads)
}
