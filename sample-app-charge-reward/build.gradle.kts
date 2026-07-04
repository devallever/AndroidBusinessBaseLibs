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
}
