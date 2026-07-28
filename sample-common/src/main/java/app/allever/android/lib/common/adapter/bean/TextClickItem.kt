package app.allever.android.lib.common.adapter.bean

import app.allever.android.lib.core.ext.toast

open class TextClickItem(
    var title: String = "",
    var id: Int = 0,
    var itemClick: ((item: TextClickItem) -> Unit)? = { toast("待完善") }
)