package com.allever.app.gif.memes.ui.main.model

import app.allever.android.lib.mvvm.base.BaseViewModel
import com.allever.app.gif.memes.ui.adapter.GifAdapter
import com.allever.app.gif.memes.ui.adapter.bean.GifItem

class TrendViewModel: BaseViewModel() {

    var gifDataList = mutableListOf<GifItem>()
    lateinit var adapter: GifAdapter
}