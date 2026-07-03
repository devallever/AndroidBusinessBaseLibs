package org.xm.app.virtual.call.app

import com.allever.app.virtual.call.R
import org.xm.app.virtual.call.bean.ContactBean
import org.xm.app.virtual.call.bean.RingtoneItem
import org.xm.app.virtual.call.bean.WallPagerItem
import org.xm.app.virtual.call.function.SettingHelper

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
        item.resId = R.drawable.default_bg
        Global.wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "Xiaomi"
        item.checked = false
        item.resId = R.drawable.xiaomi_bg
        Global.wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "HUAWEI"
        item.checked = false
        item.resId = R.drawable.huawei_bg
        Global.wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "OPPO"
        item.checked = false
        item.resId = R.drawable.oppo_bg
        Global.wallPagerItemList.add(item)

        item = WallPagerItem()
        item.title = "VIVO"
        item.checked = false
        item.resId = R.drawable.vivo_bg
        Global.wallPagerItemList.add(item)

        wallPagerItemList.map {
            wallPagerItemMap[it.title!!] = it
        }

        wallPagerItemMap[SettingHelper.getWallPagerTitle()]?.checked = true
    }
}