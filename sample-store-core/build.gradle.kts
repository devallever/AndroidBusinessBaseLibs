plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "app.allever.android.sample.store.core"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":lib-store-core"))
    implementation(project(":lib-store-datastore"))
    implementation(project(":lib-store-mmkv"))

}
