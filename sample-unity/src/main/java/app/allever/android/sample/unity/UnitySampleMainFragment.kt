package app.allever.android.sample.unity

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.plinkopro.wincash.ui.activity.MainActivity

class UnitySampleMainFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
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