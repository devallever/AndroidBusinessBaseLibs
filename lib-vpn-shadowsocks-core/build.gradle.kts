plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
//    id("org.jetbrains.kotlin.plugin.parcelize")
}

val modelPkg = "com.github.shadowsocks.core"

group = modelPkg

android {
    namespace = modelPkg

    defaultConfig {
        kapt {
            //kapt 处理 AIDL 生成的 TrafficStats.java 时，遇到 Kotlin 元数据注解中引用的类不存在（ @error.NonExistentClass() ），导致编译失败。
            //kapt 在处理 AIDL 生成的 Java 文件时，遇到了不存在的注解类。最简单的修复方式是启用 correctErrorTypes ：
            correctErrorTypes = true
            arguments {
                arg("room.incremental", true)
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    //jniLibs目录指向libs目录
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    sourceSets.getByName("androidTest") {
        assets.setSrcDirs(assets.srcDirs + files("$projectDir/schemas"))
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    //material
    implementation(libs.material)
    //preference
    implementation("androidx.preference:preference:1.2.0")
    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    //work
    api("androidx.work:work-multiprocess:2.7.1")
    api("androidx.work:work-runtime-ktx:2.7.1")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    //gson
    implementation(libs.gson)
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("dnsjava:dnsjava:3.5.2")
    //coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    //viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //kotlinx.parcelize
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.0.21")

//    implementation("androidx.core:core-ktx:1.9.0")
//    implementation("com.google.android.material:material:1.8.0")
//    implementation("androidx.preference:preference:1.2.0")
//    implementation("androidx.room:room-runtime:2.5.0")
//    implementation("androidx.room:room-compiler:2.5.0")
//    implementation("androidx.work:work-multiprocess:2.7.1")
//    implementation("androidx.work:work-runtime-ktx:2.7.1")
//    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
//    implementation("com.google.code.gson:gson:2.10.1")
//    implementation("com.jakewharton.timber:timber:5.0.1")
//    implementation("dnsjava:dnsjava:3.5.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
}
