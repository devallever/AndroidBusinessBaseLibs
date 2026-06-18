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


    // Media3 (官方媒体播放框架)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    implementation(libs.jiaozivideoplayer)

    // 必选，内部默认使用系统mediaplayer进行解码
    implementation(libs.dkplayer.java)
    // 可选，包含StandardVideoController的实现
    implementation(libs.dkplayer.ui)
    // 可选，使用exoplayer进行解码
//    implementation(libs.dkplayer.exo)
    // 可选，使用ijkplayer进行解码
    implementation(libs.dkplayer.ijk)
    // 可选，如需要缓存或者抖音预加载功能请引入此库
//    implementation(libs.dkplayer.videocache)
}
