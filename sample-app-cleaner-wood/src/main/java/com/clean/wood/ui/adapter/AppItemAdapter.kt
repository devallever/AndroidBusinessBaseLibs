package com.clean.wood.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clean.wood.R
import com.clean.wood.data.model.AppItem
import com.clean.wood.databinding.RvAppBinding
import com.clean.wood.utils.StorageUtils
import com.clean.wood.utils.TimeUtils

class AppItemAdapter(val data: MutableList<AppItem>) : RecyclerView.Adapter<AppItemAdapter.VH>() {

    var itemClickListener: ItemClickListener? = null

    class VH(val binding: RvAppBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun onItemClick(item: AppItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RvAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.binding.apply {
            ivIcon.setImageDrawable(item.icon)
            tvName.text = item.appName
            tvTime.text = TimeUtils.formatTimeYYYY_MM_DD(item.installTime)
            tvSize.text = StorageUtils.convertBytesToMBOrGB(item.usageSize * 1024).replace(" ", "")

            ivSelect.setImageResource(if (item.select) R.drawable.ic_selected else R.drawable.ic_unselect)

            root.setOnClickListener {
                item.select = !item.select
                notifyItemChanged(position, position)
                itemClickListener?.onItemClick(item)
            }
        }
    }
}