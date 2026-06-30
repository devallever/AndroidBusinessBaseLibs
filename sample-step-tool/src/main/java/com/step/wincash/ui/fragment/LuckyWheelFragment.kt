package com.step.wincash.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.core.app.App
import com.step.wincash.R
import com.step.wincash.base.BaseFragment
import com.step.wincash.beans.CurrencyType
import com.step.wincash.databinding.FragmentLuckyWheelBinding
import com.step.wincash.event.ChangeShowPage
import com.step.wincash.init.AdIndex
import com.step.wincash.ui.dialog.AddChancesDialog
import com.step.wincash.ui.dialog.DoubleAwareDialog
import com.step.wincash.ui.dialog.OverlayAwareDialog
import com.step.wincash.ui.widget.BannerNativeView
import com.step.wincash.utils.ActivityType
import com.step.wincash.utils.GoldFlyAnimatorUtil
import com.step.wincash.utils.LogUtil
import com.step.wincash.utils.PopupHelper
import com.step.wincash.utils.SpKey
import com.step.wincash.utils.SpUtil
import com.step.wincash.utils.setOnSingleListener
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class LuckyWheelFragment : BaseFragment<FragmentLuckyWheelBinding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLuckyWheelBinding {
        return FragmentLuckyWheelBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.luckyWheelView.apply {

            fixStatusBar(binding.topBarFl)
            setOnResultListener { award -> //抽奖结果回调
                val showDouble = Random.nextBoolean()
                if (showDouble) { //显示翻倍dialog
                    PopupHelper.createDialog(
                        requireContext(),
                        DoubleAwareDialog(
                            requireActivity(),
                            award,
                            AdIndex.LUCKY_WHEEL_DOUBLE_AWARD_INDEX,
                            AdIndex.LUCKY_WHEEL_DOUBLE_INTER_INDEX,
                            ActivityType.LUCKY_WHEEL,
                        ) { award ->
                            GoldFlyAnimatorUtil.start(
                                binding.currencyView.context, binding.currencyView, binding.root,
                                CurrencyType.GREEN, award.toFloat()
                            )
                            SpUtil.put(SpKey.LAST_TIME_SPIN, System.currentTimeMillis())
                        }
                    ).show()
                } else {
                    showAwardView(award)
                }
                if (App.DEBUG){
                    LogUtil.local("抽奖结果奖励：$award 是否翻倍：$showDouble")
                }
            }
            setNoChancesListener {  //无次数回调
                showAddChancesDialog()
            }
        }

        binding.backImg.setOnSingleListener {
            EventBus.getDefault().post(ChangeShowPage())
        }
        initBanner()
    }

    fun initBanner() {
        binding.bannerView.initView(
            BannerNativeView.ViewType.BANNER,
            requireActivity()
        )
    }
    fun showAwardView(aware: Int) {
        //显示奖励蒙层和飘分动画
        PopupHelper.createDialog(
            requireContext(),
            OverlayAwareDialog(requireContext(), R.drawable.ic_aware_green, aware) {
                GoldFlyAnimatorUtil.start(
                    binding.currencyView.context, binding.currencyView, binding.root,
                    CurrencyType.GREEN, aware.toFloat()
                )
                SpUtil.put(SpKey.LAST_TIME_SPIN, System.currentTimeMillis())
            }).show()
    }

    fun showAddChancesDialog() {
        PopupHelper.createDialog(
            requireContext(),
            AddChancesDialog(
                requireContext(),
                AdIndex.LUCKY_WHEEL_ADD_CHANCES_INDEX,
                {
                    binding.luckyWheelView.setChances(3)
                }, {
                    EventBus.getDefault().post(ChangeShowPage())  //回到首页
                })
        ).show()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            initBanner()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}