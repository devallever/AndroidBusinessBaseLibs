package com.step.wincash.ui.fragment

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.app.App
import com.carefree.steplib.common.StepConstants
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.utils.Mkv
import com.carefree.steplib.utils.StepTracker
import com.jeremyliao.liveeventbus.LiveEventBus
import com.step.wincash.R
import com.step.wincash.base.BaseApplication
import com.step.wincash.base.BaseFragment
import com.step.wincash.beans.CurrencyType
import com.step.wincash.beans.ExtraKey
import com.step.wincash.business.step.StepBusiness
import com.step.wincash.business.withdraw.WalletManager
import com.step.wincash.business.withdraw.WithdrawBusiness
import com.step.wincash.databinding.FragmentHomeBinding
import com.step.wincash.event.AdDismissEvent
import com.step.wincash.event.ChangeShowPage
import com.step.wincash.event.TabLayoutShowEvent
import com.step.wincash.init.AdIndex
import com.step.wincash.init.InitManager
import com.step.wincash.ui.activity.STWithdrawActivity
import com.step.wincash.ui.dialog.DoubleAwareDialog
import com.step.wincash.ui.dialog.HomeAwareDialog
import com.step.wincash.ui.dialog.SettingsDialog
import com.step.wincash.ui.dialog.StepGoalDialog
import com.step.wincash.ui.dialog.guide.Guide1Dialog
import com.step.wincash.ui.dialog.guide.Guide2Dialog
import com.step.wincash.utils.ActivityType
import com.step.wincash.utils.CurrencyUtils
import com.step.wincash.utils.GoldFlyAnimatorUtil
import com.step.wincash.utils.InterAdUtil
import com.step.wincash.utils.LogUtil
import com.step.wincash.utils.PermissionUtil
import com.step.wincash.utils.PopupHelper
import com.step.wincash.utils.SpKey
import com.step.wincash.utils.SpUtil
import com.step.wincash.utils.TimeUtil
import com.step.wincash.utils.dp2px
import com.step.wincash.utils.gone
import com.step.wincash.utils.log
import com.step.wincash.utils.setOnSingleListener
import com.step.wincash.utils.showXPopup
import com.step.wincash.utils.slideAcrossScreen
import com.step.wincash.utils.visible
import com.tencent.qgame.animplayer.util.ScaleType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.random.Random
import kotlin.random.nextInt


class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val animSet = AnimatorSet()
    private var settingDialog: SettingsDialog? = null
    private var mClickActivityType = ""//记录点击的活动类型
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    val interactIconList = listOf<Int>(
        R.drawable.ic_interact1,
        R.drawable.ic_interact2,
        R.drawable.ic_interact3,
        R.drawable.ic_interact4,
    )

    private var index1 = 0
    private var index2 = 1

    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        onInactivity()
    }

    private var mDanmuStarted = false

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerEventbus()
        fixStatusBar(binding.frameLayout)
        initObserver()
        initView()
        logFirstShowMainEvent()
        binding.apply {
            btnRequestPermission.setOnClickListener {
                requestPermission()
            }
            btnSetGoal.setOnSingleListener {
                activity?.apply {
                    showXPopup(StepGoalDialog(this, StepBusiness.getStepGoal()) {
                        StepBusiness.updateStepGoal(it)
                        updateGoalStep()
                    })
                }
            }
            ivSetting.setOnSingleListener { it ->
                settingDialog = SettingsDialog(it.context, onDismissCallback = {
                    settingDialog = null
                    root.postDelayed({
                        if (InterAdUtil.showAd()) {
                            BaseApplication.postAdDismissEvent(AdIndex.ADMOB_INTER_INDEX)
                        }
                    }, 500)
                })

                PopupHelper.createDialog(it.context, settingDialog).show()
            }
            goToLuckyWheelImg.setOnSingleListener {
                mClickActivityType = ActivityType.LUCKY_WHEEL
                logClickEvent(7)
                EventBus.getDefault().post(ChangeShowPage(1))
            }
            goToScratchImg.setOnSingleListener {
                mClickActivityType = ActivityType.SCRATCH_CARD
                logClickEvent(8)
                EventBus.getDefault().post(ChangeShowPage(0))
            }
            interactAdImg1.setOnSingleListener {
                index1 = nextIndex(index1, index2)
                updateImages()
                logClickEvent(5)
                EventBus.getDefault().post(ChangeShowPage(3))
            }
            interactAdImg2.setOnSingleListener {
                index2 = nextIndex(index2, index1)
                updateImages()
                logClickEvent(6)
                EventBus.getDefault().post(ChangeShowPage(4))
            }
            root.setOnTouchListener { _, _ ->
                resetTimer()
                binding.fingerView.gone()
                false
            }
        }
        startFloatAnimation(
            listOf(
                binding.goldBubbleImg1,
                binding.goldBubbleImg2,
                binding.greenBubbleImg1,
                binding.greenBubbleImg2,
                binding.greenBubbleImg3
            )
        )
        addPulseShrinkEffect(
            listOf(binding.interactAdImg1, binding.interactAdImg2)
        )

        if (SpUtil.get(SpKey.GUIDE, true)) {
            showGuide1Dialog()
        }
        initVideo()
    }

    fun initVideo() {
        binding.stepVideo.apply {
            setScaleType(ScaleType.FIT_XY)
            setLoop(Int.MAX_VALUE)
            startPlay(requireContext().assets, "panda_step.mp4")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun launchDanmu() {
        if (SpUtil.get(SpKey.GUIDE, true)) {
            return
        }
        if (mDanmuStarted) {
            return
        }
        lifecycleScope.launch {
            while (isActive) {
                mDanmuStarted = true
                binding.tvId.text = "ID:${WithdrawBusiness.generateRandomUserId()}"
                binding.tvDanmuTime.text = TimeUtil.formatTimeYYYY_MM_dd(System.currentTimeMillis())
                val amount = if (System.currentTimeMillis() % 2 == 0L) {
                    50
                } else {
                    100
                }
                binding.tvDanmuMessage.text = getString(R.string.danmu_message, amount)
                binding.ivPayment.setImageResource(
                    WalletManager.getPaymentParamsList(InitManager.getCountryCode())
                        .random().paymentIcon
                )

                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    binding.danmu.slideAcrossScreen(
                        100..800,
                        5000L
                    )
                    if (App.DEBUG) {
                        log("danmu show")
                    }
                }
                val nextTime = Random.nextInt(1 * 60 * 1000..5 * 60 * 1000).toLong()
//                val nextTime = if (App.DEBUG) {
//                    Random.nextInt(10 * 1000..15 * 1000).toLong()
//                } else {
//                    Random.nextInt(1 * 60 * 1000..5 * 60 * 1000).toLong()
//                }
                if (App.DEBUG) {
                    log("nextTime: $nextTime")
                }
                delay(nextTime)
            }
        }
    }

    /** 返回下一个不与另一方重复的索引 */
    private fun nextIndex(current: Int, other: Int): Int {
        var next = (current + 1) % interactIconList.size
        if (next == other) {
            next = (next + 1) % interactIconList.size
        }
        return next
    }

    /** 更新两个 ImageView 的图片 */
    private fun updateImages() {
        binding.interactAdImg1.setImageResource(interactIconList[index1])
        binding.interactAdImg2.setImageResource(interactIconList[index2])
    }

    override fun onResume() {
        super.onResume()

        binding.btnRequestPermission.visibility =
            if (StepBusiness.hasRequirePermission(requireActivity()))
                View.INVISIBLE
            else View.VISIBLE

        resetTimer()
        launchDanmu()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeoutRunnable)
    }

    private fun initView() {
        updateTodayStep(Mkv.getInt(StepConstants.KEY_CURRENT_STEP))
        updateGoalStep()

        iconAdViewMap.forEach { (view, index) ->
            view.setOnSingleListener {
                var click = 0
                binding.apply {
                    click = when (view) {
                        binding.goldBubbleImg1, binding.goldBubbleImg2 -> 1
                        binding.greenBubbleImg1, binding.greenBubbleImg2, binding.greenBubbleImg3 -> 2
                        binding.getCoinsLL -> 3
                        else -> 0
                    }
                }
                logClickEvent(click)
                BaseApplication.postAdDismissEvent( index)
            }
        }
        binding.currencyView.setOnCoinClickCallback {
            val clickId = if (it == CurrencyType.GOLD.type) {
                1//金币
            } else {
                2//绿钞
            }
            logClickEvent(clickId)
            goTo<STWithdrawActivity>(requireActivity()) {
                putExtra(ExtraKey.CURRENCY_TYPE, it)
            }
        }
    }

    private fun initObserver() {
        LiveEventBus.get<Int>(ConstStep.STEP_EVENT).observe(this) { step ->
            // 更新今天步数
            updateTodayStep(step)
        }
    }

    private fun showGuide1Dialog() {
        logGuideEvent(1)
        binding.root.postDelayed(
            {
                PopupHelper.createDialog(
                    requireContext(),
                    Guide1Dialog(requireContext(), binding.imageView16) {
                        logGuideEvent(2)
                        //显示奖励蒙层和飘分动画
                        PopupHelper.createDialog(
                            requireContext(),
                            HomeAwareDialog(requireActivity(), 1500, CurrencyType.GOLD, true) {
                                GoldFlyAnimatorUtil.start(
                                    binding.currencyView.context,
                                    binding.currencyView,
                                    binding.root,
                                    CurrencyType.GOLD,
                                    1500.toFloat()
                                ) {
                                    logGuideEvent(3)
                                    showGuide2Dialog()
                                }
                            }).show()
                    }).show()
            }, 200
        )
    }

    private fun showGuide2Dialog() {
        PopupHelper.createDialog(
            requireContext(),
            Guide2Dialog(requireContext(), binding.currencyView.getBinding().flGold) {
                goTo<STWithdrawActivity>(requireActivity()) {
                    putExtra(ExtraKey.CURRENCY_TYPE, CurrencyType.GOLD.type)
                }
            }).show()
    }

    private fun logGuideEvent(num: Int) {
    }


    private fun logFirstShowMainEvent() {
        val isFirst = SpUtil.get(SpKey.IS_FIRST_DISPLAY_MAIN, true)
        if (isFirst) {
            SpUtil.put(SpKey.IS_FIRST_DISPLAY_MAIN, false)
        }

    }

    private fun logClickEvent(clickId: Int) {
    }

    private fun updateTodayStep(step: Int) {
        lifecycleScope.launch {
            binding.tvStepCount.text = step.toString()
            binding.apply {
                tvDistance.text = StepBusiness.stepsToKilometers(step)
                tvKcal.text = StepBusiness.stepsToCalories(step)
                tvTime.text = StepBusiness.stepsToMinutes(step)
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateGoalStep() {
        lifecycleScope.launch {
            binding.tvStepGoal.text =
                getString(R.string.target_step_count, StepBusiness.getStepGoal().toString())
        }
    }

    private fun requestPermission() {
        if (PermissionUtil.areAllPermissionsPermanentlyDenied(
                requireActivity(),
                StepBusiness.getRequirePermission(requireActivity()).toTypedArray()
            )
        ) {
            PermissionUtil.openAppSettings(requireActivity())
        }
        if (!StepBusiness.hasRequirePermission(requireActivity())) {
            StepBusiness.requestPermission(requireActivity())
        } else {
            StepTracker.startTrackingService()
        }
    }

    private fun startFloatAnimation(views: List<View>) {
        val dp5 = dp2px(5f).toFloat()
        views.forEach { view ->
            val animator = if (Random.nextBoolean()) {
                ValueAnimator.ofFloat(0f, -dp5, 0f, dp5)
            } else {
                ValueAnimator.ofFloat(0f, dp5, 0f, -dp5)
            }
            animator.duration = 3000
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener { animation: ValueAnimator ->
                val value = animation.animatedValue as Float
                view.translationY = value
            }
            animator.startDelay = (100..800).random().toLong()
            animSet.playTogether(animator)
        }
        if (animSet.childAnimations.size == views.size) {
            animSet.start()
        }
    }

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

    val iconAdViewMap by lazy {
        mapOf(
            binding.goldBubbleImg1 to AdIndex.HOME_GOLD_BUBBLE1_INDEX,
            binding.goldBubbleImg2 to AdIndex.HOME_GOLD_BUBBLE2_INDEX,
            binding.greenBubbleImg1 to AdIndex.HOME_GREEN_BUBBLE1_INDEX,
            binding.greenBubbleImg2 to AdIndex.HOME_GREEN_BUBBLE2_INDEX,
            binding.greenBubbleImg3 to AdIndex.HOME_GREEN_BUBBLE3_INDEX,
            binding.getCoinsLL to AdIndex.HOME_GOLD_BUTTON_INDEX,
        )
    }

    fun showAwardView(aware: Int, view: View) {
        //显示奖励蒙层和飘分动画
        PopupHelper.createDialog(
            requireContext(),
            HomeAwareDialog(requireActivity(), aware) {
                GoldFlyAnimatorUtil.start(
                    binding.currencyView.context, binding.currencyView, binding.root,
                    CurrencyType.GREEN, aware.toFloat()
                )
                updateViewState(view)
            }).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdDismissEvent(event: AdDismissEvent) {
        if (event.adIndex in iconAdViewMap.values) {
            val currencyType = if (event.adIndex == AdIndex.HOME_GOLD_BUTTON_INDEX
                || event.adIndex == AdIndex.HOME_GOLD_BUBBLE1_INDEX
                || event.adIndex == AdIndex.HOME_GOLD_BUBBLE2_INDEX
            )
                CurrencyType.GOLD
            else CurrencyType.GREEN
            val view = iconAdViewMap.entries.first { it.value == event.adIndex }.key
            if (currencyType == CurrencyType.GOLD) {
                GoldFlyAnimatorUtil.start(
                    binding.currencyView.context, binding.currencyView, binding.root,
                    CurrencyType.GOLD, CurrencyUtils.computeGoldNum()
                )
                if (event.adIndex != AdIndex.HOME_GOLD_BUTTON_INDEX) updateViewState(view)
            } else {
                val aware = (750..800).random()

                val showDouble = Random.nextBoolean()
                if (showDouble) { //显示翻倍dialog
                    PopupHelper.createDialog(
                        requireContext(),
                        DoubleAwareDialog(
                            requireActivity(),
                            aware,
                            AdIndex.HOME_DOUBLE_AWARD_INDEX,
                            AdIndex.HOME_DOUBLE_INTER_INDEX,
                            mClickActivityType
                        ) { award ->
                            //结果回调
                            GoldFlyAnimatorUtil.start(
                                binding.currencyView.context, binding.currencyView, binding.root,
                                CurrencyType.GREEN, (aware * 5).toFloat()
                            )
                            updateViewState(view)
                        }
                    ).show()
                } else {
                    showAwardView(aware, view)
                }
                if (App.DEBUG) {
                    LogUtil.local("绿钞广告关闭奖励，随机奖励：$aware 是否翻倍：$showDouble")
                }
            }
        }
    }

    fun updateViewState(view: View) {
        view.gone()
        view.postDelayed({
            view.visible()
        }, 3000L)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            initVideo()
            EventBus.getDefault().post(TabLayoutShowEvent(true))
        }
    }

    private fun resetTimer() {
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, 5000) // 5秒后触发
    }

    private fun onInactivity() {
        binding.fingerView.visible()
    }

}