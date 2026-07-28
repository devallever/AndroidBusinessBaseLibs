package com.alsg.bakericon.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.local.LocalRepo
import com.alsg.bakericon.ui.adapter.CommonPackItemAdapter
import com.alsg.bakericon.ui.adapter.PopularItemAdapter
import com.alsg.bakericon.ui.adapter.data.PackItem
import com.alsg.bakericon.ui.adapter.data.SingleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class IconViewModel : BaseViewModel() {
    val packAdapter = CommonPackItemAdapter()
    val packItemList = mutableListOf<PackItem>()
    val packItemListLiveData = MutableLiveData<MutableList<PackItem>>()
    val popularItemAdapter = PopularItemAdapter()
    val popularItemList = mutableListOf<SingleItem>()
    val popularItemListLiveData = MutableLiveData<MutableList<SingleItem>>()

    fun fetchPackData() {
        viewModelScope.launch() {
            val list = mutableListOf<PackItem>()
            //接口数据
//            val response = NetRepo.iconData()
//            log("IconData = " + response.data?.toJson())
//            list.addAll(DataParserRepo.parseIconResponseData(response.data))
            //本地数据
            list.addAll(LocalRepo.fetchIconData())
            packItemListLiveData.postValue(list)
        }
    }

    fun fetchPopularData() {
        viewModelScope.launch(Dispatchers.IO) {
            //下部分随机200张
            val randomList = LocalRepo.fetchRandomIconList()
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