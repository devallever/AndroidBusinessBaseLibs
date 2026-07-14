package com.allever.android.card.text.pic.text.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.model.WordFormatItem
import com.allever.android.card.text.pic.text.databinding.TcRvWordCountFormatBinding

class WordFormatAdapter(val data: MutableList<WordFormatItem>) :
    RecyclerView.Adapter<WordFormatAdapter.VH>() {

    var listener: Listener? = null

    class VH(val binding: TcRvWordCountFormatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            TcRvWordCountFormatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.apply {
            val item = data[position]
            val count = 194
            tvWordCount.text = App.context.getString(item.format, count)
            ivSelect.isVisible = item.selected

            root.setOnClickListener {
                listener?.onItemClick(position, item)
            }
        }
    }

    interface Listener {
        fun onItemClick(position: Int, item: WordFormatItem)
    }
}