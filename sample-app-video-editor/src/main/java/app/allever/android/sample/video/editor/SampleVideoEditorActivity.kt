package app.allever.android.sample.video.editor

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import com.therouter.router.Route
import com.allever.video.editor.ui.AlbumActivity
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/videoeditor/main")
class SampleVideoEditorActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "视频编辑"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页", itemClick = {
        }),
        TextClickItem("主页", itemClick = {
            ActivityHelper.startActivity<AlbumActivity>()
        })
    )
}