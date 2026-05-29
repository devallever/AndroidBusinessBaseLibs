package app.allever.android.lib.common.adapter.bean

class TextDetailClickItem(
    title: String = "",
    var detail: String = "",
    id: Int = 0,
    itemClick: ((item: TextClickItem) -> Unit)? = null
) :
    TextClickItem(title, id, itemClick)