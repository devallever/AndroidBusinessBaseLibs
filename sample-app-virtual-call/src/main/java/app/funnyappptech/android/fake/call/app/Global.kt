package app.funnyappptech.android.fake.call.app

import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.bean.ContactBean
import app.funnyappptech.android.fake.call.bean.RingtoneItem
import app.funnyappptech.android.fake.call.bean.WallPagerItem
import app.funnyappptech.android.fake.call.function.SettingHelper

object Global {
    var ringtoneItemList = mutableListOf<RingtoneItem>()
    var ringtoneItemMap = mutableMapOf<String, RingtoneItem>()

    var wallPagerItemList = mutableListOf<WallPagerItem>()
    var wallPagerItemMap = mutableMapOf<String, WallPagerItem>()

    var leftRepeatCount = SettingHelper.getRepeatCount()

    var contactList = mutableListOf<ContactBean>()

    fun initWallPagerData() {
        var item = WallPagerItem()
        item.title = "Default"
        item.checked = false
        item.resId = R.drawable.vc_default_bg
        wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "Xiaomi"
        item.checked = false
        item.resId = R.drawable.vc_xiaomi_bg
        wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "HUAWEI"
        item.checked = false
        item.resId = R.drawable.vc_huawei_bg
        wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "OPPO"
        item.checked = false
        item.resId = R.drawable.vc_oppo_bg
        wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "VIVO"
        item.checked = false
        item.resId = R.drawable.vc_vivo_bg
        wallPagerItemList.add(item)

        wallPagerItemList.map {
            wallPagerItemMap[it.title!!] = it
        }

        wallPagerItemMap[SettingHelper.getWallPagerTitle()]?.checked = true
    }
}