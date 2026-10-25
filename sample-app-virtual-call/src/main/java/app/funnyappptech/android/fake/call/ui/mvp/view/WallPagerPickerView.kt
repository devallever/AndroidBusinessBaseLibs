package app.funnyappptech.android.fake.call.ui.mvp.view

import app.funnyappptech.android.fake.call.bean.WallPagerItem

interface WallPagerPickerView {
    fun refreshWallPagerList(data: MutableList<WallPagerItem>, saveIndex: Int)
}