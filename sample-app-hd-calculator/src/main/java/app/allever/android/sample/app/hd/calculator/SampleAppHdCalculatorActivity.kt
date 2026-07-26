package app.allever.android.sample.app.hd.calculator

import android.view.Gravity
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hd.calculator.app.MyApp
import com.hd.calculator.app.ui.CalculatorActivity

@Route(path = "/apphdcalculator/main")
class SampleAppHdCalculatorActivity: ListActivity<ActivityListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getPageTitle(): String = "HdCalculator"

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("主页") {
            ActivityHelper.startActivity<CalculatorActivity>()
        }
    )

    override fun init() {
        super.init()
        MyApp.onCreate()
    }
}