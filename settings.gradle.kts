pluginManagement {
    repositories {
        maven( url = "https://maven.aliyun.com/nexus/content/groups/public/")
        maven( url = "https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven( url = "https://maven.aliyun.com/repository/google")
        maven( url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven( url = "https://jitpack.io")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven (url = "https://artifact.bytedance.com/repository/pangle")
        maven(url = "https://artifacts.applovin.com/android")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven( url = "https://maven.aliyun.com/nexus/content/groups/public/")
        maven( url = "https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven( url = "https://maven.aliyun.com/repository/google")
        maven( url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven( url = "https://jitpack.io")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven (url = "https://artifact.bytedance.com/repository/pangle")
        maven(url = "https://artifacts.applovin.com/android")
    }
}

rootProject.name = "AndroidBusinessBaseLibs"
include(":app")
include(":core")
include(":lib-mvvm")
include(":lib-ad-core")
include(":lib-ad-provider-admob")
include(":lib-ad-provider-pangle")
include(":lib-ad-provider-bigo")
include(":lib-ad-provider-applovin")
include(":lib-media-core")
include(":lib-media-picker")
include(":deprecated-lib-network")
include(":lib-network-core")
include(":lib-network-engine-huc")
include(":lib-network-engine-okhttp")
include(":lib-store-core")
include(":lib-store-engine-datastore")
include(":lib-store-engine-mmkv")
include(":lib-imageloader-core")
include(":lib-imageloader-engine-glide")
include(":lib-imageloader-engine-coil")
include(":lib-permission-engine-permissionx")
include(":lib-camera-proxy-camerax")
include(":lib-camera-proxy-camera2")

include(":sample-common")
include(":sample-appsflyer")
include(":sample-adjust")
include(":sample-mvvm")
include(":sample-ad-admob")
include(":sample-ad-pangle")
include(":sample-ad-bigo")
include(":sample-ad-applovin")
include(":sample-ad-core")
include(":sample-permission")
include(":sample-media-core")
include(":sample-network-core")
include(":sample-player-core")
include(":sample-audiovideo")
include(":sample-store-core")
include(":sample-imageloader-core")
include(":sample-camera-core")

include(":z-sample-billing")
include(":z-sample-cleaner")
include(":z-sample-designpattern")
include(":z-sample-function")
include(":z-lib-widget")
include(":z-sample-jetpack")
