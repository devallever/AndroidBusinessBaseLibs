package com.plinkopro.wincash.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseFragment
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.databinding.FragmentLuckyWheelBinding
import com.plinkopro.wincash.event.ChangeShowPage
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.ui.dialog.AddChancesDialog
import com.plinkopro.wincash.ui.dialog.DoubleAwareDialog
import com.plinkopro.wincash.ui.dialog.OverlayAwareDialog
import com.plinkopro.wincash.ui.widget.BannerNativeView
import com.plinkopro.wincash.utils.ActivityType
import com.plinkopro.wincash.utils.GoldFlyAnimatorUtil
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.utils.PopupHelper
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.setOnSingleListener
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
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
                if (BuildConfig.LOG_OUTPUT){
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
        AdManager.bannerPause();
    }

    override fun onResume() {
        super.onResume()
        AdManager.bannerResume();
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            initBanner()
            AdManager.bannerResume();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroyBanner();
    }
}