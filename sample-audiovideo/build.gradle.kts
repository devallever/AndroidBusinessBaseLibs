plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
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
    implementation(project(":lib-player-core"))
    implementation(project(":lib-player-engine-media3"))
    implementation(project(":lib-player-engine-ijk"))
}
