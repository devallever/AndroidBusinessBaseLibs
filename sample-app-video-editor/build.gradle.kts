plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "com.allever.video.editor"

group = modelPkg

android {
    namespace = modelPkg

    //打包包含libs目录的so
    sourceSets.getByName("main") {
        jniLibs.setSrcDirs(jniLibs.srcDirs + files("$projectDir/libs"))
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-media-core"))

    val aarList = mutableListOf(
        "analytics-flurry-v1-1.0.aar",
        "permissions-compat-lib-1.1.aar",
        "toolkit-v1-1.1.aar"
    )
    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))


    //android
    implementation(libs.androidx.gridlayout)
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.legacy.support.v4)

    //third
    implementation(libs.android.gif.drawable)
    implementation(libs.greendao.generator)
    implementation(libs.eventbus)
    implementation(libs.greendao)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3.integration)
    implementation(libs.systembartint)
    implementation(libs.magic.viewpager)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.okdownload)
    implementation(libs.okdownload.sqlite)
    implementation(libs.okdownload.okhttp)
    implementation(libs.xiaopan.sketch)
    implementation(libs.airbnb.lottie)

}
