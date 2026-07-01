package app.allever.android.sample.charge.reward

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.charge.ChargeApp
import com.example.charge.ui.activity.LaunchActivity
import com.example.charge.ui.activity.ChargeMainActivity

@Route(path = "/chargereward/main")
class SampleChargeActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "SampleCharge"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("网赚充电-启动页") {
            ActivityHelper.startActivity(LaunchActivity::class.java)
        },
        TextClickItem("网赚充电-主页") {
            ActivityHelper.startActivity(ChargeMainActivity::class.java)
        },
    )

    override fun init() {
        super.init()
        ChargeApp.init()
    }
}