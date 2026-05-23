plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.mvvm"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
}
