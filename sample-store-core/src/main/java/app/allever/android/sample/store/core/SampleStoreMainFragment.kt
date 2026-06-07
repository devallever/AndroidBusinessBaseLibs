package app.allever.android.sample.store.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * 存储组件示例主入口
 *
 * 包含三个演示模块：
 * 1. 引擎切换 — 演示 SP / DataStore / MMKV 之间的无缝切换
 * 2. 基础操作 — 演示 CRUD、批量操作、查询等完整 API
 * 3. 多实例 — 演示 StorageKit 多存储域隔离
 */
class SampleStoreMainFragment : TabFragment<FragmentTabBinding, TabViewModel>() {

    override fun getTabTitles(): MutableList<String> = mutableListOf(
        "引擎切换", "基础操作", "多实例"
    )

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        EngineSwitchFragment(),
        BasicCrudFragment(),
        StorageKitDemoFragment(),
    )
}
