package com.clean.wood.ui.fragments

import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.BoosterManger
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

class OptimizePhoneBoosterFragment : BaseOptimizeFragment() {
    override fun onStartScan() {

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            BoosterManger.ins.boosting()
            waiting(startTime)
        }
    }

    override fun title() = getString(R.string.fun_phone_booster)

    override fun centerIcon() = R.drawable.ic_booster

    override fun funDesc() = getString(R.string.optimize_phone_booster)

    override fun stackKey() = "/optimize_phone_booster"

    override fun funType() = Constant.FunType.PHONE_BOOSTER
}