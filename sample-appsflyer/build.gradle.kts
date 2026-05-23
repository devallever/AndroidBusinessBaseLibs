plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.appsflyer"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
    api(libs.af.android.sdk)
}
