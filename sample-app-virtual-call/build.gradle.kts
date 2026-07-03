plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "com.allever.app.virtual.call"

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
    implementation(libs.androidx.cardview)

    //third
    //glide
    implementation(libs.glide)
    implementation(libs.circleimageview)
    //material
    implementation(libs.material)
}
