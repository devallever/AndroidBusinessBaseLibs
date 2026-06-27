package com.clean.wood.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.clean.wood.data.AdManager
import com.clean.wood.data.model.ResultFunItem
import com.clean.wood.databinding.FragmentResultBinding
import com.clean.wood.ui.adapter.ResultFunAdapter
import com.clean.wood.utils.Constant
import com.clean.wood.vm.ResultViewModel
import kotlinx.coroutines.launch

class ResultFragment : BaseFragment() {


    companion object {
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_TITLE = "title"
        fun newIns(type: Int, title: String): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putInt(EXTRA_TYPE, type)
            args.putString(EXTRA_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mBinding: FragmentResultBinding
    private val mViewModel by viewModels<ResultViewModel>()

    override fun stackKey() = "/result"

    override fun backPressedEnable() = true

    override fun onBackPressed() = handleClickBack()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentResultBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        arguments?.getString(EXTRA_TITLE)?.let {
            mViewModel.title = it
        }

        arguments?.getInt(EXTRA_TYPE)?.let {
            mViewModel.type = it
        }

        mViewModel.initList()

        mBinding.apply {
            includeTopBar.tvTitle.text = mViewModel.title
            includeTopBar.ivBack.setOnClickListener {
                handleClickBack()
            }

            rvFunction.layoutManager = LinearLayoutManager(requireContext())
            rvFunction.adapter = mViewModel.adapter
            mViewModel.adapter.itemClickListener = object : ResultFunAdapter.ItemClickListener {
                override fun onBtnClick(item: ResultFunItem) {
                    lifecycleScope.launch {
                        AdManager.ins.showInterAd(Constant.AdPosition.EnterInter)
                        handleFunClick(item.type)
                    }
                }
            }
        }

        return mBinding.root
    }

    private fun handleFunClick(type: Int) {
        pop()
        when (type) {
            Constant.FunType.JUNK_CLEAN -> {
                pushFragment(ScanJunkFragment())
            }

            Constant.FunType.VPN -> {
//                            pushFragment()
            }

            Constant.FunType.CPU_COOLER -> {
                pushFragment(OptimizeCpuFragment())
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

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.checkAd()
        mViewModel.showNative(mBinding.adContainer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.destroyNative()
    }
}