package com.example.charge.ui.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.charge.R
import com.example.charge.base.BaseActivity
import com.example.charge.databinding.ActivityTaskBinding
import com.example.charge.event.ClaimTipsEvent
import com.example.charge.init.InitManager
import com.example.charge.task.TaskType
import com.example.charge.ui.adapter.Pager2Adapter
import com.example.charge.ui.fragment.TaskFragment
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.ExtraKey
import com.example.charge.utils.formThousand
import com.example.charge.utils.setOnSingleListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TaskActivity : BaseActivity<ActivityTaskBinding>() {

    private val TAB_CHARGE = 0
    private val TAB_MOLE = 1
    private val TAB_COIN = 2

    private var taskType = TaskType.CHARGE

    private val fragmentList = listOf(
        TaskFragment.newInstance(TaskType.CHARGE, taskType),
        TaskFragment.newInstance(TaskType.HIT_MOLE, taskType),
        TaskFragment.newInstance(TaskType.RECEIVE_COIN, taskType)
    )

    private val pagerAdapter by lazy {
        Pager2Adapter(this, fragmentList)
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityTaskBinding {
        return ActivityTaskBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        taskType = intent?.getStringExtra(ExtraKey.TASK_TYPE)?: TaskType.CHARGE
        val index = when (taskType) {
            TaskType.CHARGE -> TAB_CHARGE
            TaskType.HIT_MOLE -> TAB_MOLE
            TaskType.RECEIVE_COIN -> TAB_COIN
            else -> TAB_CHARGE
        }
        initListener()
        initPager()
        switchTab(index)
    }

    fun initListener() {
        binding.apply {
            ivClose.setOnSingleListener {
                finish()
            }
            tabCharge.setOnSingleListener {
                switchTab(TAB_CHARGE)
            }
            tabMole.setOnSingleListener {
                switchTab(TAB_MOLE)
            }
            tabCoin.setOnSingleListener {
                switchTab(TAB_COIN)
            }
        }
    }

    private fun initPager () {
        binding.apply {
            viewPager.adapter = pagerAdapter
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    switchTab(position)
                }
            })
        }
    }

    private fun switchTab(tab: Int) {
        binding.viewPager.setCurrentItem(tab, false)
        binding.apply {
            when (tab) {
                TAB_CHARGE -> {
                    tabCharge.setBackgroundResource(R.drawable.ic_task_tab_charge)
                    tabMole.background = null
                    tabCoin.background = null
                }

                TAB_MOLE -> {
                    tabCharge.background = null
                    tabMole.setBackgroundResource(R.drawable.ic_task_tab_mole)
                    tabCoin.background = null
                }

                TAB_COIN -> {
                    tabCharge.background = null
                    tabMole.background = null
                    tabCoin.setBackgroundResource(R.drawable.ic_task_tab_coin)
                }
            }
        }
    }

    override fun enableEventBus() = true

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveClaimTipsEvent(event: ClaimTipsEvent) {
        binding.apply {
            tvGreen.text = "${CountryUtil.getSymbolByCode(InitManager.getCountryCode())} ${event.greenValue.formThousand()}"
            val objAnim = ObjectAnimator.ofFloat(tipsView, "alpha", 0f, 1f, 1f, 1f, 0f)
            objAnim.duration = 2000
            objAnim.addListener(onEnd = {
                tipsView.isVisible = false
            }, onStart = {
                tipsView.isVisible = true
            })
            objAnim.start()
        }
    }
}