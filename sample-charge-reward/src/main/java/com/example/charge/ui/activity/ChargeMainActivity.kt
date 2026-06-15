package com.example.charge.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import app.allever.android.lib.core.app.App
import com.example.charge.ChargeApp
import com.example.charge.R
import com.example.charge.ad.AdIndex
import com.example.charge.base.BaseActivity
import com.example.charge.constant.FloatIconType
import com.example.charge.constant.LogTag
import com.example.charge.currency.CurrencyFlyAnimatorUtil
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.data.FloatIconData
import com.example.charge.databinding.ActivityChargeMainBinding
import com.example.charge.databinding.ActivityMainBinding
import com.example.charge.event.InterAdCDTimeEvent
import com.example.charge.event.UpdateCurrencyEvent
import com.example.charge.init.Constance
import com.example.charge.task.TaskHelper
import com.example.charge.ui.dialog.AdFailDialog
import com.example.charge.ui.dialog.InfiniteGameDialog
import com.example.charge.ui.dialog.NewUserDialog
import com.example.charge.ui.dialog.SettingsDialog
import com.example.charge.utils.AdvancedTimer
import com.example.charge.utils.ChargeStatusListener
import com.example.charge.utils.FloatIconHelper
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import com.example.charge.utils.SpeedUpHelper
import com.example.charge.utils.formatFloat
import com.example.charge.utils.gone
import com.example.charge.utils.log
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.showXPopup
import com.example.charge.utils.sp2px
import com.example.charge.utils.toast
import com.example.charge.utils.visible
import com.example.charge.vm.VMHelper
import com.hjq.shape.view.ShapeTextView
import com.plinkopro.wincash.utils.PopupHelper
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdDismissEvent
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ChargeMainActivity : BaseActivity<ActivityChargeMainBinding>() {

    private val mFloatIconHelper by lazy {
        FloatIconHelper()
    }

    private val mSpeedUpHelper by lazy {
        SpeedUpHelper()
    }

    // 充电状态监听器
    private lateinit var chargeStatusListener: ChargeStatusListener

    private var clickFloatView: View? = null

    private val normalCreateValueTimer by lazy {
        AdvancedTimer()
    }

    private var exitTimeMill: Long = 0L

    // 当前正在执行的动画对象
    private var currentNotifyAnimator: ObjectAnimator? = null

    // 当前正在执行的延迟消失任务
    private var dismissRunnable: Runnable? = null

    private val hitMoleCountView by lazy {
        listOf(binding.hitMoleSize1Img, binding.hitMoleSize2Img, binding.hitMoleSize3Img)
    }
    private val receiveCoinCountView by lazy {
        listOf(
            binding.receiveCoinSize1Img,
            binding.receiveCoinSize2Img,
            binding.receiveCoinSize3Img
        )
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityChargeMainBinding {
        return ActivityChargeMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        fixStatusBar(binding.currencyView)
        logFirstShowMainEvent()
        // 初始化充电状态监听器
        initChargeStatusListener()
        initListener()
        initFloatIcon()
        initValueCreate()
        initObserver()
        if (App.DEBUG) {
            binding.chargeProgressBg.setOnLongClickListener {
                goTo<FunctionActivity>(this)
                true
            }
        }
        ChargeApp.minuteTimer.start()
        TaskHelper.checkShowDot()
        //新人福利
        newUserBenefits()
        TaskHelper.signIn {}

        addPulseShrinkEffect(
            listOf(binding.ivInteractiveLeft, binding.ivInteractiveRight, binding.ivChargeState)
        )

        updateSpeedupBtnUI()

        ChargeApp.interAdTimer.reset().start()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SpUtil.put(SpKey.APP_QUIT_TIME, System.currentTimeMillis())
        chargeStatusListener.stopListening()
        mFloatIconHelper.destroy()
        normalCreateValueTimer.release()
        mSpeedUpHelper.release()
        ChargeApp.minuteTimer.stop()
    }

    private fun updateSpeedupBtnUI() {
        val firstClick = SpUtil.get(SpKey.FIRST_CLICK_SPEED_UP, true)
        binding.ivSpeedUpAd.isVisible = !firstClick
    }

    private fun newUserBenefits() {
        if (!SpUtil.get(SpKey.CLAIM_NEW_USER_BENEFITS, false)) {
            showXPopup(NewUserDialog(this) {
                binding.tipsTv.apply {
                    visible()
                    setOnSingleListener {
                        gone()
                        cancelCurrentAnimation()
                    }
                    addPulseShrinkEffect(listOf(binding.tipsTv))
                    postDelayed({
                        gone()
                    },5000)
                }
                SdkManager.dot("new_user_bonus")
                CurrencyFlyAnimatorUtil.start(
                    this,
                    binding.currencyView,
                    binding.root,
                    CurrencyType.GREEN,
                    it.toFloat(),
                ) {
                    SpUtil.put(SpKey.CLAIM_NEW_USER_BENEFITS, true)
                }
            })
        }
    }

    private fun initValueCreate() {
        mSpeedUpHelper.btnSpeedCountdownListener = object : SpeedUpHelper.CountdownListener {
            override fun onFinish() {
                updateSpeedUpButtonUi()
                binding.progressBarSpeedup.isVisible = false
                binding.tvSpeedUpTime.isVisible = false
                updateSpeedUpTextStyle(false, binding.tvSpeedGreen)
                updateSpeedUpTextStyle(false, binding.tvSpeedGold)
                updateSpeedupBtnUI()
            }

            @SuppressLint("SetTextI18n")
            override fun onProgressUpdate(progress: Int, seconds: Int) {
                binding.progressBarSpeedup.isVisible = true
                binding.progressBarSpeedup.setProgress(progress)
                binding.tvSpeedUpTime.isVisible = true
                binding.tvSpeedUpTime.text = "${seconds}s"
                updateSpeedUpTextStyle(true, binding.tvSpeedGreen)
                updateSpeedUpTextStyle(true, binding.tvSpeedGold)
            }
        }
        mSpeedUpHelper.floatIconCountdownListener = object : SpeedUpHelper.CountdownListener {
            override fun onFinish() {
                binding.progressBarGold.isVisible = false
            }

            override fun onProgressUpdate(progress: Int, seconds: Int) {
                binding.progressBarGold.isVisible = true
                binding.progressBarGold.setProgress(progress)
            }
        }
        normalCreateValueTimer.setListener(object : AdvancedTimer.OnTimerTickListener {
            override fun onSecondTick(seconds: Long) {
                if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                    return
                }
                var updated = false
                // 生产金币
                if (!mSpeedUpHelper.isCountingDown()) {
                    CurrencyUtils.updateCurrencyNum(
                        CurrencyType.GOLD,
                        mSpeedUpHelper.goldCreateValue
                    )
                    binding.floatUpAnimLayoutGold.playFloat(
                        mSpeedUpHelper.goldCreateValue,
                        CurrencyType.GOLD
                    )
                    updated = true
                    EventBus.getDefault().post(UpdateCurrencyEvent())
                }
                // 生产绿钞
                if (mSpeedUpHelper.greenCreateValue != 0f) {
                    CurrencyUtils.updateCurrencyNum(
                        CurrencyType.GREEN,
                        mSpeedUpHelper.greenCreateValue
                    )
                    binding.floatUpAnimLayoutGreen.playFloat(
                        mSpeedUpHelper.greenCreateValue,
                        CurrencyType.GREEN
                    )
                    updated = true
                    EventBus.getDefault().post(UpdateCurrencyEvent(CurrencyType.GREEN))
                }
                if (updated) {
                    updateCreateValueUi()
                }
            }

            override fun onIntervalTick(milliseconds: Long, progress: Int) {
                if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                    return
                }
                if (mSpeedUpHelper.isCountingDown()) {
                    CurrencyUtils.updateCurrencyNum(
                        CurrencyType.GOLD,
                        mSpeedUpHelper.goldCreateValue
                    )
                    binding.currencyView.refreshData()
                    updateCreateValueUi()
                    binding.floatUpAnimLayoutGold.playFloat(
                        mSpeedUpHelper.goldCreateValue,
                        CurrencyType.GOLD
                    )
                }
            }
        }).setInterval(500).start()
    }

    private fun initListener() {
        binding.apply {
            ivInteractiveLeft.setOnSingleListener {
                logClickEvent(6)
                WebActivity.start(this@ChargeMainActivity, Constance.OKSPIN_URL)
            }
            ivInteractiveRight.setOnSingleListener {
                logClickEvent(7)
                WebActivity.start(this@ChargeMainActivity, Constance.OKSPIN_URL)
            }
            gameLeft.setOnSingleListener {
                logClickEvent(11)
                if (VMHelper.hitMoleViewModel.gameCount.value == 0 && VMHelper.hitMoleViewModel.isInfiniteTime.value == false) {
                    showInfiniteTimeDialog(AdIndex.MOLE_GAME_INFINITE_INDEX) {
                        VMHelper.hitMoleViewModel.residueInfiniteTime.value =
                            VMHelper.hitMoleViewModel.onceInfiniteTime
                    }
                } else {
                    goTo<HitMoleActivity>(this@ChargeMainActivity)
                }
            }
            gameRight.setOnSingleListener {
                logClickEvent(12)
                if (VMHelper.receiveCoinViewModel.gameCount.value == 0 && VMHelper.receiveCoinViewModel.isInfiniteTime.value == false) {
                    showInfiniteTimeDialog(AdIndex.COIN_GAME_INFINITE_INDEX) {
                        VMHelper.receiveCoinViewModel.residueInfiniteTime.value =
                            VMHelper.receiveCoinViewModel.onceInfiniteTime
                    }
                } else {
                    goTo<ReceiveCoinActivity>(this@ChargeMainActivity)
                }
            }
            //设置
            ivSetting.setOnSingleListener {
                PopupHelper.createDialog(it.context, SettingsDialog(this@ChargeMainActivity)).show()
            }
            //提现
            btnWithdraw.setOnSingleListener {
                logClickEvent(9)
                goTo<WithdrawActivity>(this@ChargeMainActivity)
            }
            //任务
            btnTask.setOnSingleListener {
                logClickEvent(10)
                goTo<TaskActivity>(this@ChargeMainActivity)
            }
            //加速
            btnSpeedup.setOnSingleListener {
                logClickEvent(8)
                val firstClick = SpUtil.get(SpKey.FIRST_CLICK_SPEED_UP, true)
                if (firstClick) {
                    SpUtil.put(SpKey.FIRST_CLICK_SPEED_UP, false)
                    mSpeedUpHelper.speedUpBtn()
                    updateSpeedUpButtonUi()
                } else {
                    AdManager.showRewardAd(this@ChargeMainActivity, AdIndex.HOME_SPEED_UP)
                }
            }
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val nowTimeMill = System.currentTimeMillis()
                if (nowTimeMill - exitTimeMill > 2000) {
                    toast(getString(R.string.confirm_exit_app_tips))
                    exitTimeMill = nowTimeMill
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }

        })

    }

    private fun initFloatIcon() {
        mFloatIconHelper.startFloatAnimation(
            listOf(
                binding.floatAnimContainer1,
                binding.floatAnimContainer2,
                binding.floatAnimContainer3,
                binding.floatAnimContainer4
            ), binding
        ) { target ->
            clickFloatView = target
            val floatIconData = target.tag as FloatIconData
            when (floatIconData.type) {
                FloatIconType.GOLD -> {
                    logClickEvent(3)
                    TaskHelper.addCollectCount()
                    CurrencyFlyAnimatorUtil.start(
                        this,
                        binding.currencyView,
                        binding.root,
                        CurrencyType.GOLD,
                        floatIconData.value
                    ) {
                        inAppNotify()
                    }
                }

                FloatIconType.GREEN -> {
                    logClickEvent(1)
                    TaskHelper.addCollectCount()
                    CurrencyFlyAnimatorUtil.start(
                        this,
                        binding.currencyView,
                        binding.root,
                        CurrencyType.GREEN,
                        floatIconData.value
                    ) {
                        inAppNotify()
                    }
                }

                FloatIconType.GREEN_AD -> {
                    logClickEvent(2)
                    clickFloatView?.isVisible = true
                    AdManager.showRewardAd(this, AdIndex.HOME_FLOAT_ICON)
                }

                FloatIconType.SPEED -> {
                    logClickEvent(5)
                    TaskHelper.addCollectCount()
                    inAppNotify()
                    mSpeedUpHelper.speedUpFloatIcon()
//                    FloatIconHelper.startCircleProgressBarAnim(target)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun initObserver() {
        VMHelper.hitMoleViewModel.apply {
            gameCount.observe(this@ChargeMainActivity) {
                if (isInfiniteTime.value == true) {
                    binding.llGameWhackMoleTimes.gone()
                    binding.tvGameCountLeft.gone()
                    binding.ivInfiniteLeft.visible()
                } else {
                    binding.ivInfiniteLeft.gone()
                    if (it == 0) {
                        binding.llGameWhackMoleTimes.gone()
                        binding.tvGameCountLeft.visible()
                    } else {
                        binding.llGameWhackMoleTimes.visible()
                        binding.tvGameCountLeft.gone()
                    }
                    hitMoleCountView.forEachIndexed { index, view ->
                        if (index + 1 <= it) view.visible()
                        else view.gone()
                    }
                }
            }
            countDownTimer.observe(this@ChargeMainActivity) {
                binding.ivInfiniteLeft.isVisible = isInfiniteTime.value?: false
                if (isInfiniteTime.value == false) {
                    binding.tvGameCountLeft.text = "${(it / 1000).toInt()}s"
                }
            }
            isInfiniteTime.observe(this@ChargeMainActivity) {
                gameCount.value = gameCount.value  //触发游戏次数的监听器，在那个里面有界面刷新
            }
        }

        VMHelper.receiveCoinViewModel.apply {
            gameCount.observe(this@ChargeMainActivity) {
                if (isInfiniteTime.value == true) {
                    binding.llGameReceiveCoinTimes.gone()
                    binding.tvGameCountRight.gone()
                    binding.ivInfiniteRight.visible()
                } else {
                    binding.ivInfiniteRight.gone()
                    if (it == 0) {
                        binding.llGameReceiveCoinTimes.gone()
                        binding.tvGameCountRight.visible()
                    } else {
                        binding.llGameReceiveCoinTimes.visible()
                        binding.tvGameCountRight.gone()
                    }
                    receiveCoinCountView.forEachIndexed { index, view ->
                        if (index + 1 <= it) view.visible()
                        else view.gone()
                    }
                }
            }
            countDownTimer.observe(this@ChargeMainActivity) {
                binding.ivInfiniteRight.isVisible = isInfiniteTime.value?: false
                if (isInfiniteTime.value == false) {
                    binding.tvGameCountRight.text = "${(it / 1000).toInt()}s"
                }
            }
            isInfiniteTime.observe(this@ChargeMainActivity) {
                gameCount.value = gameCount.value  //触发游戏次数的监听器，在那个里面有界面刷新
            }
        }

        VMHelper.taskViewModel.showTaskDot.observe(this) {
            binding.ivTaskDot.isVisible = it
        }


    }

    private fun updateSpeedUpTextStyle(speedUp: Boolean, shapeTextView: ShapeTextView) {
        shapeTextView.textColorBuilder.apply {
            if (speedUp) {
                textColor = getColor(R.color.white)
                textStrokeSize = sp2px(2f)
                textStrokeColor = getColor(R.color.color_FF3706)
            } else {
                textColor = getColor(R.color.color_9CFF54)
                textStrokeSize = 0
            }
        }

    }

    fun showInfiniteTimeDialog(adIndex: Int, seeAdCallBack: () -> Unit) {
        PopupHelper.createDialog(
            this,
            InfiniteGameDialog(
                this,
                adIndex,
                {
                    it.dismiss()
                }, {
                    seeAdCallBack()
                }
            )
        ).show()
    }

    /**
     * 初始化充电状态监听器
     */
    private fun initChargeStatusListener() {
        chargeStatusListener = ChargeStatusListener(this)

        // 开始监听充电状态变化
        chargeStatusListener.startListening(object :
            ChargeStatusListener.OnChargeStatusChangeListener {
            override fun onChargeStatusChanged(
                isCharging: Boolean,
                chargeType: String,
                batteryLevel: Int,
                batteryTemperature: Float,
                batteryVoltage: Int
            ) {
                // 更新UI显示充电状态信息
                binding.ivChargeState.isVisible = isCharging
                binding.progressBarCharge.progress = batteryLevel
                mSpeedUpHelper.onChargeStatusChange(isCharging)

                if (App.DEBUG) {
                    // 打印日志
                    log(
                        "ChargeDemo",
                        "充电状态变化 - 充电中:$isCharging, 类型:$chargeType, 电量:$batteryLevel%, " +
                                "温度:${batteryTemperature}°C, 电压:${batteryVoltage}mV"
                    )
                }

            }
        })

        // 立即获取并显示当前电池状态
        val currentStatus = chargeStatusListener.getCurrentBatteryStatus()
    }

    private fun updateSpeedUpButtonUi() {
        val isCountingDown = mSpeedUpHelper.isCountingDown()
        binding.apply {
            tvAppendTimeContainer.isVisible = isCountingDown
            tvSpeedUpContainer.isVisible = !isCountingDown
            if (isCountingDown) {
                btnSpeedup.setImageResource(R.drawable.ic_btn_speed_up_ing)
            } else {
                btnSpeedup.setImageResource(R.drawable.ic_btn_speed_up_normal)
            }
        }

    }
    private fun logClickEvent(clickId: Int) {
        SdkManager.dot("main_click", mapOf("click_ID" to clickId))
    }

    private fun logFirstShowMainEvent() {
        val isFirst = SpUtil.get(SpKey.IS_FIRST_DISPLAY_MAIN, true)
        if (isFirst) {
            SpUtil.put(SpKey.IS_FIRST_DISPLAY_MAIN, false)
        }

        SdkManager.dot("app_main_show", mapOf("is_first" to if (isFirst) 1 else 0))
    }

    @SuppressLint("SetTextI18n")
    private fun updateCreateValueUi() {
        binding.apply {
            //gold
            tvSpeedGold.text =
                "${mSpeedUpHelper.goldCreateValue.toInt()}/${mSpeedUpHelper.goldCreateTime.formatFloat()}s"
            //green
            tvSpeedGreen.text =
                "$${mSpeedUpHelper.greenCreateValue.formatFloat()}/${mSpeedUpHelper.greenCreateTime.formatFloat()}s"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun inAppNotify() {
        val view = binding.cardNotifyContainer

        // 取消上一次的动画和延迟消失任务
        cancelCurrentAnimation()

        // 创建新的消失任务
        dismissRunnable = Runnable {
            // 淡出动画
            val fadeOutAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            fadeOutAnimator.duration = 500
            fadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.isVisible = false
                    view.translationY = -view.measuredHeight.toFloat() // 重置位置
                    // 清空引用，避免内存泄漏
                    currentNotifyAnimator = null
                    dismissRunnable = null
                }
            })
            fadeOutAnimator.start()
        }

        clickFloatView?.let {
            val data = it.tag as FloatIconData
            val valueString = when (data.type) {
                FloatIconType.GOLD -> getString(R.string.gold)
                FloatIconType.GREEN, FloatIconType.GREEN_AD -> "$${data.value}"

                FloatIconType.SPEED -> getString(R.string.speed_up)
                else -> ""
            }
            binding.tvNotifyFloatIcon.text = "$valueString"

            // 确保控件已测量
            if (view.measuredHeight == 0) {
                // 强制测量控件
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(binding.root.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            // 重置控件状态，准备新动画
            view.alpha = 1f
            // 初始化位置：屏幕顶部外
            view.translationY = -view.measuredHeight.toFloat()
            view.isVisible = true

            // 下移动画：从顶部向下滑入
            val translateYAnimator =
                ObjectAnimator.ofFloat(view, "translationY", -view.measuredHeight.toFloat(), 0f)
            translateYAnimator.duration = 500 // 缩短动画时间，使效果更流畅
            translateYAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // 显示一段时间后消失
                    dismissRunnable?.let { runnable ->
                        view.postDelayed(runnable, 2000L)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    // 动画被取消时清空引用
                    currentNotifyAnimator = null
                }
            })

            // 保存当前动画引用
            currentNotifyAnimator = translateYAnimator
            translateYAnimator.start()
        }
    }

    /**
     * 取消当前正在执行的动画和延迟消失任务
     */
    private fun cancelCurrentAnimation() {
        val view = binding.cardNotifyContainer

        // 取消当前动画
        currentNotifyAnimator?.cancel()
        currentNotifyAnimator = null

        // 移除延迟消失任务
        if (dismissRunnable != null) {
            view.removeCallbacks(dismissRunnable)
            dismissRunnable = null
        }
    }

    override fun enableEventBus() = true

    fun addPulseShrinkEffect(
        views: List<View>,
        minScale: Float = 0.9f,
        duration: Long = 1500L,
    ) {
        views.forEach { view ->
            val animator = ValueAnimator.ofFloat(1f, minScale, 1f).apply {
                this.duration = duration
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                addUpdateListener { animation ->
                    val scale = animation.animatedValue as Float
                    view.scaleX = scale
                    view.scaleY = scale
                }
            }
            animator.start()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdDismissEvent(event: AdDismissEvent) {
        if (event.adIndex == AdIndex.HOME_FLOAT_ICON) {
            clickFloatView?.let {
                it.isVisible = false
                val data = it.tag as FloatIconData
                TaskHelper.addCollectCount()
                CurrencyFlyAnimatorUtil.start(
                    this,
                    binding.currencyView,
                    binding.root,
                    CurrencyType.GREEN,
                    data.value
                ) {
                    inAppNotify()
                }
            }
        } else if (event.adIndex == AdIndex.HOME_SPEED_UP) {
            mSpeedUpHelper.speedUpBtn()
            updateSpeedUpButtonUi()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdShowFailedEvent(event: AdShowFailedEvent) {
        when (event.adIndex) {
            AdIndex.HOME_FLOAT_ICON, AdIndex.HOME_SPEED_UP -> {
                showXPopup(AdFailDialog(this){

                })
            }
         }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveInterAdCDTimeEvent(event: InterAdCDTimeEvent) {
        if (isShow) {
            if (App.DEBUG) {
                log(LogTag.INTER_AD_CD, "首页展示插屏")
            }
            if (AdManager.showInterAd(this, AdIndex.HOME_INTER_CD)) {
                if (App.DEBUG) {
                    log(LogTag.INTER_AD_CD, "首页展示插屏成功")
                }
            } else {
                if (App.DEBUG) {
                    log(LogTag.INTER_AD_CD, "首页展示插屏失败")
                }
            }
        }
    }
}
