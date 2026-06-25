plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.player.core.engine.media3"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    api(project(":lib-player-core"))
    // Media3 (官方媒体播放框架)
    api(libs.media3.exoplayer)
    api(libs.media3.ui)
    api(libs.media3.common)
}
