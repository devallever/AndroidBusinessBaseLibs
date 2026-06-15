plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "com.example.charge"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    val aarList = mutableListOf("dusdk_v8.8.4.aar", "lib-debug.aar")
    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

    //cardView
//    implementation(libs.androidx.cardview)
    //cardview
    implementation("androidx.cardview:cardview:1.0.0")

    //gson
    implementation(libs.gson)
    //xpop
    implementation(libs.xpopup)
    //recyclerviewbaseadapter
    implementation(libs.baserecyclerviewadapterhelper)
    //glide
    implementation(libs.glide)
    //eventbus
    implementation(libs.eventbus)

    implementation(libs.shapeView)
    implementation(libs.shapeDrawable)

    //okhttp
    implementation(libs.okhttp)
    //网络框架
    implementation(libs.easyhttp)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)
    //lottie
    implementation(libs.airbnb.lottie)

    implementation(libs.getactivity.gsonfactory)
    implementation(libs.jetbrains.kotlin.reflect)
}
