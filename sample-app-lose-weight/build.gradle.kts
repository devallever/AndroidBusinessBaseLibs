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

//    val aarList = mutableListOf(
//        "toolkit-1.1.aar"
//    )
//    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))

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
    implementation ("org.litepal.guolindev:core:3.2.3")


    // 如果想使用SwipeBack 滑动边缘退出Fragment/Activity功能，完整的添加规则如下：
    implementation ("me.yokeyword:fragmentationx:1.0.2")
// swipeback基于fragmentation, 如果是自定制SupportActivity/Fragment，则参照SwipeBackActivity/Fragment实现即可
    implementation ("me.yokeyword:fragmentationx-swipeback:1.0.2")

// Activity作用域的EventBus，更安全，可有效避免after onSavenInstanceState()异常
    implementation ("me.yokeyword:eventbus-activity-scope:1.1.0")
// Your EventBus")s version

//    implementation ("com.jakewharton:butterknife:10.2.3")
//    annotationProcessor ("com.jakewharton:butterknife-compiler:10.2.3")

    implementation ("com.dinuscxj:circleprogressbar:1.1.1")
    //图表控件
    implementation ("com.github.PhilJay:MPAndroidChart:v3.0.3")
    //日历控件
    implementation ("com.haibin:calendarview:3.2.6")


}
