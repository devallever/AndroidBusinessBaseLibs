plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.ad.unity"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
    implementation("com.unity3d.ads:unity-ads:4.7.0")
}
