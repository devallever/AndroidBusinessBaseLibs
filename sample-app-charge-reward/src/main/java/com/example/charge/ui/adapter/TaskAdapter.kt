package com.example.charge.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.charge.ChargeApp
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.base.BaseBindingAdapter
import com.example.charge.data.TaskItem
import com.example.charge.databinding.RvTaskBinding
import com.example.charge.task.TaskHelper
import com.example.charge.task.TaskType
import com.example.charge.utils.log
import com.example.charge.utils.setOnSingleListener

class TaskAdapter : BaseBindingAdapter<TaskItem, RvTaskBinding>() {
    var optionClickListener: OptionClickListener? = null
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RvTaskBinding {
        return RvTaskBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun convert(
        helper: BaseBindViewHolder<RvTaskBinding>,
        item: TaskItem
    ) {
        helper.binding.apply {
            tvGreen.text = "$${item.greenValue}"
            tvProgressText.text = "${item.current}/${item.max}"
            progressBar.max = item.max
            progressBar.progress = item.current
            tvTaskDesc.text = " ${TaskHelper.getTaskDesc(item.id, item.max)}"

            val finisProgress = item.current >= item.max
            if (finisProgress) {
                tvProgressText.text = "${item.max}/${item.max}"
            }

            ivFinish.isVisible = item.isFinished
            btnAction.isVisible = !item.isFinished
//            ivAdTag.isVisible = finisProgress

            if (finisProgress) {
                btnAction.setBackgroundResource(R.drawable.ic_task_btn)
                tvAction.setShadowLayer(1f, 0f, 2f, ChargeApp.instance.getColor(R.color.color_016C03))
                tvAction.textColorBuilder.apply {
                    textStrokeColor = App.context.getColor(R.color.color_016C03)
                    intoTextColor()
                }
            } else {
                btnAction.setBackgroundResource(R.drawable.ic_task_btn_finish)
                tvAction.setShadowLayer(1f, 0f, 2f, ChargeApp.instance.getColor(R.color.color_595959))
                tvAction.textColorBuilder.apply {
                    textStrokeColor = App.context.getColor(R.color.color_595959)
                    intoTextColor()
                }
            }

            btnAction.setOnSingleListener {
                if (!finisProgress) {
                    if (App.DEBUG) {
                        log("进度未满, 不可点击")
                    }
                    return@setOnSingleListener
                }
                optionClickListener?.onClickButton(item, helper.bindingAdapterPosition)
            }

            when (item.type) {
                TaskType.CHARGE -> {
                    rootView.setBackgroundResource(R.drawable.ic_task_charge_item_bg)
                    bgLeft.setBackgroundResource(R.drawable.ic_task_charge_item_bg_left)
                    progressBg.setBackgroundResource(R.drawable.ic_task_charge_progress_bg)
                    tvTaskDesc.setShadowLayer(1f, 0f, 2f, ChargeApp.instance.getColor(R.color.color_710ABE))
                    tvTaskDesc.textColorBuilder.apply {
                        textStrokeColor = App.context.getColor(R.color.color_710ABE)
                        intoTextColor()
                    }
                }

                TaskType.HIT_MOLE -> {
                    rootView.setBackgroundResource(R.drawable.ic_task_mole_item_bg)
                    bgLeft.setBackgroundResource(R.drawable.ic_task_mole_item_bg_left)
                    progressBg.setBackgroundResource(R.drawable.ic_task_mole_progress_bg)
                    tvTaskDesc.setShadowLayer(1f, 0f, 2f, ChargeApp.instance.getColor(R.color.color_006AB5))
                    tvTaskDesc.textColorBuilder.apply {
                        textStrokeColor = App.context.getColor(R.color.color_006AB5)
                        intoTextColor()
                    }
                }

                TaskType.RECEIVE_COIN -> {
                    rootView.setBackgroundResource(R.drawable.ic_task_coin_item_bg)
                    bgLeft.setBackgroundResource(R.drawable.ic_task_coin_item_bg_left)
                    progressBg.setBackgroundResource(R.drawable.ic_task_coin_progress_bg)
                    tvTaskDesc.setShadowLayer(1f, 0f, 2f, ChargeApp.instance.getColor(R.color.color_3D5ECF))
                    tvTaskDesc.textColorBuilder.apply {
                        textStrokeColor = App.context.getColor(R.color.color_3D5ECF)
                        intoTextColor()
                    }
                }
            }

        }
    }

    interface OptionClickListener {
        fun onClickButton(taskItem: TaskItem, position: Int)
    }

}