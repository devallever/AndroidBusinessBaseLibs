plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.network.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-network-core"))
    implementation(project(":lib-network-engine-okhttp"))
}
