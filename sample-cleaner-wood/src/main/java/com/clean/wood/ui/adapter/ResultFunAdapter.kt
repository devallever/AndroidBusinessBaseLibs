package com.clean.wood.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.data.model.ResultFunItem
import com.clean.wood.databinding.RvResultAdItemBinding
import com.clean.wood.databinding.RvResultFunctionBinding
import com.clean.wood.utils.Constant
import com.clean.wood.utils.log

class ResultFunAdapter(val data: MutableList<ResultFunItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class VH(val binding: RvResultFunctionBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun onBtnClick(item: ResultFunItem)
    }

    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            RvResultFunctionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VH) {
            val item = data[position]
            holder.binding.apply {
                ivIcon.setImageResource(item.iconRes)
                tvName.text = item.name
                tvDesc.text = item.desc
                btnGo.text = item.btn
                btnGo.setBackgroundResource(if (item.type == Constant.FunType.JUNK_CLEAN) R.drawable.shape_result_item_btn_red else R.drawable.shape_result_item_btn_green)
                btnGo.setOnClickListener {
                    itemClickListener?.onBtnClick(item)
                }
            }
        }
    }
}