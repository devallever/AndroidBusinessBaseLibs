plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
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
    implementation(libs.jsoup)
    //material
    implementation(libs.material)
    implementation (libs.circleprogressbar)
    //litepal
    implementation (libs.litepal.core)
    //Location
    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)
    implementation (libs.google.places)
    implementation (libs.android.maps.utils)
}
