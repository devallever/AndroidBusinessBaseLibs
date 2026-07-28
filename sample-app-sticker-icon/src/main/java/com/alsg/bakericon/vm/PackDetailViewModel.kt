package com.alsg.bakericon.vm

import com.allever.lib.base.mvvm.BaseViewModel
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