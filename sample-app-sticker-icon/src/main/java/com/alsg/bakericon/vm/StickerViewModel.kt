package com.alsg.bakericon.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.local.LocalRepo
import com.alsg.bakericon.network.NetRepo
import com.alsg.bakericon.ui.adapter.CommonPackItemAdapter
import com.alsg.bakericon.ui.adapter.PopularItemAdapter
import com.alsg.bakericon.ui.adapter.data.PackItem
import com.alsg.bakericon.ui.adapter.data.SingleItem
import com.alsg.bakericon.logic.DataParserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class StickerViewModel : BaseViewModel() {
    val packAdapter = CommonPackItemAdapter()
    val packItemList = mutableListOf<PackItem>()
    val packItemListLiveData = MutableLiveData<MutableList<PackItem>>()
    val popularItemAdapter = PopularItemAdapter()
    val popularItemList = mutableListOf<SingleItem>()
    val popularItemListLiveData = MutableLiveData<MutableList<SingleItem>>()

    fun fetchPackData() {
        viewModelScope.launch {
            //接口数据
//            val response = NetRepo.stickerData()
//            log("StickerData = " + response.data?.toJson())
            val list = mutableListOf<PackItem>()
//            list.addAll(DataParserRepo.parseStickerResponseData(response.data))

            //本地
            list.addAll(LocalRepo.fetchStickerData())
//            log("localResponse = $localResponse")
            packItemListLiveData.value = list
        }
    }

    fun fetchPopularData() {
        viewModelScope.launch(Dispatchers.IO) {
            //下部分随机200张
            val randomList = LocalRepo.fetchRandomStickerList()
            val popularList = mutableListOf<SingleItem>()
            randomList.map {
                val item = SingleItem()
                item.url = it
                popularList.add(item)
            }

            popularItemListLiveData.postValue(popularList)
        }
    }
}