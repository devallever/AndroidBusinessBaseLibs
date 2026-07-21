plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.ipc"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        aidl = true
    }
}

dependencies {
    api(project(":sample-common"))
}
