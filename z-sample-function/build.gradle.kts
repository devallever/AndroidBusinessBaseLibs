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
    api (libs.skin.support)                   // skin-support
    api(libs.skin.support.appcompat)         // skin-support 基础控件支持
    api(libs.skin.support.design)
    // skin-support-design material design 控件支持[可选]
    api(libs.skin.support.cardview)
    // skin-support-cardview CardView 控件支持[可选]
    api(libs.skin.support.constraint.layout)
    // skin-support-constraint-layout ConstraintLayout 控件支持[可选]


    // https://github.com/vanniktech/Emoji
    api (libs.vanniktech.emoji.ios)

    // https://github.com/vdurmont/emoji-java
    api (libs.vanniktech.emoji.java)

    //Room
    api (libs.androidx.room.runtime)
    api (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)

   //Emoji
   implementation (libs.androidx.emoji2.emojipicker)
}
