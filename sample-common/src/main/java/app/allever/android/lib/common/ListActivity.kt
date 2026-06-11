package app.allever.android.lib.common

import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.FragmentHelper
import com.chad.library.adapter.base.BaseQuickAdapter

abstract class ListActivity<VB, VM, T> : BaseActivity<ActivityListBinding, ListViewModel>() {

    /**
     * 内部使用的列表 Fragment（public static，满足系统重建要求）
     * 通过 listProvider/adapterProvider 属性注入数据源和适配器
     */
    class InternalListFragment<T> :
        ListFragment<FragmentListBinding, ListViewModel, T>()

    override fun inflateChildBinding(): ActivityListBinding =
        ActivityListBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar(getPageTitle())
        val fragment = InternalListFragment<T>()
        // 通过属性注入适配器数据源
        fragment.listProvider = { getList() }
        fragment.adapterProvider = { getAdapter() }
        FragmentHelper.addToContainer(supportFragmentManager, fragment, R.id.fragmentContainer)
    }

    abstract fun getPageTitle(): String
    abstract fun getAdapter(): BaseQuickAdapter<T, *>
    abstract fun getList(): MutableList<T>
}