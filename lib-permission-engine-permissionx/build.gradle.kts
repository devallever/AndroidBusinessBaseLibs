plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.permission.engine.permissionx"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    // PermissionX 依赖（使用时需确保仓库中包含）
    implementation("com.guolindev.permissionx:permissionx:1.6.1")
}
