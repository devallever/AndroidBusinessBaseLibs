plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.mvvm"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
}
