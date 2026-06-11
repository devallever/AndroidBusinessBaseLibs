plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.toolbox"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation("com.github.QuadFlask:colorpicker:0.0.15")
    implementation("org.adw.library:discrete-seekbar:1.0.1")
    implementation("com.tapadoo.android:alerter:7.0.1")
    implementation("com.airbnb.android:lottie:3.4.0")
}
