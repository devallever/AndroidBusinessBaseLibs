plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val modelPkg = "app.allever.android.lib.mvvm.compose"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":lib-mvvm"))
    api(project(":core-compose"))
}
