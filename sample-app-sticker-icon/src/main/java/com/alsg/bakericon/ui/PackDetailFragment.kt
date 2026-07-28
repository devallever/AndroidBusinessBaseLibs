package com.alsg.bakericon.ui

import androidx.recyclerview.widget.GridLayoutManager
import com.alsg.bakericon.base.AppFragment
import com.alsg.bakericon.base.AppFragmentActivity
import com.alsg.bakericon.databinding.SiFragmentPackDetailBinding
import com.alsg.bakericon.ui.adapter.data.SingleItem
import com.alsg.bakericon.vm.PackDetailViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class PackDetailFragment : AppFragment<SiFragmentPackDetailBinding, PackDetailViewModel>() {
    companion object {
        private const val EXTRA_PATH_LIST = "pathList"
        fun start(title: String, list: MutableList<String>) {
            val arrayList = java.util.ArrayList<String>()
            arrayList.addAll(list)
            AppFragmentActivity.start<PackDetailFragment>(title) {
                it.putStringArrayList(EXTRA_PATH_LIST, arrayList)
            }
        }
    }

    override fun inflate() = SiFragmentPackDetailBinding.inflate(layoutInflater)

    override fun init() {
        val bundle = requireActivity().intent.getBundleExtra("fragmentArgs")
        bundle?.getStringArrayList(EXTRA_PATH_LIST)?.apply {
            map {
                val item = SingleItem()
                item.url = it
                mViewModel.list.add(item)
            }
        }

        mBinding.apply {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            recyclerView.adapter = mViewModel.adapter
            mViewModel.adapter.setList( mViewModel.list)
            mViewModel.adapter.setOnItemClickListener { adapter, view, position ->
                val item = mViewModel.adapter.getItem(position)
                PreviewActivity.start(item.url)
            }
        }
    }
}