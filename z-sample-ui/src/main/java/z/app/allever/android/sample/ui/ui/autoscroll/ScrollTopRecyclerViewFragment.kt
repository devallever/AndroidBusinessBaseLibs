package z.app.allever.android.sample.ui.ui.autoscroll

import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.adapter.TextAdapter
import z.app.allever.android.sample.ui.databinding.FragmentScrollTopRecyclerViewBinding
import z.app.allever.android.sample.ui.ui.widget.SmartAutoScrollRecyclerView
import app.allever.android.lib.mvvm.base.BaseViewModel

class ScrollTopRecyclerViewFragment :
    BaseFragment<FragmentScrollTopRecyclerViewBinding, BaseViewModel>() {

    override fun inflate() = FragmentScrollTopRecyclerViewBinding.inflate(layoutInflater)

    private val mAdapter = TextAdapter()

    override fun init() {
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.direction = SmartAutoScrollRecyclerView.DIRECTION_TOP
        mBinding.recyclerView.start()
        initTestData()
    }

    private fun initTestData() {
        val list = mutableListOf<String>()
        for (i in 0..29) {
            list.add("$i")
        }
        mAdapter.setList(list)
    }
}