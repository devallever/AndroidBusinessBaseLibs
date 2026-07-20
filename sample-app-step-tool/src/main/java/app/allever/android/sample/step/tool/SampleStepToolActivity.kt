package app.allever.android.sample.step.tool

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.therouter.router.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.step.wincash.ui.activity.STLaunchActivity
import com.step.wincash.ui.activity.STMainActivity

@Route(path = "/steptool/main")
class SampleStepToolActivity : ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "步数工具"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页", itemClick = {
            ActivityHelper.startActivity<STLaunchActivity>()
        }),
        TextClickItem("主页", itemClick = {
            ActivityHelper.startActivity<STMainActivity>()
        }),
    )
}