package org.xm.app.virtual.call.ui.mvp.view

import org.xm.app.virtual.call.bean.WallPagerItem

interface WallPagerPickerView {
    fun refreshWallPagerList(data: MutableList<WallPagerItem>, saveIndex: Int)
}