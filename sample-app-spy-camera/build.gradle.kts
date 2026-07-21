plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "org.xm.stealth.camera"

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
    //lib-camera-proxy-camerax
    implementation(project(":lib-camera-proxy-camerax"))

//    val aarList = mutableListOf(
//        "toolkit-v1-1.1.aar"
//    )
//    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

    //android
    implementation(libs.androidx.cardview)

    //third
    implementation(libs.chrisbanes.photoview)
    //glide
    implementation(libs.glide)
}
