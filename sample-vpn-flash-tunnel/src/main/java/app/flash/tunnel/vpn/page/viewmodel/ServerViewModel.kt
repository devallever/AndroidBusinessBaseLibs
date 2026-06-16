package app.flash.tunnel.vpn.page.viewmodel

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.data.NodeItem
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.lib.common.base.AbsViewModel
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.page.adapter.ServerAdapter
import com.github.shadowsocks.preference.DataStore

class ServerViewModel : AbsViewModel() {


    private var originItemIndex = 0
    private var selectedIndex = 0

    private val smartNode by lazy {
        NodeItem().apply {
            cn = TunnelApp.context.getString(R.string.the_fastest_server)
            nn = TunnelApp.context.getString(R.string.default_server)
        }
    }

    val adapter by lazy {
        ServerAdapter(mutableListOf<NodeItem>().apply {
            TunnelHelper.nodeListLiveData.value.let {
                clear()
                it?.let {
                    addAll(it)
                }
                add(0, smartNode)

                val adIndex = getAdIndex(size)
                if (adIndex != -1) {
                    add(adIndex, NodeItem().apply {
                        type = ServerAdapter.TYPE_AD
                    })
                }
            }
        })
    }


    private fun getAdIndex(listSize: Int): Int {
        return if (listSize >= 4) {
            4
        } else {
            listSize
        }
    }

    fun updateSelectedIndex() {
        originItemIndex = if (!TunnelHelper.isSmartMode()) {
            adapter.data.indexOf(TunnelHelper.getSelectedNodeItem())
        } else {
            0
        }

        selectedIndex = originItemIndex
    }

    fun checkNeedConnect(): Boolean {
        log("originItemIndex = $originItemIndex")
        log("selectedIndex = $selectedIndex")
        return originItemIndex != selectedIndex
    }

    private fun updateSelectProxy(item: NodeItem) {
        if (TunnelHelper.isSmartMode()) {
            DataStore.updateSelectProxy(TunnelHelper.getFastestNode()?.entity?.id ?: 0)
        } else {
            DataStore.updateSelectProxy(item.entity?.id ?: 0)
        }
    }

    fun handleClickItem(
        activity: ComponentActivity,
        adapter: ServerAdapter,
        item: NodeItem,
        position: Int
    ) {
        val type = item.type
        if (type == ServerAdapter.TYPE_AD) {
            return
        }

        if (position == selectedIndex) {
            return
        }

        selectedIndex = position
        TunnelHelper.updateMode(item.entity == null)
        if (TunnelHelper.isSmartMode() && checkNeedConnect()) {
            TunnelHelper.updateSmartModeItem()
        }
        updateSelectProxy(item)
        adapter.updateSelectPosition()
        val needConnect = checkNeedConnect()
        log("needConnect = $needConnect")

        if (needConnect) {
            activity.setResult(AppCompatActivity.RESULT_OK, Intent().apply {
                putExtra(Constants.EXTRA_NEED_CHANGE_CONNECT_NODE, true)
            })
            activity.finish()
        }
    }

}