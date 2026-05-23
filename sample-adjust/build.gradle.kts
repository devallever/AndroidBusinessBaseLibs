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
    api("com.android.installreferrer:installreferrer:2.2")
    api("com.google.android.gms:play-services-ads-identifier:18.0.1")
}
