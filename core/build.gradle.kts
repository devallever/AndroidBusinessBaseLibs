plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.allever.android.lib.core"
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
    //android & kotlin & google
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    //ActivityKtx

    //ConstraintLayout
    api("androidx.constraintlayout:constraintlayout:2.2.1")

    api("io.coil-kt:coil:2.1.0")
    api("io.coil-kt:coil-gif:2.1.0")
    api("io.coil-kt:coil-svg:2.1.0")
    api("io.coil-kt:coil-video:2.1.0")

    //ActivityKtx
    api("androidx.activity:activity-ktx:1.7.2")
    //FragmentKtx
    api("androidx.fragment:fragment-ktx:1.5.6")
    //RecyclerView
    api("androidx.recyclerview:recyclerview:1.3.0")
    //ViewPager2
    api("androidx.viewpager2:viewpager2:1.0.0")
    //LifecycleViewModelKtx
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    //LifecycleExtensions
    api("androidx.lifecycle:lifecycle-extensions:2.2.0")
    //LiveDataKtx
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    //CoroutinesCore
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    //CoroutinesAndroid
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    //dataStoreCore
    api("androidx.datastore:datastore-core:1.0.0")
    //dataStore
    api("androidx.datastore:datastore:1.0.0")
    //dataStorePreferences
    api("androidx.datastore:datastore-preferences:1.0.0")
    api("androidx.exifinterface:exifinterface:1.3.3")
    //paging
    api("androidx.paging:paging-runtime-ktx:3.1.1")

    //thirty part
    //okhttp
    api("com.squareup.okhttp3:okhttp:4.9.3")
    //okhttp3LoggingInterceptor
    api("com.squareup.okhttp3:logging-interceptor:4.9.3")
    //retrofit
    api("com.squareup.retrofit2:retrofit:2.9.0")
    //retrofitConverterGson
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    //retrofit2ConverterScalars
    api("com.squareup.retrofit2:converter-scalars:2.9.0")
    //gson
    api("com.google.code.gson:gson:2.10")
    //arouter
//    api("com.alibaba:arouter-api:1.5.2")
//    api("com.alibaba:arouter-annotation:1.5.2")
//    api("com.alibaba:arouter-compiler:1.5.2")
    //mmkv
    api("com.tencent:mmkv-static:1.2.14")
    //basepop
    api("io.github.razerdp:BasePopup:3.2.1")

}