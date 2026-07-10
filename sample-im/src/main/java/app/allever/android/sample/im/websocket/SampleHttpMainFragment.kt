package app.allever.android.sample.im.websocket

import android.util.Log
import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.engine.okhttp.OkHttpConfig
import app.allever.android.lib.network.engine.okhttp.OkHttpEngine
import app.allever.android.sample.im.response.BaseResponse
import com.chad.library.adapter.base.BaseQuickAdapter

class SampleHttpMainFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("服务端管理", ) {
            FragmentActivity.start<SampleHttpServerManageFragment>(it.title)
        },
        TextDetailClickItem("客户端管理", ) {
            FragmentActivity.start<SampleHttpClientManageFragment>(it.title)
        }
    )

    override fun init() {
        super.init()
        initNetwork()
    }

    private fun initNetwork() {
        NetCore.init {
            // 使用公开测试 API
            baseUrl("http://10.20.224.246:8080")
            // 设置统一业务响应类型
            responseClass(BaseResponse::class.java)

//            engine(OkHttpEngine.ENGINE_NAME) {
//                // OkHttp 专属配置
//                (this as? OkHttpConfig)?.apply {
////                    connectionPool(5, 5, java.util.concurrent.TimeUnit.MINUTES)
////                    retryOnConnectionFailure(true)
////                    addInterceptor("LoggingInterceptor")
////                    addNetworkInterceptor("LoggingInterceptor")
//                }
//            }
        }

        log("Network 已初始化，引擎: ${NetCore.currentEngine()}")
    }
}