package app.allever.android.sample.app.virtual.call

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import org.xm.app.virtual.call.ui.HomeActivity
import org.xm.app.virtual.call.ui.SplashActivity

@Route(path = "/appvirtualcall/main")
class SampleAppVirtualCallActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "虚拟通话"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页"){
            ActivityHelper.startActivity<SplashActivity>()
        },
        TextClickItem("主界面"){
            ActivityHelper.startActivity<HomeActivity>()
        },
    )
}