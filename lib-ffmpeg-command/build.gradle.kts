plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "com.coder.ffmpeg"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
}
