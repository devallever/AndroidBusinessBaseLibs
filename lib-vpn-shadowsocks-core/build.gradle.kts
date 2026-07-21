plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
//    id("org.jetbrains.kotlin.plugin.parcelize")
}

val modelPkg = "com.github.shadowsocks.core"

group = modelPkg

android {
    namespace = modelPkg

    defaultConfig {
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
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
    implementation(libs.androidx.preference)
    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    //work
    api(libs.androidx.work.multiprocess)
    api(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.oss.licenses)
    //gson
    implementation(libs.gson)
    implementation(libs.jakewharton.timber)
    implementation(libs.dnsjava)
    //coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    //viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //kotlinx.parcelize
    implementation(libs.kotlin.parcelize.runtime)

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
