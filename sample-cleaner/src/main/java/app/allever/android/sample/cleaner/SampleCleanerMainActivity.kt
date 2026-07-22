package app.allever.android.sample.cleaner

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * 清理工具入口列表页
 *
 * 展示所有可用的清理/管理功能入口。
 */
@Route(path = "/cleaner/main")
class SampleCleanerMainActivity :
    ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "清理工具"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("🧹 存储清理") {
            // 跳转到存储清理主界面（Tab 模式，默认显示存储清理 Tab）
            ActivityHelper.startActivity<CleanerActivity>()
        },
        TextClickItem("⚡ 内存清理") {
            ActivityHelper.startActivity<CleanerActivity>()
        },
        TextClickItem("📁 文件管理") {
            ActivityHelper.startActivity<CleanerActivity>()
        },
        TextClickItem("📊 性能监控") {
            ActivityHelper.startActivity<CleanerActivity>()
        }
    )
}
