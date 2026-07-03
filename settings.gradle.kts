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

        //火山引擎maven仓库地址
        maven(url = "https://artifact.bytedance.com/repository/Volcengine/")
        //穿山甲maven仓库地址
        maven(url = "https://artifact.bytedance.com/repository/pangle")

        maven(url = "https://s01.oss.sonatype.org/content/groups/public")

        maven(url = "https://maven.pkg.github.com/CarGuo/GSYVideoPlayer")
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


        //火山引擎maven仓库地址
        maven(url = "https://artifact.bytedance.com/repository/Volcengine/")
        //穿山甲maven仓库地址
        maven(url = "https://artifact.bytedance.com/repository/pangle")

        maven(url = "https://s01.oss.sonatype.org/content/groups/public")

        maven(url = "https://maven.pkg.github.com/CarGuo/GSYVideoPlayer")
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
include(":lib-network-engine-okhttp")
include(":lib-store-engine-datastore")
include(":lib-store-engine-mmkv")
include(":lib-imageloader-core")
include(":lib-imageloader-engine-glide")
include(":lib-imageloader-engine-coil")
include(":lib-permission-engine-permissionx")
include(":lib-camera-proxy-camerax")
include(":lib-camera-proxy-camera2")
include(":lib-vpn-shadowsocks-core")
include(":lib-player-core")
include(":lib-player-engine-media3")
include(":lib-player-engine-ijk")

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
include(":sample-unity")
include(":sample-vpn")
include(":sample-cleaner")
include(":sample-dj-csj")

include(":sample-app-charge-reward")
include(":sample-app-vpn-flash-tunnel")
include(":sample-app-cleaner-wood")
include(":sample-ipc")
include(":sample-app-net-speed-test")
include(":sample-app-step-tool")
include(":sample-app-video-editor")
include(":sample-app-secret-album")
include(":sample-app-spy-camera")

include(":z-lib-widget")
include(":z-sample-billing")
include(":z-sample-cleaner")
include(":z-sample-designpattern")
include(":z-sample-function")
include(":z-sample-jetpack")
include(":z-sample-jni")
include(":z-sample-jni-mk")
include(":z-sample-kotlin")
include(":z-sample-learning-android")
include(":z-sample-login")
include(":z-sample-material-design")
include(":z-sample-safe")
include(":z-sample-thirtypart")
include(":z-sample-ui")
include(":z-sample-toolbox")
include(":z-sample-videoeditor")
include(":z-sample-microsoft-speech")
include(":z-sample-audiovideo")
include(":sample-app-sticker-camera")
include(":lib-ffmpeg-command")
include(":sample-app-gif-search")
include(":sample-app-virtual-call")
