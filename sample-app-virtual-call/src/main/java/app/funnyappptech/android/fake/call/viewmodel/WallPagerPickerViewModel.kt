package app.funnyappptech.android.fake.call.viewmodel

import androidx.lifecycle.MutableLiveData
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.app.FakeCall
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.data.WallPagerItem
import app.funnyappptech.android.fake.call.function.SettingStore

class WallPagerPickerViewModel : BaseViewModel() {
    val refreshWallPagerListLiveData by lazy {
        MutableLiveData<WallPagerData>()
    }
    fun getWallPagerData() {
        if (FakeCall.wallPagerItemList.isNotEmpty()) {
            FakeCall.wallPagerItemList.map {
                it.checked = false
            }
        } else {
            var item = WallPagerItem()
            item.title = "Default"
            item.checked = false
            item.resId = R.drawable.vc_default_bg
            FakeCall.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "Xiaomi"
            item.checked = false
            item.resId = R.drawable.vc_xiaomi_bg
            FakeCall.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "HUAWEI"
            item.checked = false
            item.resId = R.drawable.vc_huawei_bg
            FakeCall.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "OPPO"
            item.checked = false
            item.resId = R.drawable.vc_oppo_bg
            FakeCall.wallPagerItemList.add(item)

            item = WallPagerItem()
            item.title = "VIVO"
            item.checked = false
            item.resId = R.drawable.vc_vivo_bg
            FakeCall.wallPagerItemList.add(item)

            FakeCall.wallPagerItemList.map {
                FakeCall.wallPagerItemMap[it.title!!] = it
            }
        }

        val selectedItem = FakeCall.wallPagerItemMap[SettingStore.getWallPagerTitle()]
        selectedItem?.checked = true
        var mSaveRingtoneItemPosition = 0
        if (selectedItem != null) {
            mSaveRingtoneItemPosition = FakeCall.wallPagerItemList.indexOf(selectedItem)
        }

        refreshWallPagerListLiveData.postValue(WallPagerData(FakeCall.wallPagerItemList, mSaveRingtoneItemPosition))
    }

    fun saveWallPager(wallPagerItem: WallPagerItem) {
        SettingStore.setWallPagerTitle(wallPagerItem.title!!)
    }

    class WallPagerData(val list: List<WallPagerItem>, val saveRingtoneItemPosition: Int)
}