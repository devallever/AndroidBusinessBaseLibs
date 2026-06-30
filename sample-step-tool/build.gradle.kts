plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
//    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.kotlin.serialization)
}

val modelPkg = "com.step.wincash"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":sample-common"))
    //cardview
    implementation(libs.androidx.cardview)

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

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)
    //lottie
    implementation(libs.airbnb.lottie)

    implementation(libs.jetbrains.kotlin.reflect)

    implementation(libs.material)
    //Room
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)

    implementation(libs.ktx.serialization)

    api(libs.mmkv.static)

    //腾讯vap
    implementation("io.github.tencent:vap:2.0.28")
    implementation(libs.simplepeng.picker)
}
