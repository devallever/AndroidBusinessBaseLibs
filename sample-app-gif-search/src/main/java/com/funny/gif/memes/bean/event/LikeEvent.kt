package com.funny.gif.memes.bean.event

import com.allever.app.gif.memes.ui.adapter.bean.GifItem

class LikeEvent {
    var id: String = ""
    var type: Int = 0
    var isLiked: Boolean = false
    var dataBean: GifItem? = null
}