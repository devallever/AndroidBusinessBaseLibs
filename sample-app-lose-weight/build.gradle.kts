plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
//    id("com.jakewharton.butterknife")
}

val modelPkg = "com.allever.lose.weight"

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

    //third
    //glide
    implementation(libs.glide)
    implementation(libs.eventbus)
    //gson
    implementation(libs.gson)
    //litepal
    implementation (libs.litepal.core)

// Activity作用域的EventBus，更安全，可有效避免after onSavenInstanceState()异常
    implementation (libs.eventbus.activity.scope)

    implementation (libs.circleprogressbar)
    //图表控件
    implementation (libs.mpandroidchart)
    //日历控件
    implementation (libs.calendarview)


}
