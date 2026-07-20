package app.allever.android.sample.net.speed.test

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.android.allever.gp.quick.project.NSTLoadingActivity
import app.android.allever.gp.quick.project.MyApp
import app.android.allever.gp.quick.project.ui.NSTHomeActivity
import com.therouter.router.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/netspeedtest/main")
class SampleNetSpeedTestActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "网速测试"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("启动页") {
            ActivityHelper.startActivity<NSTLoadingActivity>()
        },
        TextDetailClickItem("主页") {
            ActivityHelper.startActivity<NSTHomeActivity> { }
        },
    )

    override fun init() {
        super.init()
        MyApp.init()
    }
}