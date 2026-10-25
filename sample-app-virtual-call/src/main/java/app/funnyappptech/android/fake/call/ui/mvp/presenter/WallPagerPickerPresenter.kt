package app.funnyappptech.android.fake.call.ui.mvp.presenter

import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.mvp.BasePresenter
import app.funnyappptech.android.fake.call.app.Global
import app.funnyappptech.android.fake.call.bean.WallPagerItem
import app.funnyappptech.android.fake.call.function.SettingHelper
import app.funnyappptech.android.fake.call.ui.mvp.view.WallPagerPickerView

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