// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
            force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
        }
    }

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

    plugins.withId("com.google.devtools.ksp") {
        configureRouter()
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

    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            }
        }
    }

    plugins.withId("com.google.devtools.ksp") {
        configureRouter()
    }
}

fun Project.configureRouter() {
    dependencies {
        "implementation"(project(":lib-router-core"))
        "ksp"(project(":lib-router-compiler"))
    }
}