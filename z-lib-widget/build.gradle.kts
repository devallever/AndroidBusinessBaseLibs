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
    api("io.github.youth5201314:banner:2.2.2")
    api("de.hdodenhof:circleimageview:2.2.0")
    api("com.afollestad.material-dialogs:core:3.3.0")
    api("com.afollestad.material-dialogs:bottomsheets:3.3.0")
    api("com.github.chrisbanes:photoview:2.3.0") {
        //exclude(module = "tiktok-business-android-sdk-comp")
        //androidx.core已经包含了下面这些
        exclude(group = "com.android.support", module = "support-annotations")
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-core-utils")
    }
}
