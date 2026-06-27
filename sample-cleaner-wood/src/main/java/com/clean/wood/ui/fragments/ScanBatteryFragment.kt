package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.data.BatteryManager
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

class ScanBatteryFragment : BaseScanFragment() {

    override fun stackKey(): String {
        return "/scan_battery"
    }

    override fun onStartScan() {
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            BatteryManager.ins.scanning()

            waitingAd2(
                startTime,
                check = {
                    AdManager.ins.isAdReadyNext(Constant.AdPosition.ScanningInter)
                }, next = {
                    lifecycleScope.launch {
                        showScanningInter()
                        jumpOptimizeBattery()
                    }
                }, timeOut = {
                    jumpOptimizeBattery()
                })
        }
    }

    override fun title() = getString(R.string.fun_battery)

    override fun centerIcon() = R.drawable.ic_battery

    private fun jumpOptimizeBattery() {
        pop()
        pushFragment(OptimizeBatteryFragment())
    }
}