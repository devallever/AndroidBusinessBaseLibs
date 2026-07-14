package com.allever.android.card.text.pic.text.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.model.TemplateModel
import com.allever.android.card.text.pic.text.databinding.TcRvTemplateBinding
import com.allever.android.card.text.pic.text.util.log

class TemplateItemAdapter(val data: MutableList<TemplateModel<*>> = mutableListOf()) :
    RecyclerView.Adapter<TemplateItemAdapter.VH>() {


    var itemClick: ItemClick? = null

    class VH(val binding: TcRvTemplateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = TcRvTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.apply {
            val item = data[position]
            ivIcon.setImageResource(item.cover)
            tvName.setTextColor(App.getColor(if (item.selected) R.color.tc_theme_color else R.color.white))
            tvName.text = item.getTemplateName()
            log("${item.getTemplateName()} is selected = ${item.selected}")
            bgFrame.isVisible = item.selected

            root.setOnClickListener {
                itemClick?.onItemClick(position, item)
            }
        }
    }

    interface ItemClick {
        fun onItemClick(position: Int, item: TemplateModel<*>)
    }
}