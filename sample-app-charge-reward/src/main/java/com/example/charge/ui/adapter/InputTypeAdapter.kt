package com.example.charge.ui.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.charge.R
import com.example.charge.base.BaseBindingAdapter
import com.example.charge.data.InputTypeItem
import com.example.charge.databinding.RvInputTypeBinding
import com.example.charge.utils.setOnSingleListener

class InputTypeAdapter(val listener: (InputTypeItem) -> Unit): BaseBindingAdapter<InputTypeItem, RvInputTypeBinding>() {

    private var selectedPosition = 0

    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RvInputTypeBinding {
        return RvInputTypeBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        helper: BaseBindViewHolder<RvInputTypeBinding>,
        item: InputTypeItem
    ) {
        helper.binding.apply {
            tvAccount.text = item.name
            tvAccount.isSelected = selectedPosition == helper.bindingAdapterPosition
            if (tvAccount.isSelected) {
                rootView.setBackgroundResource(R.drawable.rv_input_type_selected_bg)
            } else {
                rootView.background = null
            }
            rootView.setOnSingleListener {
                selectedPosition = helper.bindingAdapterPosition
                notifyDataSetChanged()
                listener.invoke(item)
            }
        }
    }
    fun show(currentType: InputTypeItem?, allTypes: List<InputTypeItem>) {
        for (i in allTypes.indices) {
            if (TextUtils.equals(currentType?.name, allTypes[i].name)) {
                selectedPosition = i
                break
            }
        }
        setNewData(allTypes as MutableList<InputTypeItem>?)
    }

}