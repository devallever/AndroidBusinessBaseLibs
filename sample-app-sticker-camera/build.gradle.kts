plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
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

    //android
    implementation(libs.material)
    implementation(libs.androidx.cardview)

    //glide
    implementation(libs.glide)
    implementation(libs.eventbus)

    implementation(libs.isseiaoki.simplecropview)
    implementation(libs.flying.xiaopo.sticker)
}
