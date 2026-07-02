package com.allever.app.gif.memes.ui.search.model

import app.allever.android.lib.mvvm.base.BaseViewModel
import com.allever.app.gif.memes.ui.adapter.bean.GifItem

class SearchViewModel: BaseViewModel() {

    var gifDataList = mutableListOf<GifItem>()
}