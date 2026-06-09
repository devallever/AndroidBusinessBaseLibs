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
    api (libs.androidx.camera.core)
    // CameraX Camera2 extensions
    api(libs.androidx.camera.camera2)
    // CameraX Lifecycle library
    api (libs.androidx.camera.lifecycle)
    // CameraX View class
    api (libs.androidx.camera.view)
}
