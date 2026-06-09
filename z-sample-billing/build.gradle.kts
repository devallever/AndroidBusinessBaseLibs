plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.billing"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation("com.android.billingclient:billing-ktx:5.1.0")
}
