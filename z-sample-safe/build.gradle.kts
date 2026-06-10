plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.safe"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
}
