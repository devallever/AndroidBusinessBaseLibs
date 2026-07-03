package org.xm.app.virtual.call.ui.mvp.presenter

import com.allever.app.virtual.call.R
import org.xm.app.virtual.call.mvp.BasePresenter
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.bean.WallPagerItem
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.ui.mvp.view.WallPagerPickerView

class WallPagerPickerPresenter : BasePresenter<WallPagerPickerView>() {
    fun getWallPagerData() {
        if (Global.wallPagerItemList.isNotEmpty()) {
            Global.wallPagerItemList.map {
                it.checked = false
            }
        } else {
            var item = WallPagerItem()
            item.title = "Default"
            item.checked = false
            item.resId = R.drawable.vc_default_bg
            Global.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "Xiaomi"
            item.checked = false
            item.resId = R.drawable.vc_xiaomi_bg
            Global.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "HUAWEI"
            item.checked = false
            item.resId = R.drawable.vc_huawei_bg
            Global.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "OPPO"
            item.checked = false
            item.resId = R.drawable.vc_oppo_bg
            Global.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "VIVO"
            item.checked = false
            item.resId = R.drawable.vc_vivo_bg
            Global.wallPagerItemList.add(item)

            Global.wallPagerItemList.map {
                Global.wallPagerItemMap[it.title!!] = it
            }
        }

        val selectedItem = Global.wallPagerItemMap[SettingHelper.getWallPagerTitle()]
        selectedItem?.checked = true
        var mSaveRingtoneItemPosition = 0
        if (selectedItem != null) {
            mSaveRingtoneItemPosition = Global.wallPagerItemList.indexOf(selectedItem)
        }

        mViewRef?.get()?.refreshWallPagerList(Global.wallPagerItemList, mSaveRingtoneItemPosition)
    }

    fun saveWallPager(wallPagerItem: WallPagerItem) {
        SettingHelper.setWallPagerTitle(wallPagerItem.title!!)
    }
}