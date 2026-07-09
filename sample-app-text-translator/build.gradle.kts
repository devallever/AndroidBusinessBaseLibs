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
    //lib rxjava
    //eventbus
    api(libs.eventbus)
    //litepal
    api(libs.litepal.core)
    //material
    api(libs.material)
}
