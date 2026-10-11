plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

//app.allever.android.compose.sample.app.green.vpn
val modelPkg = "app.allever.android.compose.sample.app.tweet"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":sample-common-compose"))
}
