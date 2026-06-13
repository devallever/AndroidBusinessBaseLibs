plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.media.picker"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))
    api(project(":lib-media-core"))
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.glide)
    implementation(libs.chrisbanes.photoview)
    implementation(libs.androidx.activity.ktx)
}
