package com.allever.android.card.text.pic.text.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.databinding.FragmentColorBinding
import com.allever.android.card.text.pic.text.view.adapter.ColorAdapter

class ColorFragment(val pageIndex: Int) : Fragment() {
    private lateinit var mBinding: FragmentColorBinding
    private val colorDataList = mutableListOf<ColorData>()

    var colorListener: ColorListener? = null

    private val colorAdapter by lazy {
        ColorAdapter().apply {
            data.clear()
            data.addAll(colorDataList)

            itemClick = object : ColorAdapter.ItemClick {
                override fun onItemClick(position: Int, item: ColorData) {
                    data.map {
                        it.selected = false
                        if (it.name == item.name) {
                            it.selected = true
                        }
                    }
                    notifyDataSetChanged()

                    colorListener?.onColorSelect(pageIndex, item)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentColorBinding.inflate(layoutInflater, container, false)
        mBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 8)
        mBinding.recyclerView.adapter = colorAdapter
        return mBinding.root
    }

    fun updateData(colorData: MutableList<ColorData>) {
        colorAdapter.data.clear()
        colorAdapter.data.addAll(colorData)
        colorAdapter.notifyDataSetChanged()
    }

    fun notifySelectedChanged() {
        colorAdapter.notifyDataSetChanged()
    }

    private fun updateSelectColor() {

    }

    interface ColorListener {
        fun onColorSelect(pageIndex: Int, colorData: ColorData)
    }
}