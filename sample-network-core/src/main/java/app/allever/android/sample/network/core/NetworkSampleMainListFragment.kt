package app.allever.android.sample.network.core

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class NetworkSampleMainListFragment: ListFragment<FragmentListBinding, ListViewModel,  TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("HttpUrlConnection"),
        TextClickItem("OkHttp"),
        TextClickItem("Retrofit"),
        TextClickItem("NetworkCoreEngine") {
            FragmentActivity.start<EngineTabFragment>(it.title)
        }
    )
}