package com.allever.video.editor.function.online

import androidx.annotation.Keep

@Keep
class OnlineEffectBean : OnlineDataBean() {
    var smallImgUrlList: MutableList<String>? = mutableListOf()
    var midImgUrlList: MutableList<String>? = mutableListOf()
    var bigImgUrlList: MutableList<String>? = mutableListOf()

    //内置特效字段
    var isBuildin = true
    var resIconName = 0
    var assetName = ""

    fun getDefaultSmallImgUrl(): String? {
        return if (smallImgUrlList?.size!! > 0) {
            smallImgUrlList?.get(0)
        } else {
            ""
        }
    }

    fun getDefaultMidImgUrl(): String? {
        return if (midImgUrlList?.size!! > 0) {
            midImgUrlList?.get(0)
        } else {
            ""
        }
    }

    fun getDefaultBigImgUrl(): String? {
        return if (bigImgUrlList?.size!! > 0) {
            bigImgUrlList?.get(0)
        } else {
            ""
        }
    }

}