package com.plinkopro.wincash.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import app.allever.android.lib.core.app.App
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseActivity
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.beans.ExtraKey
import com.plinkopro.wincash.beans.WithdrawRecord
import com.plinkopro.wincash.business.withdraw.WdDialogManager
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.StActivityWithdrawBinding
import com.plinkopro.wincash.event.AdDismissEvent
import com.plinkopro.wincash.event.ChangeShowPage
import com.plinkopro.wincash.event.ShowInterAdEvent
import com.plinkopro.wincash.event.UpdateCurrencyEvent
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.ui.dialog.DebugAddBalanceDialog
import com.plinkopro.wincash.ui.dialog.DebugSetWIthdrawRankDialog
import com.plinkopro.wincash.ui.dialog.WithdrawNotSufficientDialog
import com.plinkopro.wincash.ui.dialog.WithdrawWaitingDialog
import com.plinkopro.wincash.ui.dialog.guide.Guide4Dialog
import com.plinkopro.wincash.ui.dialog.guide.Guide5Dialog
import com.plinkopro.wincash.ui.dialog.guide.Guide6Dialog
import com.plinkopro.wincash.utils.CurrencyUtils
import com.plinkopro.wincash.utils.InterAdUtil
import com.plinkopro.wincash.utils.KeyboardHelper
import com.plinkopro.wincash.utils.PopupHelper
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.formThousand
import com.plinkopro.wincash.utils.log
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.showXPopup
import com.plinkopro.wincash.viewmodel.WithdrawViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class STWithdrawActivity : BaseActivity<StActivityWithdrawBinding>() {

    private val mViewModel: WithdrawViewModel by viewModels()
    lateinit var wdDialogManager: WdDialogManager
    private var keyboardHelper: KeyboardHelper? = null


    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StActivityWithdrawBinding {
        return StActivityWithdrawBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 禁止截屏
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        fixStatusBar(binding.topBarBg)
        registerEventbus()
        mViewModel.initExtra(intent)
        initView()
        initObserver()
        initWdDialogManager()
        showGuide()
        logShowWithdrawEvent()
        
        // 初始化键盘监听助手
        keyboardHelper = KeyboardHelper.create(this) { visible ->
            onKeyboardVisibilityChanged(visible)
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.updateBalance()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().post(ChangeShowPage(2))
        // 移除键盘监听器
        keyboardHelper?.removeKeyboardListener()
    }

    private fun initObserver() {
        mViewModel.balanceLiveData.observe(this) {
            binding.tvBalance.text = CurrencyUtils.getCurrencyNum(mViewModel.currencyType).formThousand()
        }
        mViewModel.level1AmountLiveData.observe(this) {
            binding.tvAmountProgress1.text = it
        }
        mViewModel.level2AmountLiveData.observe(this) {
            binding.tvAmountProgress2.text = it
        }
        mViewModel.level1AmountProgressLiveData.observe(this) {
            if (mViewModel.level1Record != null) {
                binding.amountProgress1.progress = 100
            } else {
                binding.amountProgress1.progress = it
            }

        }
        mViewModel.level2AmountProgressLiveData.observe(this) {
            if (mViewModel.level2Record != null) {
                binding.amountProgress2.progress = 100
            } else {
                binding.amountProgress2.progress = it
            }
        }
        WithdrawBusiness.recordListLiveData.observe(this) { list ->
            list?: return@observe
            list.forEach { item ->
                // 确保item是WithdrawRecord类型
                if (item is WithdrawRecord) {
                    if (item.level == WithdrawBusiness.WITHDRAW_LEVEL_1 && item.currencyType == mViewModel.currencyType.type) {
                        mViewModel.level1Record = item
                        updateCashOutOrExpedite(item)
                        binding.tvAccelerateRank1.text = getString(R.string.withdraw_accelerate_rank, item.rank)
                    }

                    if (item.level == WithdrawBusiness.WITHDRAW_LEVEL_2 && item.currencyType == mViewModel.currencyType.type) {
                        mViewModel.level2Record = item
                        updateCashOutOrExpedite(item)
                    }
                }
            }
        }
    }

    private fun updateRank1VisibleUi(finish: Boolean) {
        binding.btnExpedite1.isVisible = !finish
        binding.ivTips1.isVisible = finish
    }


    private fun updateRank2VisibleUi(finish: Boolean) {
        binding.btnExpedite2.isVisible = !finish
        binding.ivTips2.isVisible = finish
    }

    private fun initView() {
        binding.apply {
            ivBack.setOnSingleListener {
                finish()
            }
            if (App.DEBUG) {
                tvBalance.setOnLongClickListener {
                    showXPopup(
                        DebugAddBalanceDialog(mViewModel.currencyType, this@STWithdrawActivity),
                        autoDismiss = true
                    )
                    return@setOnLongClickListener true
                }
                levelContainer1.setOnLongClickListener {
                    if (mViewModel.level1Record == null) {
                        return@setOnLongClickListener true
                    }
                    showXPopup(
                        DebugSetWIthdrawRankDialog(mViewModel.level1Record!!, mViewModel.currencyType, this@STWithdrawActivity),
                        autoDismiss = true
                    )
                    true
                }
                levelContainer2.setOnLongClickListener {
                    if (mViewModel.level2Record == null) {
                        return@setOnLongClickListener true
                    }
                    showXPopup(
                        DebugSetWIthdrawRankDialog(mViewModel.level2Record!!, mViewModel.currencyType, this@STWithdrawActivity),
                        autoDismiss = true
                    )
                    true
                }

                levelContainer1.setOnClickListener {
                    if (mViewModel.level1Record != null) {
                        log("排名终点：${mViewModel.level1Record?.endRank}")
                    }
                }
                levelContainer2.setOnClickListener {
                    if (mViewModel.level2Record != null) {
                        log("排名终点：${mViewModel.level2Record?.endRank}")
                    }
                }
            }

            tvRecord.setOnSingleListener {
                goTo<WithdrawRecordActivity>(this@STWithdrawActivity) {
                    putExtra(ExtraKey.CURRENCY_TYPE, mViewModel.currencyType.type)
                }
            }
            tvCashOut1.setOnSingleListener {
                handleCashOut(WithdrawBusiness.WITHDRAW_LEVEL_1)
            }
            tvCashOut2.setOnSingleListener {
                handleCashOut(WithdrawBusiness.WITHDRAW_LEVEL_2)
            }
            btnExpedite1.setOnSingleListener {
                handleExpedite(WithdrawBusiness.WITHDRAW_LEVEL_1)
            }
            btnExpedite2.setOnSingleListener {
                handleExpedite(WithdrawBusiness.WITHDRAW_LEVEL_2)
            }
            ivTips1.setOnSingleListener {
                showXPopup(WithdrawWaitingDialog(this@STWithdrawActivity))
            }
            ivTips2.setOnSingleListener {
                showXPopup(WithdrawWaitingDialog(this@STWithdrawActivity))
            }
        }

        binding.rvWallet.layoutManager =
            GridLayoutManager(this, 3)
        binding.rvWallet.adapter = mViewModel.paymentAdapter

        binding.tvId.text = mViewModel.getUserId()

        binding.tvAmountSymbols1.text = mViewModel.getSymbolByCode(1)
        binding.tvAmountSymbols2.text = mViewModel.getSymbolByCode(2)

        binding.amountProgress1.setProgressColor("#EE7E1D", "#EAD1AD")
        binding.amountProgress2.setProgressColor("#EE7E1D", "#EAD1AD")

        binding.balanceContainer.setBackgroundResource(mViewModel.getBalanceContainerBg())
        binding.ivCoinType1.setImageResource(mViewModel.getCoinTypeIcon())
        binding.ivCoinType2.setImageResource(mViewModel.getCoinTypeIcon())

        initCashOutOrExpedite()

    }

    private fun handleExpedite(level: Int) {
        mViewModel.clickRecordLevel = level
        BaseApplication.postAdDismissEvent(AdIndex.WITHDRAW_ACCELERATE_INDEX)
    }


    private fun initCashOutOrExpedite() {
        if (mViewModel.hasWithdrawRecord(WithdrawBusiness.WITHDRAW_LEVEL_1)) {
            binding.tvCashOut1.isVisible = false
            binding.tvAccelerateRank1.isVisible = true
            binding.btnExpedite1.isVisible = true
            binding.ivTips1.isVisible = false
        }

        if (mViewModel.hasWithdrawRecord(WithdrawBusiness.WITHDRAW_LEVEL_2)) {
            binding.tvCashOut2.isVisible = false
            binding.tvAccelerateRank2.isVisible = true
            binding.btnExpedite2.isVisible = true
            binding.ivTips2.isVisible = false
        }
    }

    private fun updateCashOutOrExpedite(record: WithdrawRecord) {
        when (record.level) {
            WithdrawBusiness.WITHDRAW_LEVEL_1 -> {
                binding.tvCashOut1.isVisible = false
                binding.tvAccelerateRank1.isVisible = true
                binding.tvAccelerateRank1.text = getString(R.string.withdraw_accelerate_rank, record.rank)
                binding.btnExpedite1.isVisible = !record.finish
                binding.ivTips1.isVisible = record.finish

            }
            WithdrawBusiness.WITHDRAW_LEVEL_2 -> {
                binding.tvCashOut2.isVisible = false
                binding.tvAccelerateRank2.isVisible = true
                binding.tvAccelerateRank2.text = getString(R.string.withdraw_accelerate_rank, record.rank)
                binding.btnExpedite2.isVisible = !record.finish
                binding.ivTips2.isVisible = record.finish
            }
        }
    }

    private fun initData() {
    }

    @SuppressLint("DefaultLocale")
    fun initWdDialogManager() {
        wdDialogManager = WdDialogManager { account, paymentParams ->
            val limit = WithdrawBusiness.getWithdrawCurrencyLabelValue(InitManager.getCountryCode(), mSelectLevel)
            //减少减少金币
            val reduceValue = WithdrawBusiness.getWithdrawCurrencyLimit(mViewModel.currencyType, mSelectLevel)
            CurrencyUtils.updateCurrencyNum(mViewModel.currencyType, -reduceValue.toFloat())
            val record = WithdrawRecord(WithdrawBusiness.getStartRank(), System.currentTimeMillis(), limit, InitManager.getCountryCode(), mViewModel.currencyType.type, mSelectLevel, mViewModel.selectPayment().paymentName)
            WithdrawBusiness.insertRecord(record) {
                EventBus.getDefault().post(UpdateCurrencyEvent(mViewModel.currencyType, this))
                lifecycleScope.launch {
                    updateCashOutOrExpedite(record)
                    if (record.level == WithdrawBusiness.WITHDRAW_LEVEL_2) {
                        mViewModel.level2Record = record
                        binding.tvAccelerateRank2.text = getString(R.string.withdraw_accelerate_rank, record.rank)
                    }
                    if (record.level == WithdrawBusiness.WITHDRAW_LEVEL_1) {
                        mViewModel.level1Record = record
                        binding.tvAccelerateRank1.text = getString(R.string.withdraw_accelerate_rank, record.rank)
                    }

                }
            }

        }
    }

    private var mSelectLevel = 1
    private fun handleCashOut(level: Int) {
        mSelectLevel = level

        mViewModel.handleCashOut(level, notEnough = {
            showXPopup(WithdrawNotSufficientDialog(this@STWithdrawActivity))
        }, enough = {
            wdDialogManager.show(this, WithdrawBusiness.getWithdrawCurrencyLabelValue(InitManager.getCountryCode(), level).toFloat(), mViewModel.selectPayment())
        })
    }

    private fun showGuide() {

        if (getGuideState()) {
            PopupHelper.createDialog(
                this, Guide4Dialog(
                    this,
                    actionCallback = {
                        showGuide5()
                    }
                )).show()
        }
    }

    private fun showGuide5() {
        PopupHelper.createDialog(
            this, Guide5Dialog(
                this, binding.tvCashOut1,
                actionCallback = {
                    showGuide6()
                }
            )).show()
    }

    private fun showGuide6() {
        PopupHelper.createDialog(
            this, Guide6Dialog(
                this,
                actionCallback = {
                    setGuideFinish()
                    finish()
                }
            )).show()
    }

    fun setGuideFinish() {
        SpUtil.put(SpKey.GUIDE, false)
    }

    fun getGuideState(): Boolean {
        return SpUtil.get(SpKey.GUIDE, true)
    }

    private fun logShowWithdrawEvent() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateUpdateCurrencyEvent(event: UpdateCurrencyEvent) {
        mViewModel.updateBalance()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showInterAdEvent(event: ShowInterAdEvent) {
        binding.root.postDelayed({
            if (InterAdUtil.showAd()) {
                BaseApplication.postAdDismissEvent(AdIndex.ADMOB_INTER_INDEX)
            }
        }, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdDismissEvent(event: AdDismissEvent) {
        if (event.adIndex == AdIndex.WITHDRAW_ACCELERATE_INDEX) {
            val withdrawRecord = if (mViewModel.clickRecordLevel == WithdrawBusiness.WITHDRAW_LEVEL_1) {
                mViewModel.level1Record
            } else {
                mViewModel.level2Record
            }
            withdrawRecord?.let {
                WithdrawBusiness.accelerateRank(withdrawRecord) {
//                    updateRankVisibleUi1(true)
                }
            }

        }
    }

    /**
     * 软键盘显示状态变化回调
     * @param visible 键盘是否可见
     */
    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        if (App.DEBUG) {
            log("WithdrawActivity", "键盘状态变化: ${if (visible) "显示" else "隐藏"}")
        }
        TipsPopupHelper.dismiss()
        
        // 这里可以添加键盘显示/隐藏时的业务逻辑
        if (visible) {
            // 键盘显示时的处理逻辑
        } else {
            // 键盘隐藏时的处理逻辑
        }
    }
}