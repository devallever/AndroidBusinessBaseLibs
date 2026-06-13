plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.login"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(libs.google.play.services.auth)
    implementation(libs.facebook.login)
    implementation(libs.facebook.share)
    implementation(libs.facebook.fresco)
}
