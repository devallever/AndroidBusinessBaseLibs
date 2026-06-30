package com.plinkopro.wincash.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.plinkopro.wincash.base.BaseBindingAdapter
import com.plinkopro.wincash.business.withdraw.bean.ItemBean
import com.plinkopro.wincash.databinding.ItemInputContentBinding
import com.plinkopro.wincash.utils.setVisible
import kotlin.apply

class ConfirmAdapter : BaseBindingAdapter<ItemBean, ItemInputContentBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemInputContentBinding {
        return ItemInputContentBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun convert(
        helper: BaseBindViewHolder<ItemInputContentBinding>,
        item: ItemBean
    ) {
        helper.binding.apply {
            start.text =  item.start + " :"

            if (helper.adapterPosition == 0){

                if (start.layoutParams is LinearLayout.LayoutParams){
                    start.updateLayoutParams<LinearLayout.LayoutParams> {
                        width = 0
                        weight = 1f
                    }
                }
            }else{
                if (start.layoutParams is LinearLayout.LayoutParams){
                    start.updateLayoutParams<LinearLayout.LayoutParams> {
                        width = LinearLayout.LayoutParams.WRAP_CONTENT
                        weight = 0f
                    }
                }
            }

            end.isVisible = item.end is String
            ivImage.isVisible = item.end is Int
            if (item.end is String) {
                end.text = item.end as CharSequence
                if (item.isFailed) {
                    end.setTextColor("#FF2E00".toColorInt())
                }else{
                    end.setTextColor("#26B502".toColorInt())
                }
            } else if (item.end is Int) {
                end.text = ""
//                end.setCompoundDrawablesWithIntrinsicBounds((item.end as Int), 0, 0, 0)
                ivImage.setImageResource(item.end as Int)
            }
        }
    }
}