plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val modelPkg = "app.allever.android.lib.store.datastore"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    api(project(":lib-store-core"))
    api(libs.androidx.datastore.preferences)
}
