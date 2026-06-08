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
include(":sample-common")
include(":sample-appsflyer")
include(":sample-adjust")
include(":sample-mvvm")
include(":sample-ad-admob")
include(":sample-ad-pangle")
include(":sample-ad-bigo")
include(":sample-ad-applovin")
include(":lib-ad-core")
include(":lib-ad-provider-admob")
include(":lib-ad-provider-pangle")
include(":lib-ad-provider-bigo")
include(":sample-ad-core")
include(":lib-ad-provider-applovin")
include(":sample-permission")
include(":lib-media-core")
include(":sample-media-core")
include(":lib-media-picker")
include(":deprecated-lib-network")
include(":lib-network-core")
include(":lib-network-engine-huc")
include(":lib-network-engine-okhttp")
include(":sample-network-core")
include(":sample-player-core")
include(":sample-audiovideo")
include(":lib-store-core")
include(":lib-store-datastore")
include(":lib-store-mmkv")
include(":lib-imageloader-core")
include(":lib-imageloader-engine-glide")
include(":sample-store-core")
include(":sample-imageloader-core")
