package com.clean.wood.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.data.AppManager
import com.clean.wood.data.model.AppItem
import com.clean.wood.databinding.ScanAppFgBinding
import com.clean.wood.utils.Constant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.truncate

class ScanAppFragment : BaseScanFragment() {

    override fun stackKey(): String {
        return "/scan_app"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val fg = ScanAppFgBinding.inflate(layoutInflater, null, false).root
        mBinding.scanFg.addView(fg, lp)
        return root
    }

    override fun onStartScan() {

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            val appList = AppManager.ins.scanApp()
            WoodApp.appInfoLost.clear()
            appList.forEach {
                WoodApp.appInfoLost.add(
                    AppItem(
                        it.icon,
                        it.appName,
                        it.installTime,
                        it.usageSize,
                        false
                    )
                )
            }

            waitingAd2(
                startTime,
                check = {
                    AdManager.ins.isAdReadyNext(Constant.AdPosition.ScanningInter)
                }, next = {
                    lifecycleScope.launch {
                        showScanningInter()
                        jumpAppList()
                    }
                }, timeOut = {
                    jumpAppList()
                })
        }
    }

    override fun title() = getString(R.string.fun_app_manage)

    override fun centerIcon() = R.drawable.ic_robot

    private fun jumpAppList() {
        pop()
        pushFragment(OptimizeAppFragment())
    }
}