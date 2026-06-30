package com.plinkopro.wincash.ui.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.plinkopro.wincash.base.BaseBindingAdapter
import com.plinkopro.wincash.databinding.ItemBrTypeBinding
import com.plinkopro.wincash.utils.setOnSingleListener
import kotlin.apply
import kotlin.collections.indices

class BrAccountTypeAdapter(val listener: (String) -> Unit) : BaseBindingAdapter<String, ItemBrTypeBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemBrTypeBinding {
        return  ItemBrTypeBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun convert(
        helper: BaseBindViewHolder<ItemBrTypeBinding>,
        item: String
    ) {
        helper.binding.root.apply {
            text = item
           isSelected = selectedPosition == helper.bindingAdapterPosition
          setOnSingleListener {
                selectedPosition = helper.bindingAdapterPosition
                notifyDataSetChanged()
                listener.invoke(item)
            }
        }
    }

    private var selectedPosition = 0

    fun show(currentType: String?, allTypes: List<String>) {
        for (i in allTypes.indices) {
            if (TextUtils.equals(currentType, allTypes[i])) {
                selectedPosition = i
                break
            }
        }
        setNewData(allTypes as MutableList<String>?)
    }

}