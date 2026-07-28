package com.alsg.bakericon.ui

import androidx.recyclerview.widget.GridLayoutManager
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.core.util.BarUtils
import com.alsg.bakericon.base.AppFragment
import com.alsg.bakericon.databinding.FragmentTopBinding
import com.alsg.bakericon.vm.TopViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class TopFragment : AppFragment<FragmentTopBinding, TopViewModel>() {
    override fun inflate() = FragmentTopBinding.inflate(layoutInflater)

    override fun init() {
        initObserver()
        mBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        mBinding.recyclerView.adapter = mViewModel.adapter
        mViewModel.adapter.setList(mViewModel.itemList)
        mViewModel.adapter.setOnItemClickListener { adapter, view, position ->
            val item = mViewModel.adapter.getItem(position)
            PreviewActivity.start(item.url)
        }

        mViewModel.fetchTopData()
    }

    private fun initObserver() {
        mViewModel.itemListLiveData.observe(this) {
            mViewModel.adapter.setList(it)
        }
    }
}