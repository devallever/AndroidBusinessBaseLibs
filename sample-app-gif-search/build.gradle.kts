plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
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

    val aarList = mutableListOf(
        "toolkit-1.1.aar"
    )
    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

    //android
    implementation(libs.material)
    implementation(libs.androidx.cardview)

    //third
    //glide
    implementation(libs.glide)
    implementation(libs.eventbus)


    //local
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.19")

    //1.0.7 会报错
    implementation("com.liulishuo.okdownload:okdownload:1.0.4")
    implementation("com.liulishuo.okdownload:sqlite:1.0.4")
    implementation("com.liulishuo.okdownload:okhttp:1.0.4")

    implementation ("org.litepal.android:kotlin:3.0.0")
//    implementation project(path: ':myselector')

}
