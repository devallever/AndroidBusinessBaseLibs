plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

val modelPkg = "z.compose.app.allever.android.sample.compose.project"

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
    api(project(":core-compose"))
    implementation(project(":sample-common-compose"))
    implementation(libs.play.services.ads)
}
