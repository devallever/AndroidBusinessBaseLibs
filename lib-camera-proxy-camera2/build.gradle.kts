plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.camera.proxy.camera2"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    // CameraX core library
    val camerax_version = "1.2.2"
    api ("androidx.camera:camera-core:${camerax_version}")
    // CameraX Camera2 extensions
    api("androidx.camera:camera-camera2:${camerax_version}")
    // CameraX Lifecycle library
    api ("androidx.camera:camera-lifecycle:${camerax_version}")
    // CameraX View class
    api ("androidx.camera:camera-view:${camerax_version}")
}
