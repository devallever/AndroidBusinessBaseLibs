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
    }
}

rootProject.name = "AndroidBusinessBaseLibs"
include(":app")
include(":core")
include(":lib-mvvm")
include(":sample-common")
include(":sample-appsflyer")
include(":sample-adjust")
