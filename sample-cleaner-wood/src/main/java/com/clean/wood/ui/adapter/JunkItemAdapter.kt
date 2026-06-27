package com.clean.wood.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.model.JunkItem
import com.clean.wood.databinding.RvJunkBinding
import com.clean.wood.utils.Constant
import com.clean.wood.utils.toast

class JunkItemAdapter(val data: MutableList<JunkItem>) :
    RecyclerView.Adapter<JunkItemAdapter.VH>() {

    var itemClickListener: ItemClickListener? = null

    class VH(val binding: RvJunkBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun onItemClick(item: JunkItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RvJunkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.binding.apply {
            ivIcon.setImageResource(item.icon)
            tvType.text = item.label
            ivSelect.isVisible = !item.scanning
            ivSelect.setImageResource(if (item.select) R.drawable.ic_selected else R.drawable.ic_unselect)
            progressBar.isVisible = item.scanning

            root.setOnClickListener {
                if (item.scanning) {
                    toast(WoodApp.context.getString(R.string.scanning))
                } else {
                    item.select = !item.select
                    notifyItemChanged(position, position)
                    itemClickListener?.onItemClick(item)
                }
            }
        }
    }

    fun selectTypeList(): MutableList<Constant.JunkType> {
        val list = mutableListOf<Constant.JunkType>()
        data.forEach {
            if (it.select) {
                list.add(it.junkType)
            }
        }
        return list
    }
}