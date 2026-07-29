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
    }
}

dependencies {
    api(project(":core-compose"))
    implementation("com.google.accompanist:accompanist-pager:0.23.1")
}
