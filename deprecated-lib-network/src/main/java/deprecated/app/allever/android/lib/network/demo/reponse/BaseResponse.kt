package deprecated.app.allever.android.lib.network.demo.reponse

import deprecated.app.allever.android.lib.network.response.NetResponse

open class BaseResponse<DATA> : NetResponse<DATA>() {
    var errorCode: Int = 0
    var errorMsg: String = ""

    override fun getCode() = errorCode
    override fun getMsg() = errorMsg

    override fun setData(code: Int, msg: String, data: DATA?) {
        this.errorCode = code
        this.errorMsg = msg
        this.data = data
    }
}