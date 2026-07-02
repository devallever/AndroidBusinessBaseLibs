package app.allever.android.sample.app.sticker.camera

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.allever.sticker.ui.MainActivity
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/appstickercamera/main")
class SampleAppStickerCamera : ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "贴纸相机"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> {
        return TextClickAdapter()
    }

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页") {
            ActivityHelper.startActivity<MainActivity>()
        }
    )
}