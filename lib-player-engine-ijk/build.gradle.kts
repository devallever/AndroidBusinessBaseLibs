plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.player.core.engine.ijk"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    api(project(":lib-player-core"))
    api(libs.dkplayer.ijk)
}
