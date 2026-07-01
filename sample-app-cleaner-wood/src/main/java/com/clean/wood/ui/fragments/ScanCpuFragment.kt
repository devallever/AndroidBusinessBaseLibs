package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.data.CoolerManager
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

class ScanCpuFragment : BaseScanFragment() {

    override fun stackKey(): String {
        return "/scan_cpu"
    }

    override fun onStartScan() {
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            CoolerManager.ins.scanning()

            waitingAd2(
                startTime,
                check = {
                    AdManager.ins.isAdReadyNext(Constant.AdPosition.ScanningInter)
                }, next = {
                    lifecycleScope.launch {
                        showScanningInter()
                        jumpOptimizeCpu()
                    }
                }, timeOut = {
                    jumpOptimizeCpu()
                })
        }
    }

    override fun title() = getString(R.string.fun_cpu_cooler)

    override fun centerIcon() = R.drawable.ic_cpu

    private fun jumpOptimizeCpu() {
        pop()
        pushFragment(OptimizeCpuFragment())
    }
}