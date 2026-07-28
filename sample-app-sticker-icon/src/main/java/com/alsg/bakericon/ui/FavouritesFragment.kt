package com.alsg.bakericon.ui

import androidx.recyclerview.widget.GridLayoutManager
import com.alsg.bakericon.base.AppFragment
import com.alsg.bakericon.databinding.FragmentFavouritesBinding
import com.alsg.bakericon.vm.FavouritesViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class FavouritesFragment : AppFragment<FragmentFavouritesBinding, FavouritesViewModel>() {
    override fun inflate() = FragmentFavouritesBinding.inflate(layoutInflater)

    override fun init() {
        initObserver()
        mBinding.apply {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            recyclerView.adapter = mViewModel.adapter
            mViewModel.adapter.setOnItemClickListener { adapter, view, position ->
                val item = mViewModel.adapter.getItem(position)
                PreviewActivity.start(item.url)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.fetchFavouriteData()
    }

    override fun initObserver() {
        mViewModel.listLiveData.observe(this) {
            mViewModel.adapter.setList(it)
        }
    }
}