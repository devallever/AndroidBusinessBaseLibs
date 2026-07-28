package com.alsg.bakericon.ui

import androidx.recyclerview.widget.GridLayoutManager
import com.allever.lib.base.helper.ViewHelper
import com.allever.lib.base.util.BarUtils
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
        mBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        mBinding.recyclerView.adapter = mViewModel.adapter
        mViewModel.adapter.data = mViewModel.itemList
        mViewModel.adapter.setOnItemClickListener { adapter, view, position ->
            val item = mViewModel.adapter.getItem(position)
            PreviewActivity.start(item.url)
        }

        mViewModel.fetchTopData()
    }

    override fun initObserver() {
        mViewModel.itemListLiveData.observe(this) {
            mViewModel.adapter.setList(it)
        }
    }
}