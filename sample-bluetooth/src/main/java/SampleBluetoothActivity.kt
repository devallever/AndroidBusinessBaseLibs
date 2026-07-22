package app.allever.android.sample.bluetooth
import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/bluetooth/main")
class SampleBluetoothActivity: ListActivity<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "蓝牙"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("蓝牙聊天程序") {
        }
    )
}