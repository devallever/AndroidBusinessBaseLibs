plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "org.xm.sticker.camera"

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

//    val aarList = mutableListOf(
//        "toolkit-v1-1.1.aar"
//    )
//    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

    //android
    implementation(libs.material)
    implementation(libs.androidx.cardview)

    //glide
    implementation(libs.glide)
    implementation(libs.eventbus)
//    implementation(libs.retrofit)
//    implementation(libs.retrofit.converter.gson)

    implementation("com.isseiaoki:simplecropview:1.1.8")
    implementation("com.flying.xiaopo:sticker:1.6.0")
//    implementation("io.reactivex:rxandroid:1.2.1")
//    implementation("io.reactivex.rxjava2:rxjava:2.1.8")
//    implementation("com.squareup.retrofit2:adapter-rxjava:2.3.0")
//    implementation("com.liulishuo.filedownloader:library:1.7.1")
}
