plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "com.allever.daymatter"

group = modelPkg

android {
    namespace = modelPkg

    //打包包含libs目录的so
    sourceSets.getByName("main") {
        jniLibs.setSrcDirs(jniLibs.srcDirs + files("$projectDir/libs"))
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":z-lib-widget"))

    //android
    implementation(libs.androidx.cardview)

    //third
    implementation(libs.eventbus)

    implementation (libs.litepal.core)

    // okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)

    //Rx

    //功能性RecyclerView
    implementation (libs.recyclerviewx)

}
