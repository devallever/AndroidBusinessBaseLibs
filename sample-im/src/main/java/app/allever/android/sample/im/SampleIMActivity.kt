package app.allever.android.sample.im

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.sample.im.business.LoginSampleFragment
import app.allever.android.sample.im.business.RegisterSampleFragment
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.websocket.SampleHttpMainFragment
import app.allever.android.sample.im.websocket.SampleWebSocketMainFragment
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import com.therouter.router.Route
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
        IMGlobal.init()
        IMWebSocketServer.registerServerListener(object : IMWebSocketServer.ServerListener {

            override fun onLog(log: String) {

            }

            override fun onStarted(url: String) {
                IMConfig.saveWebsocketUrl(url)
            }

            override fun onStopped() {
            }
        })
        LocalHttpServer.registerListener(object : LocalHttpServer.HttpServerListener {
            override fun onLog(log: String) {

            }

            override fun onStarted(url: String) {
                IMConfig.saveHttpBaseUrl(url)
            }

            override fun onStopped() {

            }

            override fun onError(msg: String) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        IMWebSocketServer.unregisterServerListener()
        LocalHttpServer.unregisterListener()
    }
}