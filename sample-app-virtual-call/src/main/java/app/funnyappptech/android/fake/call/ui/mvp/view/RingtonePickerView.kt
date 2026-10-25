package org.xm.app.virtual.call.ui.mvp.view

import org.xm.app.virtual.call.bean.RingtoneItem

interface RingtonePickerView {
    fun refreshRingtoneList(data: MutableList<RingtoneItem>, saveIndex: Int)
}