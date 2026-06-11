package app.allever.android.lib.common

import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.helper.KeyEventHelper
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.chad.library.adapter.base.BaseQuickAdapter

abstract class ListFragment<DB : ViewBinding, VM : BaseViewModel, T> :
    BaseFragment<FragmentListBinding, ListViewModel>() {

    protected var mAdapter: BaseQuickAdapter<T, *>? = null

    /** 外部注入的数据源提供者（替代抽象方法，支持 public static Fragment） */
    var listProvider: (() -> MutableList<T>)? = null

    /** 外部注入的适配器提供者（替代抽象方法，支持 public static Fragment） */
    var adapterProvider: (() -> BaseQuickAdapter<T, *>)? = null

    override fun inflate() = FragmentListBinding.inflate(layoutInflater)

    override fun init() {

        initTopBar()

        mBinding.smartRefreshLayout.setEnableOverScrollDrag(true)

        mBinding.recyclerView.layoutManager = layoutManager()

        mAdapter = getAdapter()
        mBinding.recyclerView.adapter = mAdapter

        mAdapter?.setList(getList())

        mAdapter?.setOnItemClickListener { adapter, view, position ->
            onItemClick(position, mAdapter?.getItem(position) ?: return@setOnItemClickListener)
        }
    }

    private fun initTopBar() {
        val title = getTitle()
        val showTopBar = !TextUtils.isEmpty(title)
        if (showTopBar) {
            ViewHelper.setViewHeight(
                mBinding.statusBar,
                DisplayHelper.getStatusBarHeight(requireContext())
            )
            ViewHelper.setVisible(mBinding.topBar, showTopBar)
            mBinding.tvTitle.text = title
            mBinding.ivBack.setOnClickListener {
                KeyEventHelper.clickBack()
            }
        }
    }

    open fun getAdapter(): BaseQuickAdapter<T, *> =
        adapterProvider?.invoke() ?: throw NotImplementedError("请设置 adapterProvider 或重写 getAdapter()")

    open fun getList(): MutableList<T> =
        listProvider?.invoke() ?: throw NotImplementedError("请设置 listProvider 或重写 getList()")
    protected open fun onItemClick(position: Int, item: T) {

    }

    protected open fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(requireContext())
    }

    open fun getTitle(): String {
        return ""
    }

    protected fun updateList(list: MutableList<T>) {
        mAdapter?.setList(list)
    }
}

class ListViewModel : BaseViewModel()