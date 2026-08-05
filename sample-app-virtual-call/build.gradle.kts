plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
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
    implementation(project(":z-lib-widget"))

    //android
    implementation(libs.androidx.cardview)

    //glide
    implementation(libs.glide)
    implementation(libs.circleimageview)
}
