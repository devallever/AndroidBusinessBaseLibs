package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.CoolerManager
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

class OptimizeCpuFragment : BaseOptimizeFragment() {
    override fun onStartScan() {
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            CoolerManager.ins.cooling()
            waiting(startTime)
        }
    }

    override fun title() = getString(R.string.fun_cpu_cooler)

    override fun centerIcon() = R.drawable.ic_cpu

    override fun funDesc() = getString(R.string.optimize_cpu)

    override fun stackKey() = "/optimize_cpu"

    override fun funType() = Constant.FunType.CPU_COOLER
}