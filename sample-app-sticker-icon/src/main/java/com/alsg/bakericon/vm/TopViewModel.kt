package com.alsg.bakericon.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.Constant
import com.alsg.bakericon.local.LocalRepo
import com.alsg.bakericon.network.NetRepo
import com.alsg.bakericon.ui.adapter.TopItemAdapter
import com.alsg.bakericon.ui.adapter.data.SingleItem
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class TopViewModel : BaseViewModel() {
    val adapter = TopItemAdapter()
    val itemList = mutableListOf<SingleItem>()
    val itemListLiveData = MutableLiveData<MutableList<SingleItem>>()

    fun fetchTopData() {
        viewModelScope.launch {

            //接口数据
//            val count = NetRepo.topData().count
            val imageList = mutableListOf<SingleItem>()
            //拼接图片 https://baker.app-lessfunc.uk/baker/top/1.png
//            if (count > 0) {
//                for (i in 1..count) {
//                    val item = SingleItem()
//                    val imageUrl = "${Constant.TOP_PATH}/${i}.png"
////                    log("top imageUrl = $imageUrl")
//                    item.url = imageUrl
//                    imageList.add(item)
//                }
//            }

            //本地数据
            imageList.addAll(LocalRepo.fetchTopData())

            itemListLiveData.value = imageList
        }
    }
}