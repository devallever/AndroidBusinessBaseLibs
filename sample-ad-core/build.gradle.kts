plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.ad.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-ad-core"))
    
    // Provider modules (按需引入)
    implementation(project(":lib-ad-provider-admob"))
    implementation(project(":lib-ad-provider-pangle"))
    implementation(project(":lib-ad-provider-bigo"))
}
