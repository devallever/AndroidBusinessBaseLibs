plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.android.gp.ai.translator"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    api(project(":sample-common"))
    //retrofit
    api(libs.retrofit)
    //retrofit gson
    api(libs.retrofit.converter.gson)
    //okhttp
    api(libs.okhttp)
    //rx
    implementation("com.squareup.retrofit2:adapter-rxjava:2.1.0")
    implementation("io.reactivex:rxandroid:1.2.1")
    implementation("io.reactivex:rxjava:1.1.6")

    //eventbus
    api(libs.eventbus)
    //litepal
    api(libs.litepal.core)
}
