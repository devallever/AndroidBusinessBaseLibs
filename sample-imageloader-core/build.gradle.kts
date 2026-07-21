plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.imageloader.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-imageloader-core"))
    implementation(project(":lib-imageloader-engine-glide"))
    implementation(project(":lib-imageloader-engine-coil"))
    implementation(project(":lib-media-picker"))
}
