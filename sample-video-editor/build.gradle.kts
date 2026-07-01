plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
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

    val aarList = mutableListOf(
        "analytics-flurry-v1-1.0.aar",
//        "business-sdk-v1-2.1-noadw.aar",
//        "inappbilling-compat-lib-1.0.11.aar",
        "permissions-compat-lib-1.1.aar",
//        "processdaemon-photoeditor.photoeditor.photoeditor.pro-1.1.aar",
        "toolkit-v1-1.1.aar"
    )
    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))


    //android
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    //third
    implementation("pub.devrel:easypermissions:1.2.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.12")
    implementation("org.greenrobot:greendao-generator:3.2.0")
//    implementation("com.jakewharton:butterknife:10.1.0")
    annotationProcessor ("com.jakewharton:butterknife-compiler:10.1.0")
    implementation("org.greenrobot:eventbus:3.0.0")
    implementation("org.greenrobot:greendao:3.2.0")
//    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation(libs.glide)
    implementation("com.github.bumptech.glide:okhttp3-integration:4.3.1@aar")
//    annotationProcessor ")com.github.bumptech.glide:compiler:4.3.1")
//    implementation("com.github.bumptech.glide:compiler:4.3.1")
    implementation("com.readystatesoftware.systembartint:systembartint:1.0.3")
    implementation("com.zhy:magic-viewpager:1.0.1")
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation("com.liulishuo.okdownload:okdownload:1.0.4")
    implementation("com.liulishuo.okdownload:sqlite:1.0.4")
    implementation("com.liulishuo.okdownload:okhttp:1.0.4")
    implementation("me.xiaopan:sketch:2.5.0")
    implementation(libs.airbnb.lottie)
}
