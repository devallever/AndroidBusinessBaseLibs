plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
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

//    val aarList = mutableListOf(
//        "toolkit-1.1.aar"
//    )
//    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

    //android
    implementation(libs.material)
    implementation(libs.androidx.cardview)

    //third
    implementation(libs.eventbus)

    implementation ("org.litepal.guolindev:core:3.2.3")

    // okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)

    //Butterknife
    implementation ("com.jakewharton:butterknife:10.2.3")
    annotationProcessor ("com.jakewharton:butterknife-compiler:10.2.3")
    //Rx

    //功能性RecyclerView
    implementation ("com.yanzhenjie.recyclerview:x:1.3.2")

}
