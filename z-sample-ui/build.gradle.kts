plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.ui"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    api(project(":lib-imageloader-engine-glide"))
    api(project(":lib-media-picker"))
    api(project(":lib-store-core"))
    api(project(":z-lib-widget"))

    implementation("com.github.Dimezis:BlurView:version-2.0.6")
    implementation("com.github.centerzx:ShapeBlurView:1.0.5")
    implementation("com.github.yhaolpz:FloatWindow:1.0.9")
    implementation("com.github.pokercc:ExpandableRecyclerView:0.9.3")
    implementation("com.ernestoyaquello.dragdropswiperecyclerview:drag-drop-swipe-recyclerview:1.1.1")
}
