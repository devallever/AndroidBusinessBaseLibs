package app.allever.android.sample.app.text.card

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.view.MainActivity
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/apptextcard/main")
class SampleTextCardActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "TextCard"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("主页") {
            ActivityHelper.startActivity<MainActivity>()
        }
    )

    override fun init() {
        super.init()
        App.onCreate()
    }
}