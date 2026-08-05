plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.allever.android.sample.company.heifeng.light.control"

group = modelPkg

android {
    namespace = modelPkg
}

dependencies {
    implementation(project(":sample-common"))
    val aarList = mutableListOf(
        "meshprovisioner-2.2.3.2.aar"
    )
    implementation(fileTree(mapOf("dir" to "libs", "include" to aarList)))
    //material
    implementation(libs.material)
}
