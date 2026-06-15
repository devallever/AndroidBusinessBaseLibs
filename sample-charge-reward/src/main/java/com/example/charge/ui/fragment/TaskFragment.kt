package com.example.charge.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charge.base.BaseFragment
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.data.TaskItem
import com.example.charge.databinding.FragmentTaskBinding
import com.example.charge.event.ClaimTipsEvent
import com.example.charge.event.UpdateCurrencyEvent
import com.example.charge.task.TaskCategory
import com.example.charge.task.TaskHelper
import com.example.charge.task.TaskType
import com.example.charge.ui.adapter.TaskAdapter
import com.example.charge.utils.dp2px
import org.greenrobot.eventbus.EventBus

class TaskFragment : BaseFragment<FragmentTaskBinding>() {
    private var type: String = TaskType.CHARGE
    private var enterType: String = TaskType.CHARGE
    private var targetTaskItemIndex = -1

    private val adapter by lazy {
        TaskAdapter().apply {
            when (type) {
                TaskType.CHARGE -> {
                    setNewData(TaskHelper.chargeItemList)
                }

                TaskType.HIT_MOLE -> {
                    setNewData(TaskHelper.hitMoleItemList)
                }

                TaskType.RECEIVE_COIN -> {
                    setNewData(TaskHelper.receiveCoinItemList)
                }
            }
        }
    }

    companion object {

        private val EXTRA_TYPE = "EXTRA_TYPE"
        private val EXTRA_ENTER_TYPE = "EXTRA_ENTER_TYPE"

        fun newInstance(type: String, enterType: String): TaskFragment {
            val fragment = TaskFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_TYPE, type)
            bundle.putString(EXTRA_ENTER_TYPE,enterType)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTaskBinding {
        return FragmentTaskBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        type = arguments?.getString(EXTRA_TYPE, TaskType.CHARGE) ?: TaskType.CHARGE
        enterType = arguments?.getString(EXTRA_ENTER_TYPE, TaskType.CHARGE) ?: TaskType.CHARGE

        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTask.adapter = adapter
        appendBottomMargin(binding.rvTask)
        adapter.optionClickListener = object : TaskAdapter.OptionClickListener {
            override fun onClickButton(
                item: TaskItem,
                position: Int
            ) {
                when (type) {
                    TaskType.CHARGE -> {
                        handleChargeTask(item, position)
                    }

                    TaskType.HIT_MOLE -> {
                        handleHitMoleTask(item, position)
                    }

                    TaskType.RECEIVE_COIN -> {
                        handleReceiveCoinTask(item, position)
                    }
                }
            }
        }

        updateListData()
    }

    private fun handleChargeTask(item: TaskItem, position: Int) {
        val finishProgress = item.current >= item.max
        when (item.category) {
            TaskCategory.CHARGE_COLLECT -> {
                if (finishProgress) {
                    targetTaskItemIndex = position
                    TaskHelper.dotCharge(item.id)
                    when (item.category) {
                        TaskCategory.CHARGE_COLLECT -> {
                            TaskHelper.setCollectFinish(item.id)
                            updateItemFinish(item, targetTaskItemIndex)
                        }

                        TaskCategory.CHARGE_SIGN -> {
                            TaskHelper.setSignFinish(item.id)
                            updateItemFinish(item, targetTaskItemIndex)
                        }
                    }
                }
            }

            TaskCategory.CHARGE_SIGN -> {
                if (finishProgress) {
                    targetTaskItemIndex = position
                    TaskHelper.dotCharge(item.id)
                    when (item.category) {
                        TaskCategory.CHARGE_COLLECT -> {
                            TaskHelper.setCollectFinish(item.id)
                            updateItemFinish(item, targetTaskItemIndex)
                        }

                        TaskCategory.CHARGE_SIGN -> {
                            TaskHelper.setSignFinish(item.id)
                            updateItemFinish(item, targetTaskItemIndex)
                        }
                    }
                }
            }
        }
    }

    private fun handleHitMoleTask(item: TaskItem, position: Int) {
        val finishProgress = item.current >= item.max
        if (finishProgress) {
            targetTaskItemIndex = position
            TaskHelper.dotHitMole(item.id)
            when(item.category) {
                TaskCategory.HIT_MOLE -> {
                    TaskHelper.setHitMoleFinish(item.id)
                    updateItemFinish(item, targetTaskItemIndex)
                }
                TaskCategory.HIT_MOLE_GAME_COUNT -> {
                    TaskHelper.setHitMoleGameFinish(item.id)
                    updateItemFinish(item, targetTaskItemIndex)
                }
            }
        }
    }

    private fun handleReceiveCoinTask(item: TaskItem, position: Int) {
        val finishProgress = item.current >= item.max
        if (finishProgress) {
            targetTaskItemIndex = position
            TaskHelper.dotReceiveCoin(item.id)
            when(item.category) {
                TaskCategory.RECEIVE_COIN -> {
                    TaskHelper.setReceiveCoinFinish(item.id)
                    updateItemFinish(item, targetTaskItemIndex)
                }
                TaskCategory.RECEIVE_COIN_GAME_COUNT -> {
                    TaskHelper.setReceiveCoinGameFinish(item.id)
                    updateItemFinish(item, targetTaskItemIndex)
                }
            }
        }
    }

    private fun updateListData() {
        adapter.data.forEach {
            it.current = TaskHelper.getTaskProgress(it.id)
            it.isFinished = TaskHelper.checkFinish(it.id)
        }
        adapter.notifyDataSetChanged()
        TaskHelper.checkShowDot()
    }

    private fun appendBottomMargin(rv: RecyclerView) {
        // 移除已有的ItemDecoration，避免重复添加
        for (i in 0 until rv.itemDecorationCount) {
            rv.removeItemDecorationAt(i)
        }

        // 添加新的ItemDecoration，为最后一个item设置90dp的bottom margin
        rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: android.view.View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val adapter = parent.adapter
                if (adapter != null && position == adapter.itemCount - 1) {
                    // 为最后一个item添加60dp的底部margin
                    outRect.bottom = dp2px(90f)
                }
            }
        })
    }

    private fun updateItemFinish(item: TaskItem, position: Int) {
        item.isFinished = true
        adapter.data.remove(item)
        adapter.data.add(adapter.data.size, item)
        adapter.notifyDataSetChanged()
        CurrencyUtils.updateCurrencyNum(CurrencyType.GREEN, item.greenValue)
        EventBus.getDefault().post(UpdateCurrencyEvent(CurrencyType.GREEN))
        TaskHelper.checkShowDot()
        EventBus.getDefault().post(ClaimTipsEvent(item.greenValue))
    }
}