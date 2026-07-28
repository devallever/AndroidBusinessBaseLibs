package com.alsg.bakericon.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.db.DBRepo
import com.alsg.bakericon.ui.adapter.BigFrameImgAdapter
import com.alsg.bakericon.ui.adapter.data.SingleItem
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class FavouritesViewModel : BaseViewModel() {
    val adapter = BigFrameImgAdapter()
    val listLiveData = MutableLiveData<MutableList<SingleItem>>()

    fun fetchFavouriteData() {
        viewModelScope.launch {
            val favouriteList = DBRepo.fetchFavouriteData()
            listLiveData.postValue(favouriteList)
        }
    }
}