//package com.videoeditor.function.online
//
//import com.allever.video.editor.BuildConfig
//import Base64
//
//object OnlineConstant {
//    /**
//     * http://www.allinai.global:23456/%s/%s
//     */
//    private val CONFIG_URL_FORMAT_ENC = "aHR0cDovL3d3dy5hbGxpbmFpLmdsb2JhbDoyMzQ1Ni8lcy8lcw=="
//
//    /**
//     * http://www.allinai.global:23456/%s/${urlSuffix}
//     */
//    fun getUrl(urlSuffix: String): String {
//
//        val configUrlFormat = String(Base64.decode(CONFIG_URL_FORMAT_ENC))
//        return String.format(configUrlFormat, BuildConfig.PRODUCT_CRC, urlSuffix)
//    }
//}