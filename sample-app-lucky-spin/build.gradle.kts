plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.lucky.choice.spin"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":sample-common"))

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.koin.android)
    implementation(libs.markown.core)


    implementation(libs.material)
    //Room
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    ksp (libs.androidx.room.compiler)
}
