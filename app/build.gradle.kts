plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
//    id("applovin-quality-service")//编译时检查key安全
}

//applovin {
//    //书歌测试key
//    //qWSEfQRhBblNPZKpX0Ikm5pk8K3XiUFmAdTwLLitAgT-nZdIMIqoN2-RpCdO0qTocL5Nd3KL04gddZQnszhiH-
//    apiKey = "wQSEfQRhBblNPZKpX0Ikm5pk8K3XiUFmAdTwLLitAgT-nZdIMIqoN2-RpCdO0qTocL5Nd3KL04gddZQnszhiH-"
//。  //Your SafeDK API key is invalid
//}

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
    implementation(project(":lib-ad-core"))
    implementation(project(":sample-common"))
//    implementation(project(":sample-appsflyer"))
//    implementation(project(":sample-ad-applovin"))
//    implementation(project(":sample-adjust"))

//    implementation(project(":sample-mvvm"))
//    implementation(project(":sample-ad-admob"))
//    implementation(project(":sample-ad-pangle"))
//    implementation(project(":sample-ad-bigo"))
//    implementation(project(":sample-ad-core"))
    //permission
//    implementation(project(":sample-permission"))
//    implementation(project(":sample-media-core"))
//    implementation(project(":sample-network-core"))
//    implementation(project(":sample-player-core"))
//    implementation(project(":sample-audiovideo"))
//    implementation(project(":sample-store-core"))
//    implementation(project(":sample-imageloader-core"))
//    implementation(project(":sample-camera-core"))
    implementation(project(":z-sample-billing"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation (libs.leakcanary.android)
}
