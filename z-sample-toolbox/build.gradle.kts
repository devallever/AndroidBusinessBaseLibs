plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "z.app.allever.android.sample.toolbox"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    //material
    implementation(libs.material)
    implementation(libs.quadflask.color.picker)
    implementation(libs.adw.library.discrete.seekbar)
    implementation(libs.tapadoo.alerter)
    implementation(libs.airbnb.lottie)
}
