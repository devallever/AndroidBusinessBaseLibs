plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "com.allever.app.qr.code.scaner"

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
        "toolkit-v1-1.1.aar"
    )
    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

    //android
    implementation(libs.androidx.gridlayout)
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.legacy.support.v4)
    //glide
    implementation(libs.glide)
    //lottie
    implementation(libs.airbnb.lottie)

}
