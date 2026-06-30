package com.plinkopro.wincash.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView

import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseBindingAdapter
import com.plinkopro.wincash.beans.ScratchItem
import com.plinkopro.wincash.databinding.ItemScratchBinding
import com.plinkopro.wincash.utils.dp2px
import com.plinkopro.wincash.utils.gone
import com.plinkopro.wincash.utils.visible

class ScratchAdapter(val resetAllCallback: (aware: Int, multiple : Int) -> Unit) : BaseBindingAdapter<ScratchItem, ItemScratchBinding>() {

    var resetAllState = false
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemScratchBinding {
        return ItemScratchBinding.inflate(inflater, parent, false)
    }

    val textColor = "#FF974901".toColorInt()
    val textLightStart = "#FFF42646".toColorInt()
    val textLightEnd = "#FFC60122".toColorInt()

    override fun convert(
        helper: BaseBindViewHolder<ItemScratchBinding>,
        item: ScratchItem
    ) {

        helper.binding.apply {

            scratch.animate().cancel()
            scratch.alpha = 1f

            if (item.text<10){
                tvMultiple.text = "x${item.text}"
                tvMultiple.visible()
                awareIcon.gone()
                tvPrize.gone()
                tvMultiple.textColorBuilder
                    .setTextColor(textColor)
                    .intoTextColor()
            }else{
                tvPrize.text = item.text.toString()
                tvMultiple.gone()
                awareIcon.visible()
                tvPrize.visible()

                tvPrize.textColorBuilder
                    .setTextColor(textColor)
                    .intoTextColor()

                awareIcon.setImageResource(
                    when (item.level) {
                        0 -> R.drawable.ic_scratch_aware1
                        1 -> R.drawable.ic_scratch_aware2
                        2 -> R.drawable.ic_scratch_aware3
                        3 -> R.drawable.ic_scratch_aware4
                        else ->R.drawable.ic_scratch_aware1
                    }
                )
            }

            if (resetAllState){
                if (item.text<10 || item.level == 0){
                    tvPrize.textColorBuilder
                        .setTextStrokeSize(dp2px(1f))
                        .setTextStrokeColor(Color.WHITE)
                        .setTextGradientColors(textLightStart, textLightEnd)
                        .intoTextColor()
                }
            }

            // 关键：当前格子达到阈值时，只清除自身，并把状态写回数据
            scratch.onRevealed = {
                val pos = helper.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && !item.revealed) {
                    item.revealed = true
                    judgeState()
                }
            }

            // 根据状态恢复视图
            if (item.revealed) {
                scratch.revealAll(animation = false) // 这里的 revealAll 是“清空本视图遮罩”
            } else {
                scratch.reset()
            }
        }
    }

    /** 若需要从外部“全部重置” */
    fun resetAll() {
        mData.forEach { it.revealed = false }
        notifyDataSetChanged()
    }
    // 判断满足刮开所有遮挡的条件
    fun judgeState() {
        val targetItems = mData.filter { item -> item.level == 0 || item.text < 10}
        resetAllState =  targetItems.all { it.revealed }
        if (resetAllState){
            mData.forEach { it.revealed = true }
            setNewData(mData)
            val aware = mData.find { it.level == 0 }?.text?:0
            val multiple = mData.find { it.text <10 }?.text?:0
            resetAllCallback.invoke(aware, multiple)
        }
    }
}