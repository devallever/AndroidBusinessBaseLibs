plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.imageloader.engine.coil"
group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":lib-imageloader-core"))
    // coil
    api(libs.coil)
    api(libs.coil.gif)
    api(libs.coil.svg)
    api(libs.coil.video)
}

