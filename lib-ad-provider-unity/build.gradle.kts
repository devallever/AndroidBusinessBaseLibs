plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.ad.provider.unity"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":lib-ad-core"))

    implementation("com.unity3d.ads:unity-ads:4.7.0")
}
