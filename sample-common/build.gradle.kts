plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.common"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":lib-mvvm"))

    // BaseRecyclerViewAdapterHelper
    api(libs.baserecyclerviewadapterhelper)

    // FlycoTabLayout
    api(libs.flycotablayout)

    // refresh layout
    api(libs.refresh.layout.kernel)
    api(libs.refresh.header.classics)
}
