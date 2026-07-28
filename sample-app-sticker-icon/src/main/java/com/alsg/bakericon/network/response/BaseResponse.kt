//package com.alsg.bakericon.network.response
//
//import androidx.annotation.Keep
//import com.allever.lib.base.function.network.internal.response.NetResponse
//
//@Keep
//open class BaseResponse<DATA> : NetResponse<DATA>() {
//    var code: Int = 0
//    var message: String = ""
//    var count:Int = 0
//
//    override fun getResponseCode() = code
//    override fun getMsg() = message
//
//    override fun setData(code: Int, msg: String, data: DATA?) {
//        this.code = code
//        this.message = msg
//        this.data = data
//    }
//}