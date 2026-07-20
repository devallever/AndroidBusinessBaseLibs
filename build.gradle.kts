// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
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
    
    // 如果使用了 kapt，配置 TheRouter 注解处理器
    plugins.withId("org.jetbrains.kotlin.kapt") {
        configureTheRouter()
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

    // 如果使用了 kapt，配置 TheRouter 注解处理器
    plugins.withId("org.jetbrains.kotlin.kapt") {
        configureTheRouter()
    }
}

fun Project.configureTheRouter() {
    val libs = the<VersionCatalogsExtension>().named("libs")
    dependencies {
        "implementation"(libs.findLibrary("therouter.router").get())
        "kapt"(libs.findLibrary("therouter.apt").get())
    }
}
