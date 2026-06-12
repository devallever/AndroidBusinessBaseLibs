package app.allever.android.sample.cleaner

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabActivity
import app.allever.android.lib.common.databinding.ActivityTabBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.cleaner.ui.fragment.FileManageFragment
import app.allever.android.sample.cleaner.ui.fragment.MemoryCleanFragment
import app.allever.android.sample.cleaner.ui.fragment.MonitorFragment
import app.allever.android.sample.cleaner.ui.fragment.StorageCleanFragment

/**
 * 清理工具主界面 - TabActivity
 *
 * 承载 4 个功能模块：
 * 1. 存储清理（扫描垃圾文件 → 预览 → 选择 → 一键清理）
 * 2. 内存清理（内存监控 + 进程管理 + 一键加速）
 * 3. 文件管理（大文件 / 重复文件 / 文件分类）
 * 4. 性能监控（CPU / 电池 / 温度）
 */
class CleanerActivity : TabActivity<ActivityTabBinding, BaseViewModel>() {

    override fun getPageTitle(): String = "清理工具"

    override fun getTabTitles(): MutableList<String> = mutableListOf(
        "存储清理",
        "内存清理",
        "文件管理",
        "性能监控"
    )

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        StorageCleanFragment(),
        MemoryCleanFragment(),
        FileManageFragment(),
        MonitorFragment()
    )
}
