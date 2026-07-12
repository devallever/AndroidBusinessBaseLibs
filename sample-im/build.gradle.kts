plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.serialization)
}

val modelPkg = "app.allever.android.sample.im"
group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-network-core"))
    implementation(project(":lib-network-engine-okhttp"))
    implementation(libs.java.websocket)
    implementation(libs.nanohttpd)
    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.ktx.serialization)
}
