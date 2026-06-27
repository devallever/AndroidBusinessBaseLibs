package com.clean.wood.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.clean.wood.WoodApp
import com.clean.wood.data.model.FunItem
import com.clean.wood.databinding.RvFunctionBinding

class HomeFunAdapter(val data: MutableList<FunItem>) : RecyclerView.Adapter<HomeFunAdapter.VH>() {

    class VH(val binding: RvFunctionBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun onItemClick(item: FunItem)
    }

    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RvFunctionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.binding.apply {
            ivBg.setImageResource(item.bgRes)
            ivFun.setImageResource(item.iconRes)
            tvFun.text = item.name
            tvFun.setTextColor(ContextCompat.getColor(WoodApp.context, item.colorRes))
            ivBg.setOnClickListener {
                itemClickListener?.onItemClick(item)
            }
        }
    }
}