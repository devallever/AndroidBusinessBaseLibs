package com.alsg.bakericon.vm

import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.ui.adapter.BigFrameImgAdapter
import com.alsg.bakericon.ui.adapter.data.SingleItem

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class PackDetailViewModel : BaseViewModel() {
    val adapter = BigFrameImgAdapter()
    val list = mutableListOf<SingleItem>()
}