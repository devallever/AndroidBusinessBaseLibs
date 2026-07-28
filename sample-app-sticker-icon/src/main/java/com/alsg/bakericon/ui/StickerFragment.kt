package com.alsg.bakericon.ui

import androidx.recyclerview.widget.GridLayoutManager
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.core.util.BarUtils
import com.alsg.bakericon.base.AppFragment
import com.alsg.bakericon.databinding.FragmentStickerBinding
import com.alsg.bakericon.vm.StickerViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class StickerFragment : AppFragment<FragmentStickerBinding, StickerViewModel>() {
    override fun inflate() = FragmentStickerBinding.inflate(layoutInflater)

    override fun init() {
        initObserver()
        mBinding.rvPack.layoutManager = GridLayoutManager(requireContext(), 2)
        mBinding.rvPack.adapter = mViewModel.packAdapter
        mViewModel.packAdapter.setList(mViewModel.packItemList)
        mViewModel.packAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mViewModel.packAdapter.getItem(position)
            PackDetailFragment.start(item.name, item.imageList)
        }

        mBinding.rvPopular.layoutManager = GridLayoutManager(requireContext(), 3)
        mBinding.rvPopular.adapter = mViewModel.popularItemAdapter
        mViewModel.popularItemAdapter.setList(mViewModel.popularItemList)
        mViewModel.popularItemAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mViewModel.popularItemAdapter.getItem(position)
            PreviewActivity.start(item.url)
        }

        mViewModel.fetchPackData()
        mViewModel.fetchPopularData()
    }

    private fun initObserver() {
        mViewModel.packItemListLiveData.observe(this) {
            mViewModel.packAdapter.setList(it)
        }


        mViewModel.popularItemListLiveData.observe(this) {
            mViewModel.popularItemAdapter.setList(it)
        }
    }
}