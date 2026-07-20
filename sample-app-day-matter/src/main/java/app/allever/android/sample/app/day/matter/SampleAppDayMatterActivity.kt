package app.allever.android.sample.app.day.matter

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.therouter.router.Route
import com.allever.daymatter.MainActivity
import com.allever.daymatter.MyApp
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/appdaymatter/main")
class SampleAppDayMatterActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "DayMatter"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("主页") {
            ActivityHelper.startActivity<MainActivity>()
            }
        )

    override fun init() {
        super.init()
        MyApp.onCreate()
    }
}