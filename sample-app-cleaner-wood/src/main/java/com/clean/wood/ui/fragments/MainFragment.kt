package com.clean.wood.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.clean.wood.data.AdManager
import com.clean.wood.data.model.FunItem
import com.clean.wood.databinding.WoodFragmentMainBinding
import com.clean.wood.ui.adapter.HomeFunAdapter
import com.clean.wood.utils.Constant
import com.clean.wood.utils.DisplayUtils
import com.clean.wood.vm.MainViewModel
import kotlinx.coroutines.launch

class MainFragment : BaseFragment() {
    private val mViewModel by viewModels<MainViewModel>()
    private lateinit var mBinding: WoodFragmentMainBinding

    override fun stackKey(): String {
        return "/"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = WoodFragmentMainBinding.inflate(layoutInflater)

        mBinding.topBar.post {
            val lp = mBinding.topBar.layoutParams as MarginLayoutParams
            lp.topMargin = DisplayUtils.getStatusBarHeight(requireContext())
            mBinding.topBar.layoutParams = lp
        }

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.apply {
            rvFunction.layoutManager = GridLayoutManager(context, 3)
            rvFunction.adapter = mViewModel.adapter
            mViewModel.adapter.itemClickListener = object : HomeFunAdapter.ItemClickListener {
                override fun onItemClick(item: FunItem) {
                    handleFunClick(item.type)
                }
            }

            circleContainer.post {
                circleContainer.radius = (circleContainer.width / 2).toFloat()
            }
        }

        initListener()

        initObserver()

        return mBinding.root
    }

    private fun handleFunClick(type: Int) {
        mViewModel.clickFunCheckAd()
        lifecycleScope.launch {
            AdManager.ins.showInterAd(Constant.AdPosition.EnterInter)

            when (type) {
                Constant.FunType.JUNK_CLEAN -> {
                    pushFragment(ScanJunkFragment())
                }

                Constant.FunType.VPN -> {

                }

                Constant.FunType.CPU_COOLER -> {
                    pushFragment(ScanCpuFragment())
                }

                Constant.FunType.BATTERY -> {
                    pushFragment(ScanBatteryFragment())
                }

                Constant.FunType.APP_MANAGER -> {
                    pushFragment(ScanAppFragment())
                }

                Constant.FunType.PHONE_BOOSTER -> {
                    pushFragment(ScanPhoneBoosterFragment())
                }

                else -> {
//                        toast(item.name)
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.getStoragePercent()
        mViewModel.getRamPercent()
        mViewModel.scanJunk()
    }

    override fun onHide() {
        super.onHide()
        mViewModel.destroyNative()
    }

    override fun onShow() {
        if (isAdded) {
            mViewModel.checkAd()
            mViewModel.showNative(mBinding.adContainer)
            mViewModel.updateFunItemList()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        mViewModel.junkSizeLiveData.observe(viewLifecycleOwner) {
            mBinding.tvJunkSize.text = it.toString()
        }

        mViewModel.junkSizeUnitLiveData.observe(viewLifecycleOwner) {
            mBinding.tvJunkUnit.text = it
        }

        mViewModel.storagePercentageLiveData.observe(viewLifecycleOwner) {
            mBinding.circleProgress.setProgress(it.toFloat())
            mBinding.tvStorage.text = "$it%"
        }

        mViewModel.ramPercentageLiveData.observe(viewLifecycleOwner) {
            mBinding.tvRam.text = "$it%"
        }
    }

    private fun initListener() {
        mBinding.apply {
            ivSetting.setOnClickListener {
                pushFragment(SettingsFragment())
            }

            circleContainer.setOnClickListener {
                handleFunClick(Constant.FunType.JUNK_CLEAN)
            }

            btnSmartClean.setOnClickListener {
                handleFunClick(Constant.FunType.JUNK_CLEAN)
            }
        }
    }
}