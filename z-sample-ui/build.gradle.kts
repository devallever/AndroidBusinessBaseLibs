plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
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
    api(project(":z-lib-widget"))
    api(libs.material)

    implementation(libs.dimezis.blur.view)
    implementation(libs.centerzx.shape.blur.view)
    implementation(libs.yhaolpz.float.window)
    implementation(libs.pokercc.expandable.recycler.view)
    implementation(libs.ernestoyaquello.drag.drop.swipe.recycler.view)
}
