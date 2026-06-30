package com.plinkopro.wincash.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseBindingAdapter
import com.plinkopro.wincash.beans.Cell
import com.plinkopro.wincash.databinding.ItemCellBinding
import com.plinkopro.wincash.utils.invisible
import com.plinkopro.wincash.utils.setVisible

class CellAdapter : BaseBindingAdapter<Cell, ItemCellBinding>() {

    val textLight = "#F42646".toColorInt()
    val textColor = "#FF974901".toColorInt()
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemCellBinding {
        return  ItemCellBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        helper: BaseBindViewHolder<ItemCellBinding>,
        cell: Cell
    ) {
        helper.binding.apply {

            if (cell.isCenter ) {
                root.invisible()
                return
            }
            // 设置文本样式
            tvValue.apply {
                if (cell.light) {
                    textColorBuilder
                        .setTextColor(textLight)
                        .intoTextColor()
                } else {
                    textColorBuilder
                        .setTextColor(textColor)
                        .intoTextColor()
                }
                tvValue.text = cell.text
            }

            selectImg.setVisible(cell.light)
            unselectImg.setVisible(!cell.light)

            if (cell.text.isNotEmpty()) {
                val num = cell.text.toInt()
                // 设置钱图标
                moneyIcon.setImageResource(
                    when {
                        num >= 15000 -> R.drawable.ic_green_5
                        num >= 4000 -> R.drawable.ic_green_4
                        num >= 2000 -> R.drawable.ic_green_3
                        num >= 1000 -> R.drawable.ic_green_2
                        else -> R.drawable.ic_green_1
                    }
                )
            }
        }
    }
}