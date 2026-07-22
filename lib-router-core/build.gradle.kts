plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.router"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":lib-router-annotation"))
    api(project(":core"))
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)
}