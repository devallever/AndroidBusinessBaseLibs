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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }

        // AGP 8.x 默认 useLegacyPackaging=false，导致 SO 不被提取到文件系统
        // 必须设为 true，否则 nativeLibraryDir 下找不到 .so 文件
        //fix: /lib/x86_64/libsslocal.so" (in directory "/data/user_de/0/com.swimpp.proxysafeline.application/no_backup"): error=2, No such file or directory
        //https://github.com/shadowsocks/shadowsocks-android/issues/3066
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":sample-common"))
    implementation(project(":sample-permission"))

//    implementation(project(":sample-appsflyer"))
//    implementation(project(":sample-ad-applovin"))
//    implementation(project(":sample-adjust"))

//    implementation(project(":sample-mvvm"))
//    implementation(project(":sample-ad-admob"))
//    implementation(project(":sample-ad-pangle"))
//    implementation(project(":sample-ad-bigo"))
//    implementation(project(":sample-ad-core"))
//    implementation(project(":sample-media-core"))
//    implementation(project(":sample-network-core"))
//    implementation(project(":sample-player-core"))
//    implementation(project(":sample-audiovideo"))
//    implementation(project(":sample-store-core"))
//    implementation(project(":sample-imageloader-core"))
//    implementation(project(":sample-camera-core"))
//    implementation(project(":sample-unity"))
    implementation(project(":sample-vpn"))
    implementation(project(":lib-vpn-shadowsocks-core"))
//    implementation(project(":z-sample-billing"))
//    implementation(project(":z-sample-cleaner"))
//    implementation(project(":z-sample-designpattern"))
//    implementation(project(":z-sample-function"))
//    implementation(project(":z-sample-jetpack"))
//    implementation(project(":z-sample-jni"))
//    implementation(project(":z-sample-jni-mk"))
//    implementation(project(":z-sample-kotlin"))
//    implementation(project(":z-sample-learning-android"))
//    implementation(project(":z-sample-login"))
//    implementation(project(":z-sample-material-design"))
//    implementation(project(":z-sample-thirtypart"))
//    implementation(project(":z-sample-safe"))
//    implementation(project(":z-sample-ui"))
//    implementation(project(":z-sample-toolbox"))
//    implementation(project(":z-sample-videoeditor"))
//    implementation(project(":z-sample-microsoft-speech"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
//    debugImplementation (libs.leakcanary.android)
}
