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
    
    // 如果使用了 kapt，检查是否需要 ARouter 配置
    plugins.withId("org.jetbrains.kotlin.kapt") {
        configureArouter()
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
    
    // 如果使用了 kapt，检查是否需要 ARouter 配置
    plugins.withId("org.jetbrains.kotlin.kapt") {
        configureArouter(true)
    }
}

fun Project.configureArouter(isApplication: Boolean = false) {
    val libs = the<VersionCatalogsExtension>().named("libs")
    
    // 添加 ARouter 依赖
    dependencies {
        "implementation"(libs.findLibrary("arouter.api").get())
        "kapt"(libs.findLibrary("arouter.compiler").get())
    }
    
    // 配置 ARouter 编译参数
    if (isApplication) {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            defaultConfig {
                buildConfigField("boolean", "AROUTER_DEBUG", "true")
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments["AROUTER_MODULE_NAME"] = project.name
                    }
                }
            }
        }
    } else {
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            defaultConfig {
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments["AROUTER_MODULE_NAME"] = project.name
                    }
                }
            }
        }
    }
}
