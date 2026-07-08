package app.allever.android.sample.im

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.sample.im.connection.JavaWebSocketConnectionManager
import app.allever.android.sample.im.connection.OkHttpWebSocketConnectionManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/im/main")
class SampleIMActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "IM"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("OkHttpWebSocket") {
            OkHttpWebSocketConnectionManager.connect("ws://192.168.1.102:8080/ws")

        },
        TextDetailClickItem("Java-WebSocket") {
            JavaWebSocketConnectionManager.connect("ws://192.168.1.102:8080/ws")
        },
    )
}