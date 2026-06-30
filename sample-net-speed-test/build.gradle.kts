plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.android.allever.gp.quick.project"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":sample-common"))
    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    //gso
    implementation("org.jsoup:jsoup:1.14.3")
    //material
    implementation(libs.material)
    implementation("com.dinuscxj:circleprogressbar:1.3.0")
    //litepal
    implementation ("org.litepal.guolindev:core:3.2.3")
    //Location
    implementation ("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.libraries.places:places:3.5.0")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")
}
