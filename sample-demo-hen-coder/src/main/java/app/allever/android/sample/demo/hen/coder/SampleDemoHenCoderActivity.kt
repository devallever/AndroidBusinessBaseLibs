package app.allever.android.sample.demo.hen.coder

import android.view.Gravity
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/demohencoder/main")
class SampleDemoHenCoderActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "hen-coder"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> =
        TextDetailClickAdapter(Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("主页", "hen-coder-auto-tag") {
            FragmentActivity.start<MainListFragment>("Hen Coder")
        }
    )
}