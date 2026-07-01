package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.BatteryManager
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

class OptimizeBatteryFragment : BaseOptimizeFragment() {
    override fun onStartScan() {

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            BatteryManager.ins.optimizing()
            waiting(startTime)

        }
    }

    override fun title() = getString(R.string.fun_battery)

    override fun centerIcon() = R.drawable.ic_battery

    override fun funDesc() = getString(R.string.optimize_battery)

    override fun stackKey() = "/optimize_battery"

    override fun funType() = Constant.FunType.BATTERY
}