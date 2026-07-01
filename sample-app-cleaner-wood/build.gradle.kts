plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "com.clean.wood"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))


    implementation(libs.permissionx)
    implementation(libs.play.services.ads)
    implementation(libs.material)
}
