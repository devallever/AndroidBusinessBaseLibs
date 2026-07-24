plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.camera.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-camera-core"))
    implementation(project(":lib-camera-proxy-camerax"))
    implementation(project(":lib-camera-proxy-camera2"))
    implementation(project(":lib-imageloader-core"))

}
