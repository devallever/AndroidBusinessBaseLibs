package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.data.BoosterManger
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

class ScanPhoneBoosterFragment : BaseScanFragment() {

    override fun stackKey(): String {
        return "/scan_phone_booster"
    }

    override fun onStartScan() {

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            BoosterManger.ins.scanning()

            waitingAd2(
                startTime,
                check = {
                    AdManager.ins.isAdReadyNext(Constant.AdPosition.ScanningInter)
                }, next = {
                    lifecycleScope.launch {
                        showScanningInter()
                        jumpOptimizeBooster()
                    }
                }, timeOut = {
                    jumpOptimizeBooster()
                })
        }
    }

    override fun title() = getString(R.string.fun_phone_booster)

    override fun centerIcon() = R.drawable.ic_booster

    private fun jumpOptimizeBooster() {
        pop()
        pushFragment(OptimizePhoneBoosterFragment())
    }
}