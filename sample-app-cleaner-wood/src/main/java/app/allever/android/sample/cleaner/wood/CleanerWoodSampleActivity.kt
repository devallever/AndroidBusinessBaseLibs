package app.allever.android.sample.cleaner.wood

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.therouter.router.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.clean.wood.MainActivity
import com.clean.wood.SplashActivity
import com.clean.wood.WoodApp

@Route(path = "/cleanerwood/main")
class CleanerWoodSampleActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "清理wood"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem>  = mutableListOf(
        TextDetailClickItem("启动页") {
            ActivityHelper.startActivity<SplashActivity>()
        },
        TextDetailClickItem("主页") {
            ActivityHelper.startActivity<MainActivity>()
        },
    )

    override fun init() {
        super.init()
        WoodApp.init()
    }
}