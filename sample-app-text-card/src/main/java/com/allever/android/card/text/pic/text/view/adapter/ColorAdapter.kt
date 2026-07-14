package com.allever.android.card.text.pic.text.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.databinding.RvColorBinding

class ColorAdapter(val data: MutableList<ColorData> = mutableListOf()) :
    RecyclerView.Adapter<ColorAdapter.VH>() {

    var itemClick: ItemClick? = null

    class VH(val binding: RvColorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RvColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding = holder.binding
        binding.apply {
            val item = data[position]
            val colorList = item.colorValue
            gradientCircleView.setColorList(colorList)

            viewColorBg.isVisible = item.selected

            root.setOnClickListener {
                itemClick?.onItemClick(position, item)
            }
        }
    }

    interface ItemClick {
        fun onItemClick(position: Int, item: ColorData)
    }
}