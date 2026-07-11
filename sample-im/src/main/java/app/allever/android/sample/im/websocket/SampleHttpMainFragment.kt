package app.allever.android.sample.im.websocket

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.http.response.BaseResponse
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
    }
}