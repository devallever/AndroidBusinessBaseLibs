plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.dj.csj"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))

    //player
    implementation("com.shuyu:gsyvideoplayer-java:10.1.0")
    implementation("com.shuyu:gsyvideoplayer-exo2:10.1.0")
//为保证兼容性 建议使用广告SDK版本
//    implementation 'com.pangle.cn:ads-sdk-pro:6.7.1.6'
    implementation("com.pangle.cn:ads-sdk-pro:6.8.2.0"){
        exclude(module = "tiktok-business-android-sdk-comp")
        // pag-apm 与 volcengine:apm_insight_crash 包含重复的 com.apm.insight.* 类
        exclude(group = "com.volcengine", module = "apm_insight_crash")
    }
    implementation("com.pangle.cn:pangrowth-base:2.8.0.1")
    {
        exclude(module = "tiktok-business-android-sdk-comp")
        // pag-apm 与 volcengine:apm_insight_crash 包含重复的 com.apm.insight.* 类
        exclude(group = "com.volcengine", module = "apm_insight_crash")
    }
    implementation("com.pangle.cn:pangrowth-djx-sdk-lite:2.8.0.1")
    {
        exclude(module = "tiktok-business-android-sdk-comp")
        // pag-apm 与 volcengine:apm_insight_crash 包含重复的 com.apm.insight.* 类
        exclude(group = "com.volcengine", module = "apm_insight_crash")
    }
    implementation("com.pangle.cn:pangrowth-nov-sdk:2.9.0.4"){
        exclude(module = "tiktok-business-android-sdk-comp")
        // pag-apm 与 volcengine:apm_insight_crash 包含重复的 com.apm.insight.* 类
        exclude(group = "com.volcengine", module = "apm_insight_crash")
    }
}
