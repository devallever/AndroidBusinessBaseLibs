plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "deprecated.app.allever.android.lib.network"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    // okhttp
    implementation(libs.okhttp)
    api(libs.okhttp.logging.interceptor)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)

    // gson
    implementation(libs.gson)

    // thirty party
    implementation(libs.mmkv.static)
}
