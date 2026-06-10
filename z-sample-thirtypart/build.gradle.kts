plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.thirtypart"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(libs.glide)
    implementation(libs.glide.transformations)
    implementation(libs.okhttp)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.4.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.0.0")
}
