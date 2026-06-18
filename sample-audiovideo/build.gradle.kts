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
    // 可选，使用ijkplayer进行解码
    implementation(libs.dkplayer.ijk)

}
