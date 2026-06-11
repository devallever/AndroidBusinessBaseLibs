package app.allever.android.sample.unity

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.plinkopro.wincash.ui.activity.MainActivity

@Route(path = "/unity/main")
class UnitySampleMainActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "Unity"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("继承UnityPlayerActivity") {
            ActivityHelper.startActivity(MainActivity::class.java)
        },
        TextClickItem("创建UnityPlayer") {
            ActivityHelper.startActivity(UnityContainerActivity::class.java)
        },
    )
}