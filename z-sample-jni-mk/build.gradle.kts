plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.jni.mk"

group = modelPkg

android {
    namespace = modelPkg

    defaultConfig {
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//            version = "3.18.1"
//        }

        //使用Android.mk 方式，打包时自动构建
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    //打包包含libs目录的so
    sourceSets.getByName("main") {
        jniLibs.setSrcDirs(jniLibs.srcDirs + files("$projectDir/libs"))
    }
}

dependencies {
    implementation(project(":sample-common"))
}
