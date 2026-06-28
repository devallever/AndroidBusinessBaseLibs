package app.allever.android.sample.ipc

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.sample.ipc.binder.client.BinderProxy
import app.allever.android.sample.ipc.binder.service.BinderService
import com.chad.library.adapter.base.BaseQuickAdapter

class IPCBinderFragment : ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    private var mBinderProxy: BinderProxy? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?, service: IBinder?
        ) {
            mBinderProxy = service?.let { BinderProxy(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBinderProxy = null
        }

    }

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> =
        mutableListOf(TextDetailClickItem("加法") {
            val result = mBinderProxy?.add(1, 2)
            if (result != null) {
                toast("1 + 2 = $result")
            }
        }, TextDetailClickItem("乘法") {
            val result = mBinderProxy?.multiply(3, 2)
            if (result != null) {
                toast("3 * 2 = $result")
            }
        })

    override fun init() {
        super.init()
        requireActivity().bindService(
            Intent(requireContext(), BinderService::class.java),
            mConnection,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(mConnection)
    }
}