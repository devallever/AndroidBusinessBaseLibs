plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // ✅ 替换 kapt 为 ksp
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

val modelPkg = "app.allever.android.sample.im"
group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-network-core"))
    implementation(project(":lib-network-engine-okhttp"))
    implementation(libs.java.websocket)
    implementation(libs.nanohttpd)

    // room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // ✅ Room 从 kapt 切换到 ksp
    ksp(libs.androidx.room.compiler)

    implementation(libs.ktx.serialization)

    // media-core
    implementation(project(":lib-media-core"))
    implementation(project(":lib-media-picker"))
    // glide
    implementation(libs.glide)
    // ✅ Glide 如果使用注解处理器，也需要切换到 KSP
    // ksp(libs.glide.compiler) // 如果有这个依赖的话
}

//// ✅ 可选：配置 KSP 特定选项
//ksp {
//    // Room 特定配置
//    arg("room.schemaLocation", "${layout.projectDirectory}/schemas")
//    arg("room.generateKotlin", "true")
//    arg("room.incremental", "true")
//
//    // TheRouter 配置（如果需要在 library 中使用路由）
//    // arg("therouter.debug", "true")
//}