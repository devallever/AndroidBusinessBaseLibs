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
    implementation("com.google.android.gms:play-services-auth:20.4.1")
    implementation("com.facebook.android:facebook-login:18.0.3")
    implementation("com.facebook.android:facebook-share:18.0.3")
    implementation("com.facebook.fresco:fresco:3.6.0")
}
