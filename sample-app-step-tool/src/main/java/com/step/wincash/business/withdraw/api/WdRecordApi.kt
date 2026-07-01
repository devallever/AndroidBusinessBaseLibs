//package com.step.wincash.business.withdraw.api
//
//import androidx.appcompat.app.AppCompatActivity
//import com.step.wincash.business.withdraw.bean.WdRecordsResult
//
//import com.step.wincash.init.Constance
//
//class WdRecordApi(
//    val orderIds: String = "", //提现id集合多个以逗号分隔
//    val page: Int,
//    val limit: Int,
//) : IRequestApi {
//    override fun getApi(): String = Constance.RECORD_API
//
//    fun request(
//        activity: AppCompatActivity,
//        onSuccess: (res: WdRecordsResult) -> Unit = {},
//        onFailure: (code: Int) -> Unit = {},
//        onError: (message: String) -> Unit = {},
//    ) {
//        val api = WdRecordApi(
//            orderIds = orderIds,
//            page = page,
//            limit = limit
//        )
//        EasyHttp
//            //post请求
//            .post(activity)
//            .api(api)
//            .request(object : OnHttpListener<WdRecordsResult> {
//                //这里的返回内容是data里面的, 不需要写code和msg
//                override fun onHttpSuccess(result: WdRecordsResult) {
//                    onSuccess.invoke(result)
//                }
//
//                override fun onHttpFail(throwable: Throwable) {
//                    if (throwable is CustomException){
//                        //接口返回的code
//                        val errorCode = throwable.errorCode
//                        onFailure.invoke(errorCode)
//                    }else{
//                        //其他报错,网络异常,服务器连接不上等
//                        onError(throwable.message?:"未知错误")
//                    }
//                }
//            })
//    }
//}