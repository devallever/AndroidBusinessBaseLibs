package app.allever.android.lib.common.adapter.bean

import app.allever.android.lib.core.ext.toast

class TextDetailClickItem(
    title: String = "",
    var detail: String = "",
    id: Int = 0,
    itemClick: ((item: TextClickItem) -> Unit)? = { toast("待完善") }
) :
    TextClickItem(title, id, itemClick)