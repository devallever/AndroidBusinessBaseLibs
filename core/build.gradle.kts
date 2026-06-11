plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.core"
group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    // android & kotlin & google
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.appcompat)
//    api(libs.material)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.recyclerview)
    api(libs.androidx.viewpager2)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.extensions)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.exifinterface)
    api(libs.androidx.paging.runtime.ktx)

    // coroutines
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // gson
    api(libs.gson)

    api(libs.basepopup)
}
