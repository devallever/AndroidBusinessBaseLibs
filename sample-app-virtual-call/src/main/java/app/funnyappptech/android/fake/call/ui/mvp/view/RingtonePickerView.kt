package app.funnyappptech.android.fake.call.ui.mvp.view

import app.funnyappptech.android.fake.call.bean.RingtoneItem

interface RingtonePickerView {
    fun refreshRingtoneList(data: MutableList<RingtoneItem>, saveIndex: Int)
}