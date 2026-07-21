plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "org.xm.secret.photo.album"

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

    //third
    implementation(libs.chrisbanes.photoview)
    //glide
    implementation(libs.glide)
    implementation(libs.subsampling.scale.image.view.androidx)
    implementation(libs.eventbus)

}
