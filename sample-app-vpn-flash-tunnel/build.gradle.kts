plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

val modelPkg = "app.flash.tunnel.vpn"

group = modelPkg

android {
    namespace = modelPkg

    compileOptions {
        isCoreLibraryDesugaringEnabled =  true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }

        //fix: /lib/x86_64/libsslocal.so" (in directory "/data/user_de/0/com.swimpp.proxysafeline.application/no_backup"): error=2, No such file or directory
        //https://github.com/shadowsocks/shadowsocks-android/issues/3066
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(project(":sample-common"))
    api(project(":lib-vpn-shadowsocks-core"))
    api(libs.androidx.datastore.preferences)

    //playService
    api(libs.play.services.ads)
    //lottie
    api(libs.airbnb.lottie)
    //glide
    api(libs.glide)
    //glide-transformations
    api(libs.glide.transformations)
    //material
    api(libs.material)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

}
