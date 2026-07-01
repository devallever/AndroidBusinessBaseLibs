package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.JunkManager
import com.clean.wood.utils.Constant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OptimizeJunkFragment(private val types: List<Constant.JunkType>) : BaseOptimizeFragment() {
    override fun onStartScan() {

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            JunkManager.ins.cleanJunk(types)
            waiting(startTime)
        }
    }

    override fun title() = getString(R.string.fun_junk_clean)

    override fun centerIcon() = R.drawable.ic_junk

    override fun funDesc() = getString(R.string.optimize_junk)

    override fun stackKey() = "/optimize_junk"

    override fun funType() = Constant.FunType.JUNK_CLEAN
}