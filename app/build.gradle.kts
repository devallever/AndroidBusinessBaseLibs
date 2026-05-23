plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "com.allever.business.lib.project"
group = modelPkg

android {
    namespace = modelPkg
    defaultConfig {
        applicationId = modelPkg
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":lib-mvvm"))
    implementation(project(":sample-common"))
    api(project(":sample-appsflyer"))
    api(project(":sample-adjust"))
    api(project(":sample-mvvm"))
    api(project(":sample-ad-admob"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
