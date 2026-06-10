package z.app.allever.android.sample.kotlin

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import z.app.allever.android.sample.kotlin.function.flow.FlowMainFragment

class KotlinMainFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("Flow") {
            FragmentActivity.start<FlowMainFragment>(it.title)
        },
        TextClickItem("Coroutine"),
        TextClickItem("Higher Function")
    )
}