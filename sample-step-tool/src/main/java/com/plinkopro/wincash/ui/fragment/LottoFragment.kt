package com.plinkopro.wincash.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import app.allever.android.lib.core.app.App
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseFragment
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.beans.ScratchItem
import com.plinkopro.wincash.databinding.FragmentLottoBinding
import com.plinkopro.wincash.event.ChangeShowPage
import com.plinkopro.wincash.init.AdIndex
import com.plinkopro.wincash.ui.adapter.ScratchAdapter
import com.plinkopro.wincash.ui.dialog.AddChancesDialog
import com.plinkopro.wincash.ui.dialog.DoubleAwareDialog
import com.plinkopro.wincash.ui.dialog.OverlayAwareDialog
import com.plinkopro.wincash.ui.widget.BannerNativeView
import com.plinkopro.wincash.ui.widget.CrossItemScratchTouchListener
import com.plinkopro.wincash.ui.widget.SpaceItemDecoration
import com.plinkopro.wincash.utils.ActivityType
import com.plinkopro.wincash.utils.GoldFlyAnimatorUtil
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.utils.PopupHelper
import com.plinkopro.wincash.utils.SpKey
import com.plinkopro.wincash.utils.SpUtil
import com.plinkopro.wincash.utils.TimeUtil.isSameDay
import com.plinkopro.wincash.utils.formThousand
import com.plinkopro.wincash.utils.gone
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.setVisible
import com.plinkopro.wincash.utils.visible
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class LottoFragment : BaseFragment<FragmentLottoBinding>() {

    var chances: Int = 0

    lateinit var adapter: ScratchAdapter

    private var crossItemScratchListener: CrossItemScratchTouchListener? = null
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLottoBinding {
        return FragmentLottoBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fixStatusBar(binding.topBarFl)
        binding.rv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(SpaceItemDecoration(requireContext(), 3, 3, 3))
        }

        adapter = ScratchAdapter { award, multiple ->
            val aware = award * multiple
            val showDouble = Random.nextBoolean()
            if (showDouble) { //显示翻倍dialog
                PopupHelper.createDialog(
                    requireContext(),
                    DoubleAwareDialog(
                        requireActivity(),
                        aware,
                        AdIndex.SCRATCH_DOUBLE_AWARD_INDEX,
                        AdIndex.SCRATCH_DOUBLE_INTER_INDEX,
                        ActivityType.SCRATCH_CARD
                    ) { award ->
                        //结果回调
                        GoldFlyAnimatorUtil.start(
                            binding.currencyView.context, binding.currencyView, binding.root,
                            CurrencyType.GREEN, aware.toFloat()
                        )
                        adapter.setNewData(buildData() as MutableList<ScratchItem>?)
                        binding.apply {
                            upToFl.visible()
                            youWinFl.gone()
                        }
                    }
                ).show()
            } else {
                showAwardView(aware)
            }
            if (App.DEBUG){
                LogUtil.local("刮奖结果奖励：$award 是否翻倍：$showDouble")
            }
            binding.apply {
                awareTv.text = aware.formThousand()
                upToFl.gone()
                youWinFl.visible()
            }
            SpUtil.put(SpKey.LOTTO_CHANCES, chances-1)
            updateLottoChancesFL()
        }.apply {
            setNewData(buildData() as MutableList<ScratchItem>?)
        }

        binding.backImg.setOnSingleListener {
            EventBus.getDefault().post(ChangeShowPage())
        }

        binding.rv.adapter = adapter
        crossItemScratchListener = CrossItemScratchTouchListener(binding.rv)
        binding.rv.addOnItemTouchListener(crossItemScratchListener!!)

        updateLottoChancesFL()

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
                adapter.setNewData(buildData() as MutableList<ScratchItem>?)
                binding.apply {
                    upToFl.visible()
                    youWinFl.gone()
                }
            }).show()
    }
    fun showAddChancesDialog() {
        PopupHelper.createDialog(
            requireContext(),
            AddChancesDialog(
                requireContext(),
                AdIndex.SCRATCH_ADD_CHANCES_INDEX,
                {
                    SpUtil.put(SpKey.LOTTO_CHANCES, 3)
                    updateLottoChancesFL()
                }, {
                    EventBus.getDefault().post(ChangeShowPage())  //回到首页
                })
        ).show()
    }

    fun updateLottoChancesFL() {
        val lastTime = SpUtil.get(SpKey.LAST_LOTTO_TIME_SPIN, 0L)
        chances = if (isSameDay(lastTime)) {
            SpUtil.get(SpKey.LOTTO_CHANCES, 5)
        } else {
            SpUtil.put(SpKey.LAST_LOTTO_TIME_SPIN, System.currentTimeMillis())
            SpUtil.put(SpKey.LOTTO_CHANCES, 5)
            5
        }
        binding.tvChances.text = " $chances "
        binding.noLottoFL.apply {
            setVisible(chances <= 0)
            setOnSingleListener {
                showAddChancesDialog()
            }
        }
    }

    fun buildData(): List<ScratchItem> {

        val aware1 = (4..20).random() * 100
        val aware2 = (30..100).random() * 100
        val aware3 = (30..100).random() * 100
        val aware4 = (10..30).random() * 1000

        val aware5 = (3..9).random()
        val list = mutableListOf<ScratchItem>()
        list.apply {
            repeat(3) {
                add(ScratchItem(aware1, 0))
            }
        }
        list.addRepeat(2, ScratchItem(aware2, if (aware2 > aware3) 1 else 2))
        list.addRepeat(2, ScratchItem(aware3, if (aware2 > aware3) 1 else 2))

        list.add(ScratchItem(aware4, 3))
        list.add(ScratchItem(aware5, 4))
        list.shuffle()
        return list
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
        // 防止内存泄漏，移除监听器
        crossItemScratchListener?.let { binding.rv.removeOnItemTouchListener(it) }
        crossItemScratchListener = null
    }

    fun <T> MutableList<T>.addRepeat(times: Int, item: T) {
        repeat(times) {
            this.add(item)
        }
    }

}