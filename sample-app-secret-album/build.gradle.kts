plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
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
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    //third
    implementation(libs.chrisbanes.photoview)
    //glide
    implementation(libs.glide)
//    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")
    implementation("com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0")
    implementation(libs.eventbus)

}
