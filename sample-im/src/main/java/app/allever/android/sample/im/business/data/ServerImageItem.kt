package app.allever.android.sample.im.business.data

import app.allever.android.lib.common.adapter.bean.TextClickItem

data class ServerImageItem(val url: String, var itemClick: ((item: TextClickItem) -> Unit)? = null) {
}