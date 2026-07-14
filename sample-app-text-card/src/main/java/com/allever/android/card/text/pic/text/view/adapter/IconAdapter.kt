package com.allever.android.card.text.pic.text.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.allever.android.card.text.pic.text.model.IconData
import com.allever.android.card.text.pic.text.databinding.RvIconBinding

class IconAdapter(val data: MutableList<IconData>) : RecyclerView.Adapter<IconAdapter.VH>() {

    var listener: Listener? = null

    class VH(val binding: RvIconBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RvIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding = holder.binding
        binding.apply {
            val item = data[position]
            iconBg.isVisible = item.selected
            ivIcon.setImageResource(item.icon)

            root.setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }

    interface Listener {
        fun onItemClick(position: Int, item: IconData)
    }
}