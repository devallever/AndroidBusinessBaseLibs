// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
// ✅ 改用版本目录别名，不要再硬编码版本号
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
//        classpath (libs.applovinqualityservicegradleplugin)
//        classpath(libs.butterknife.gradle.plugin)
        classpath ("cn.therouter:plugin:1.3.2")
    }
}

subprojects {
    afterEvaluate {
        plugins.withId("com.android.library") {
            configureAndroidLibrary()
        }
        plugins.withId("com.android.application") {
            configureAndroidApplication()
        }
    }
}

fun Project.configureAndroidLibrary() {
    val libs = the<VersionCatalogsExtension>().named("libs")

    extensions.configure<com.android.build.api.dsl.LibraryExtension> {
        compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
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

        buildFeatures {
            viewBinding = true
            buildConfig = true
        }
    }

    // 配置 Kotlin 编译选项
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            }
        }
    }

    // ✅ 如果使用了 KSP，配置 TheRouter KSP 处理器
    plugins.withId("com.google.devtools.ksp") {
        configureTheRouterWithKsp()
    }
}

fun Project.configureAndroidApplication() {
    val libs = the<VersionCatalogsExtension>().named("libs")

    extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
        compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
            targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            debug {
                isMinifyEnabled = false
                // ✅ TheRouter 增量编译开关
                extra["enableTheRouterIncremental"] = true
            }

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

        buildFeatures {
            viewBinding = true
            buildConfig = true
            prefab = false
        }
    }

    // 配置 Kotlin 编译选项
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            }
        }
    }

    // ✅ 如果使用了 KSP，配置 TheRouter KSP 处理器
    plugins.withId("com.google.devtools.ksp") {
        configureTheRouterWithKsp()
    }
}

// ✅ 新的 KSP 配置函数（替代原来的 configureTheRouter）
fun Project.configureTheRouterWithKsp() {
    val libs = the<VersionCatalogsExtension>().named("libs")
    dependencies {
        // TheRouter 核心库
        "implementation"(libs.findLibrary("therouter.router").get())
        // ✅ 使用 KSP 替代 KAPT
        "ksp"(libs.findLibrary("therouter.apt").get())
    }
}