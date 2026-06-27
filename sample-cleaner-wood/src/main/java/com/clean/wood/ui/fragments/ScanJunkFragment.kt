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
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.data.JunkManager
import com.clean.wood.databinding.FragmentJunkScanBinding
import com.clean.wood.utils.Constant
import com.clean.wood.utils.toast
import com.clean.wood.vm.ScanJunkViewModel
import kotlinx.coroutines.launch

class ScanJunkFragment : BaseFragment() {
    private lateinit var mBinding: FragmentJunkScanBinding

    private val mViewModel by viewModels<ScanJunkViewModel>()

    override fun stackKey() = "/scan_junk"

    override fun backPressedEnable() = true

    override fun onBackPressed() = handleClickBack()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentJunkScanBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.apply {
            includeTopBar.tvTitle.text = getString(R.string.fun_junk_clean)
            includeTopBar.ivBack.setOnClickListener {
                handleClickBack()
            }

            rvJunk.layoutManager = LinearLayoutManager(requireContext())
            rvJunk.adapter = mViewModel.adapter
        }

        initObserver()

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            mViewModel.startProgress()
            JunkManager.ins.scanJunk(WoodApp.junkSize)
            waitingAd2(startTime,
                check = {
                    AdManager.ins.isAdReadyNext(Constant.AdPosition.ScanningInter)
                }, next = {
                    mViewModel.finishProgress()
                }, timeOut = {
                    mViewModel.finishProgress()
                })
        }

        mBinding.tvClean.setOnClickListener {
            if (mViewModel.scanning) {
                toast(getString(R.string.scanning))
                return@setOnClickListener
            }
            val selectTypeList = mViewModel.adapter.selectTypeList()
            if (selectTypeList.isEmpty()) {
                toast(getString(R.string.please_select_one_item))
                return@setOnClickListener
            }
            pop()
            pushFragment(OptimizeJunkFragment(selectTypeList))
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.checkAd()
    }

    private fun initObserver() {
        mViewModel.junkSizeLiveData.observe(viewLifecycleOwner) {
            mBinding.tvJunkSize.text = it
        }

        mViewModel.progressLiveData.observe(viewLifecycleOwner) {
            mBinding.progressBar.progress = it
            if (it == 100) {
                mViewModel.scanning = false
                mBinding.tvClean.text = getString(R.string.clean)
                mViewModel.updateSelectedJunkSize()

                mViewModel.showScanningAd()
            }
        }
    }
}