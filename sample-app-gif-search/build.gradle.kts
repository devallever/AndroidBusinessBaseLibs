plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "com.allever.app.gif.memes"

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
    implementation(project(":lib-ffmpeg-command"))
    implementation(project(":lib-media-core"))
    implementation(project(":z-lib-widget"))

    //android
    implementation(libs.material)
    implementation(libs.androidx.cardview)

    //third
    //glide
    implementation(libs.glide)
    implementation(libs.eventbus)


    implementation(libs.android.gif.drawable)

    //1.0.7 会报错
    implementation(libs.okdownload)
    implementation(libs.okdownload.sqlite)
    implementation(libs.okdownload.okhttp)

    //litepalcore
    implementation(libs.litepal.core)

    // okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)

}
