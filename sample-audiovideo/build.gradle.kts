plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.audiovideo"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-media-core"))
    implementation(project(":lib-media-picker"))

    // 官方的so不支持https, 需要自己编译，或找其他编译好的so
//    # required, enough for most devices.
//    implementation 'tv.danmaku.ijk.media:ijkplayer-java:0.8.8'
//    implementation 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.8'

//    # Other ABIs: optional
//    implementation 'tv.danmaku.ijk.media:ijkplayer-armv5:0.8.8'
//    implementation 'tv.danmaku.ijk.media:ijkplayer-arm64:0.8.8'
//    implementation 'tv.danmaku.ijk.media:ijkplayer-x86:0.8.8'
//    implementation 'tv.danmaku.ijk.media:ijkplayer-x86_64:0.8.8'

    implementation("cn.jzvd:jiaozivideoplayer:7.7.0")

    // 必选，内部默认使用系统mediaplayer进行解码
    implementation("xyz.doikki.android.dkplayer:dkplayer-java:3.3.7")
    // 可选，包含StandardVideoController的实现
    implementation("xyz.doikki.android.dkplayer:dkplayer-ui:3.3.7")
    // 可选，使用exoplayer进行解码
    implementation("xyz.doikki.android.dkplayer:player-exo:3.3.7")
    // 可选，使用ijkplayer进行解码
    implementation("xyz.doikki.android.dkplayer:player-ijk:3.3.7")
    // 可选，如需要缓存或者抖音预加载功能请引入此库
    implementation("xyz.doikki.android.dkplayer:videocache:3.3.7")
}
