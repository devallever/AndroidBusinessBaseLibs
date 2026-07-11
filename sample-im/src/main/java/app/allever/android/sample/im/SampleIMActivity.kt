package app.allever.android.sample.im

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.network.core.NetCore
import app.allever.android.lib.network.engine.okhttp.OkHttpConfig
import app.allever.android.lib.network.engine.okhttp.OkHttpEngine
import app.allever.android.sample.im.business.LoginSampleFragment
import app.allever.android.sample.im.business.RegisterSampleFragment
import app.allever.android.sample.im.connection.JavaWebSocketConnectionManager
import app.allever.android.sample.im.connection.OkHttpWebSocketConnectionManager
import app.allever.android.sample.im.database.AppDatabase
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.websocket.SampleHttpMainFragment
import app.allever.android.sample.im.websocket.SampleWebSocketMainFragment
import app.allever.android.sample.im.websocket.client.IMWebSocketClient
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/im/main")
class SampleIMActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "IM"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("WebSocket") {
            FragmentActivity.start<SampleWebSocketMainFragment>(it.title)
        },
        TextDetailClickItem("Http") {
            FragmentActivity.start<SampleHttpMainFragment>(it.title)
        },
        TextDetailClickItem("一键启动WebSocket和Http") {
            IMWebSocketServer.startServer(5400)
            LocalHttpServer.startServer()
        },
        TextDetailClickItem("一键停止WebSocket和Http") {
            IMWebSocketServer.stopServer()
            LocalHttpServer.stopServer()
        },
        //注册
        TextDetailClickItem("注册") {
            FragmentActivity.start<RegisterSampleFragment>(it.title)
        },
        //登录
        TextDetailClickItem("登录") {
            FragmentActivity.start<LoginSampleFragment>(it.title)
        }
    )

    override fun init() {
        super.init()
        AppDatabase.init(this.applicationContext)
        if (IMConfig.isLogin()) {
            IMWebSocketClient.connect(IMConfig.getConnectWebsocketUrl(IMConfig.getLoginUser()))
        }
        IMGlobal.initNetwork()
    }
}