plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.player.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))

    // 网络视频缓存库（本地HTTP代理服务器）
//    implementation("com.danikula:videocache:2.7.1") //官方
    api(libs.dkplayer.videocache)//暂时使用这个 //冲突暂时
}
