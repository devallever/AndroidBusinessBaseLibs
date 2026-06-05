plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.network.engine.okhttp"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    api(project(":lib-network-core"))
    implementation(libs.okhttp)
}
