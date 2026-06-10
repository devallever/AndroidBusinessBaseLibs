plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val modelPkg = "z.app.allever.android.sample.function"

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
    api ("skin.support:skin-support:4.0.5")                   // skin-support
    api("skin.support:skin-support-appcompat:4.0.5")         // skin-support 基础控件支持
    api("skin.support:skin-support-design:4.0.5")
    // skin-support-design material design 控件支持[可选]
    api("skin.support:skin-support-cardview:4.0.5")
    // skin-support-cardview CardView 控件支持[可选]
    api("skin.support:skin-support-constraint-layout:4.0.5")
    // skin-support-constraint-layout ConstraintLayout 控件支持[可选]


    // https://github.com/vanniktech/Emoji
    api ("com.vanniktech:emoji-ios:0.8.0")

    // https://github.com/vdurmont/emoji-java
    api ("com.vdurmont:emoji-java:5.1.1")

    //Room
    api ("androidx.room:room-runtime:2.4.3")
    api ("androidx.room:room-ktx:2.4.3")
    kapt ("androidx.room:room-compiler:2.4.3")

   //Emoji
   implementation ("androidx.emoji2:emoji2-emojipicker:1.4.0")
}
