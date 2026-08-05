plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "z.app.allever.android.lib.widget"
group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":core"))

    // BaseRecyclerViewAdapterHelper
    api(libs.baserecyclerviewadapterhelper)
    //material
    api(libs.material)

    // FlycoTabLayout
    api(libs.flycotablayout)

    // refresh layout
    api(libs.refresh.layout.kernel)
    api(libs.refresh.header.classics)
//    implementation  'io.github.scwang90:refresh-header-radar:2.0.5'       //雷达刷新头
//    implementation  'io.github.scwang90:refresh-header-falsify:2.0.5'     //虚拟刷新头
//    implementation  'io.github.scwang90:refresh-header-material:2.0.5'    //谷歌刷新头
//    implementation  'io.github.scwang90:refresh-header-two-level:2.0.5'   //二级刷新头
//    implementation  'io.github.scwang90:refresh-footer-ball:2.0.5'        //球脉冲加载
//    implementation  'io.github.scwang90:refresh-footer-classics:2.0.5'    //经典加载
    api(libs.youth.banner)
    api(libs.circleimageview)
    api(libs.afollestad.material.dialogs)
    api(libs.afollestad.material.dialogs.bottomsheets)
    api(libs.chrisbanes.photoview) {
        //exclude(module = "tiktok-business-android-sdk-comp")
        //androidx.core已经包含了下面这些
        exclude(group = "com.android.support", module = "support-annotations")
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-core-utils")
    }
}
