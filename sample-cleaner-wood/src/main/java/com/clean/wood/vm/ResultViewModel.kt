package com.clean.wood.vm

import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.data.ReferManager
import com.clean.wood.data.model.ResultFunItem
import com.clean.wood.ui.adapter.ResultFunAdapter
import com.clean.wood.utils.Constant
import java.sql.Ref

class ResultViewModel : ViewModel() {
    var title = WoodApp.context.getString(R.string.result)
    var type = Constant.FunType.JUNK_CLEAN
    val funList by lazy {
        mutableListOf<ResultFunItem>()
    }
    val adapter = ResultFunAdapter(funList)

    fun initList() {
        funList.clear()
        if (type != Constant.FunType.JUNK_CLEAN) {
            funList.add(
                ResultFunItem(
                    Constant.FunType.JUNK_CLEAN,
                    R.drawable.ic_junk,
                    WoodApp.context.getString(R.string.fun_junk_clean),
                    WoodApp.context.getString(R.string.fun_junk_clean_desc),
                    WoodApp.context.getString(R.string.result_btn_junk)
                )
            )
        }

//        if (type != Constant.FunType.VPN) {
//            funList.add(
//                ResultFunItem(
//                    Constant.FunType.VPN,
//                    R.drawable.ic_vpn,
//                    WoodApp.context.getString(R.string.fun_vpn),
//                    WoodApp.context.getString(R.string.fun_vpn_desc),
//                    WoodApp.context.getString(R.string.result_btn_vpn)
//                )
//            )
//        }

        if (type != Constant.FunType.CPU_COOLER && ReferManager.ins.isReferUser()) {
            funList.add(
                ResultFunItem(
                    Constant.FunType.CPU_COOLER,
                    R.drawable.ic_cpu,
                    WoodApp.context.getString(R.string.fun_cpu_cooler),
                    WoodApp.context.getString(R.string.fun_cpu_cooler_desc),
                    WoodApp.context.getString(R.string.result_btn_cpu)
                )
            )
        }

        if (type != Constant.FunType.BATTERY && ReferManager.ins.isReferUser()) {
            funList.add(
                ResultFunItem(
                    Constant.FunType.BATTERY,
                    R.drawable.ic_battery,
                    WoodApp.context.getString(R.string.fun_battery),
                    WoodApp.context.getString(R.string.fun_battery_desc),
                    WoodApp.context.getString(R.string.result_btn_battery)
                )
            )
        }

        if (type != Constant.FunType.APP_MANAGER) {
            funList.add(
                ResultFunItem(
                    Constant.FunType.APP_MANAGER,
                    R.drawable.ic_app,
                    WoodApp.context.getString(R.string.fun_app_manage),
                    WoodApp.context.getString(R.string.fun_app_manager_desc),
                    WoodApp.context.getString(R.string.result_btn_app_manage)
                )
            )
        }

        if (type != Constant.FunType.PHONE_BOOSTER) {
            funList.add(
                ResultFunItem(
                    Constant.FunType.PHONE_BOOSTER,
                    R.drawable.ic_booster,
                    WoodApp.context.getString(R.string.fun_phone_booster),
                    WoodApp.context.getString(R.string.fun_phone_booster_desc),
                    WoodApp.context.getString(R.string.result_btn_phone_booster)
                )
            )
        }
    }

    fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.ScanningNative)
        AdManager.ins.checkAd(Constant.AdPosition.OptimizingNative)
        AdManager.ins.checkAd(Constant.AdPosition.ResultNative)
    }

    fun destroyNative() {
        AdManager.ins.cancelNativeShow(Constant.AdPosition.ResultNative)
    }

    fun showNative(viewGroup: ViewGroup) {
        AdManager.ins.showNative(Constant.AdPosition.ResultNative, viewGroup)
    }
}