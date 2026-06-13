plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.adjust"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":sample-common"))
    api(libs.adjust.android)
    //Install Referrer 是一种唯一标识符，可用来将安装归因至来源。
    implementation(libs.android.install.referrer)
    implementation(libs.play.services.ads.identifier)
}
