plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.imageloader.engine.glide"
group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":lib-imageloader-core"))
    implementation(libs.glide)
}
