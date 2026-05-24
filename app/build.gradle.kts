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
    implementation(project(":sample-appsflyer"))
    implementation(project(":sample-adjust"))
    implementation(project(":sample-mvvm"))
    implementation(project(":sample-ad-admob"))
    implementation(project(":sample-ad-pangle"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
