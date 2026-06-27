package com.clean.wood.vm

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.data.JunkManager
import com.clean.wood.data.ReferManager
import com.clean.wood.data.model.FunItem
import com.clean.wood.ui.adapter.HomeFunAdapter
import com.clean.wood.utils.Constant
import com.clean.wood.utils.StorageUtils
import com.clean.wood.utils.log
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val storagePercentageLiveData = MutableLiveData<Int>()
    val ramPercentageLiveData = MutableLiveData<Int>()
    val junkSizeLiveData = MutableLiveData<Double>()
    val junkSizeUnitLiveData = MutableLiveData("MB")

    private val FUN_JUNK = FunItem(
        Constant.FunType.JUNK_CLEAN,
        R.drawable.ic_fun_junk_bg,
        R.drawable.ic_junk,
        WoodApp.context.getString(R.string.fun_junk_clean),
        R.color.color_fun_junk
    )
    private val FUN_VPN = (FunItem(
        Constant.FunType.VPN,
        R.drawable.ic_fun_vpn_bg,
        R.drawable.ic_vpn,
        WoodApp.context.getString(R.string.fun_vpn),
        R.color.color_fun_vpn
    ))
    private val FUN_CPU_COOLER = FunItem(
        Constant.FunType.CPU_COOLER,
        R.drawable.ic_fun_cup_bg,
        R.drawable.ic_cpu,
        WoodApp.context.getString(R.string.fun_cpu_cooler),
        R.color.color_fun_cpu
    )
    private val FUN_BATTERY = FunItem(
        Constant.FunType.BATTERY,
        R.drawable.ic_fun_battery_bg,
        R.drawable.ic_battery,
        WoodApp.context.getString(R.string.fun_battery),
        R.color.color_fun_battery
    )
    private val FUN_APP_MANAGE = FunItem(
        Constant.FunType.APP_MANAGER,
        R.drawable.ic_fun_app_bg,
        R.drawable.ic_app,
        WoodApp.context.getString(R.string.fun_app_manage),
        R.color.color_fun_app
    )
    private val FUN_PHONE_BOOSTER = FunItem(
        Constant.FunType.PHONE_BOOSTER,
        R.drawable.ic_fun_booster_bg,
        R.drawable.ic_booster,
        WoodApp.context.getString(R.string.fun_phone_booster),
        R.color.color_fun_booster
    )

    private val funList = mutableListOf<FunItem>().apply {
        add(FUN_JUNK)
        if (ReferManager.ins.isReferUser()) {
            add(FUN_CPU_COOLER)
            add(FUN_BATTERY)
        }
        add(FUN_APP_MANAGE)
        add(FUN_PHONE_BOOSTER)
    }

    val adapter = HomeFunAdapter(funList)

    init {
        WoodApp.junkSize.observeForever {
            val sizeDisplay = StorageUtils.convertBytesToMBOrGB((it * 1024).toLong())
            log("sizeDisplay = $sizeDisplay")
            val sizeDisplayArray = sizeDisplay.split(" ")
            val size = sizeDisplayArray[0].toDouble()
            val unit = sizeDisplayArray[1]
            junkSizeLiveData.postValue(size)
            junkSizeUnitLiveData.postValue(unit)
        }
    }

    fun getStoragePercent() {
        viewModelScope.launch {
            storagePercentageLiveData.postValue((JunkManager.ins.getStorageUsagePercent() * 100).toInt())
        }
    }

    fun getRamPercent() {
        viewModelScope.launch {
            ramPercentageLiveData.postValue((JunkManager.ins.getRamUsagePercent() * 100).toInt())
        }
    }

    fun scanJunk() {
        viewModelScope.launch {
            JunkManager.ins.scanJunk(WoodApp.junkSize)
        }
    }

    fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.HomeNative)
        AdManager.ins.checkAd(Constant.AdPosition.ScanningNative)
        AdManager.ins.checkAd(Constant.AdPosition.BackupNative)

        AdManager.ins.checkAd(Constant.AdPosition.ScanningInter)
        AdManager.ins.checkAd(Constant.AdPosition.EnterInter)
        AdManager.ins.checkAd(Constant.AdPosition.ExitInter)
        AdManager.ins.checkAd(Constant.AdPosition.BackupInter)
    }

    fun clickFunCheckAd() {
        AdManager.ins.checkAd(Constant.AdPosition.ScanningInter)
        AdManager.ins.checkAd(Constant.AdPosition.OptimizingInter)
    }

    fun showNative(viewGroup: ViewGroup) {
        AdManager.ins.showNative(Constant.AdPosition.HomeNative, viewGroup)
    }

    fun destroyNative() {
        AdManager.ins.cancelNativeShow(Constant.AdPosition.HomeNative)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFunItemList() {
        updateFunListData()
        adapter.notifyDataSetChanged()
    }

    private fun updateFunListData() {
        if (ReferManager.ins.isReferUser()) {
            if (!funList.contains(FUN_CPU_COOLER)) {
                funList.add(1, FUN_CPU_COOLER)
            }
            if (!funList.contains(FUN_BATTERY)) {
                funList.add(2, FUN_BATTERY)
            }
        } else {
            funList.remove(FUN_CPU_COOLER)
            funList.remove(FUN_BATTERY)
        }
    }
}