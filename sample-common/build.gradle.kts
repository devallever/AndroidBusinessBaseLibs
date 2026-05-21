plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.allever.android.lib.common"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    api(project(":lib-mvvm"))
    //baseRecyclerViewAdapterHelper
    api("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.7")
    //flycoTabLayout
    api("com.github.li-xiaojun:FlycoTabLayout:2.0.6")
    //上拉加载/下拉刷新,核心必须依赖
    //refreshLayoutKernel
    api("io.github.scwang90:refresh-layout-kernel:2.0.5")
    //refreshHeaderClassics
    api("io.github.scwang90:refresh-header-classics:2.0.5")

}