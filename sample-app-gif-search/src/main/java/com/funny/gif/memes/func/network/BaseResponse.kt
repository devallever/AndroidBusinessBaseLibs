package com.funny.gif.memes.func.network

open class BaseResponse<T>() {
    var `data`: T? = null
    var errorCode: Int = 0
    var errorMsg: String = ""
}