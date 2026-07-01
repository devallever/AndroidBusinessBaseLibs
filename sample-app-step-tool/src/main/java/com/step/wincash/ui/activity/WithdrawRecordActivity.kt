package com.step.wincash.ui.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.step.wincash.R
import com.step.wincash.base.BaseActivity
import com.step.wincash.beans.WithdrawRecord
import com.step.wincash.business.withdraw.WithdrawBusiness
import com.step.wincash.databinding.StActivityWithdrawRecordBinding
import com.step.wincash.event.NetworkChangeEvent
import com.step.wincash.event.ShowInterAdEvent
import com.step.wincash.utils.isNetworkAvailable
import com.step.wincash.utils.setOnSingleListener
import com.step.wincash.viewmodel.WithdrawRecordViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WithdrawRecordActivity : BaseActivity<StActivityWithdrawRecordBinding>() {
    //viewModel
    private val mViewModel by viewModels<WithdrawRecordViewModel>()
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StActivityWithdrawRecordBinding {
        return StActivityWithdrawRecordBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fixStatusBar(binding.topBarBg)
        registerEventbus()
        // 禁止截屏
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        mViewModel.initExtra(intent)
        initView()
        initData()
        initObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRefreshAnimator.pause()
    }

    private fun initView() {
        binding.apply {
            ivBack.setOnClickListener {
                EventBus.getDefault().post(ShowInterAdEvent())
                finish()
            }
        }

        binding.rvWithdrawRecord.layoutManager = LinearLayoutManager(this)
        binding.rvWithdrawRecord.adapter = mViewModel.recordAdapter
        mViewModel.recordAdapter.setEmptyView(layoutInflater.inflate(R.layout.layout_empty_record, null, false))
//        mViewModel.recordAdapter.onItemClickListener =
//            BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
//                mViewModel.recordAdapter.data[position]?.let {
//                    showXPopup(WithdrawRankDialog(this, it), autoDismiss = true)
//                }
//            }

        val networkEnable = isNetworkAvailable()
        binding.rvWithdrawRecord.isVisible = networkEnable
        binding.networkErrorLayout.isVisible = !networkEnable
        binding.btnRetry.setOnSingleListener {
            if (isPlayAnim) {
                return@setOnSingleListener
            }
            mRefreshAnimator.start()
            isPlayAnim = true
            lifecycleScope.launch {
                delay(3000)
                mRefreshAnimator.pause()
                isPlayAnim = false
            }
        }
    }

    private fun initData() {
    }

    private fun initObserver() {
        WithdrawBusiness.recordListLiveData.observe(this) {
            it ?: return@observe
            mViewModel.listData.clear()
            it.forEach { item ->
                // 确保item是WithdrawRecord类型
                if (item is WithdrawRecord && item.currencyType == mViewModel.currencyType.type) {
                    mViewModel.listData.add(item)
                }
            }
            mViewModel.recordAdapter.notifyDataSetChanged()
        }
    }

    private var isPlayAnim = false
    private val mRefreshAnimator by lazy {
        val animator = ObjectAnimator.ofFloat(binding.ivRefresh, "rotation", 0f, 360f)
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator
    }

    private fun initRefreshAnimator() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveNetworkChangeEvent(event: NetworkChangeEvent) {
        binding.rvWithdrawRecord.isVisible = event.isConnected
        binding.networkErrorLayout.isVisible = !event.isConnected
    }

}