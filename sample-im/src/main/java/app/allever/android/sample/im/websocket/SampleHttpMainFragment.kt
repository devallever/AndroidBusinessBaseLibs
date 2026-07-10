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
            baseUrl("http://192.168.43.106:8080")
            // 设置统一业务响应类型
            responseClass(BaseResponse::class.java)
        }

        toast("初始化完成！引擎: ${NetCore.currentEngine()}")
        log("Network 已初始化，引擎: ${NetCore.currentEngine()}")
    }
}