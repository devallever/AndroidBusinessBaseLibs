package app.allever.android.sample.app.lose.weight

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.therouter.router.Route
import com.allever.lose.weight.MainActivity
import com.allever.lose.weight.MyApplication
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/apploseweight/main")
class SampleAppLoseWeightActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "30天减肥"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("主页",) {
            ActivityHelper.startActivity<MainActivity>()
        }
    )

    override fun init() {
        super.init()
        MyApplication.getInstance().onCreate()
    }
}