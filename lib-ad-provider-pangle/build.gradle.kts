plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.ad.provider.pangle"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":lib-ad-core"))

    // Pangle (穿山甲/字节跳动)
    api(libs.pangle.sdk)
}
