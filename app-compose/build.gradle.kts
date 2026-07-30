plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

val modelPkg = "com.allever.business.lib.project.compose"

group = modelPkg

android {
    namespace = modelPkg

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":core-compose"))
    implementation(project(":sample-common-compose"))

    implementation(project(":z-compose-sample-compose-project"))
}
