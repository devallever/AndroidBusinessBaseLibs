plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.vpn"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))

//    val aarList = mutableListOf("unityLibrary-release.aar")
//    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))
}
