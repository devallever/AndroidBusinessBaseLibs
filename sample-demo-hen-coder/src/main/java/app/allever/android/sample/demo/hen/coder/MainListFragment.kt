package app.allever.android.sample.demo.hen.coder

import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.sample.demo.hen.coder.draw.CustomDrawActivity
import app.allever.android.sample.demo.hen.coder.draw.PorterDuffXfermodeDemoActivity
import com.chad.library.adapter.base.BaseQuickAdapter

class MainListFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER
    )

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("绘制DEMO") {
            ActivityHelper.startActivity(CustomDrawActivity::class.java)
        },
        TextDetailClickItem("裁剪DEMO") {
            ActivityHelper.startActivity(PorterDuffXfermodeDemoActivity::class.java)
        },
        TextDetailClickItem("搜索框") {
            ActivityHelper.startActivity(SearchViewActivity::class.java)
        },
        TextDetailClickItem("圆形头像") {
            ActivityHelper.startActivity(CircleImageViewActivity::class.java)
        },
        TextDetailClickItem("自动标签") {
            ActivityHelper.startActivity(AutoTagActivity::class.java)
        },
    )
}