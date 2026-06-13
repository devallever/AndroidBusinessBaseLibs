plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.jetpack"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
//    api(project(":lib-imageloader-engine-glide"))
//    api(project(":lib-media-picker"))
    api(project(":lib-store-core"))
    api(project(":lib-network-core"))
    api(project(":z-lib-widget"))

    //Room
    api (libs.androidx.room.runtime)
    api (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)

    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation ("androidx.navigation:navigation-dynamic-features-fragment:2.5.3")
    api(libs.androidx.datastore)
    api(libs.androidx.datastore.preferences)
}
